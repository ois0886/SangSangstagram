package com.example.sangsangstagram.view.home.post.postcreate

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.sangsangstagram.R
import com.example.sangsangstagram.databinding.ActivityPostCreateBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class PostCreateActivity : AppCompatActivity() {
    lateinit var binding: ActivityPostCreateBinding

    private val viewModel: PostCreateViewModel by viewModels()

    private val fileChooserContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            if (imageUri != null) {
                viewModel.selectImage(imageUri)
            } else if (viewModel.uiState.value.selectedImage == null && viewModel.uiState.value.isCreating) {
                finish()
            }
        }

    companion object {
        fun getIntent(
            context: Context,
            postContent: String,
            postImage: String,
            postUuid: String
        ): Intent {
            return Intent(context, PostCreateActivity::class.java)
                .putExtra("content", postContent)
                .putExtra("image", postImage)
                .putExtra("uuid", postUuid)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.postBackButton.setOnClickListener {
            finish()
        }


        val glide = Glide.with(this)
        val contentEditText = binding.imageExpression
        val imageView = binding.addImage
        val backButton = binding.postBackButton
        val postButton = binding.postButton
        val postContent = intent.getStringExtra("content")
        val postImage = intent.getStringExtra("image")
        val postUuid = intent.getStringExtra("uuid")

        val storage: FirebaseStorage =
            FirebaseStorage.getInstance("gs://sangsangstagram.appspot.com/")
        val storageReference = storage.reference

        if (postContent != null && postImage != null && postUuid != null) {
            viewModel.changeToEditMode()
            val postReference = postImage.let { storageReference.child(it) }
            postReference.downloadUrl.addOnSuccessListener { uri ->
                glide
                    .load(uri)
                    .into(binding.addImage)
            }
            binding.toolbarTitle.text = getString(R.string.post_edit)
            binding.postButton.text = getString(R.string.post_editting)
            contentEditText.setText(postContent)
        } else {
            showImagePicker()
        }

        postButton.setOnClickListener {
            if (!viewModel.uiState.value.isCreating) {
                viewModel.editContent(postUuid.toString(), contentEditText.text.toString())
            } else {
                viewModel.uploadContent(contentEditText.text.toString())
            }
        }

        imageView.setOnClickListener {
            showImagePicker()
        }

        backButton.setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::updateUi)
            }
        }
    }

    private fun updateUi(uiState: PostCreateUiState) {
        if (uiState.selectedImage != null) {
            binding.addImage.setImageURI(uiState.selectedImage)
        }
        if (uiState.userMessage != null) {
            showSnackBar(getString(uiState.userMessage))
            viewModel.userMessageShown()
        }
        if (uiState.successToUpload) {
            Toast.makeText(this, "게시글 업로드에 성공했습니다.", Toast.LENGTH_LONG).show()
            setResult(RESULT_OK)
            finish()
        }

        binding.postButton.apply {
            isEnabled = !uiState.isLoading
            alpha = if (uiState.isLoading) 0.5F else 1.0F
        }
    }

    private fun showImagePicker() {
        if (!viewModel.uiState.value.isLoading) {
            fileChooserContract.launch("image/*")
        }
    }

    private fun showSnackBar(message: String) {
        val root = binding.postingRoot
        Snackbar.make(root, message, Snackbar.LENGTH_LONG).show()
    }
}