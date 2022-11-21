package com.example.sangsangstagram.view.home.mypage

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sangsangstagram.R
import com.example.sangsangstagram.databinding.ActivityInfoUpdateBinding
import com.example.sangsangstagram.view.login.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class InfoUpdateActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context, userUuid: String): Intent {
            return Intent(context, InfoUpdateActivity::class.java).apply {
                putExtra("userUuid", userUuid)
            }
        }
    }

    private fun getUserUuid(): String {
        return intent.getStringExtra("userUuid")!!
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { imageUri ->
            if (imageUri != null) {
                val bitmap = imageUri.toBitmap(this)
                viewModel.updateImageBitmap(bitmap)
            }

        }

    private fun Uri.toBitmap(context: Context): Bitmap =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, this)
            ) { decoder: ImageDecoder, _: ImageDecoder.ImageInfo?, _: ImageDecoder.Source? ->
                decoder.isMutableRequired = true
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            BitmapDrawable(
                context.resources,
                MediaStore.Images.Media.getBitmap(context.contentResolver, this)
            ).bitmap
        }

    private val viewModel: InfoUpdateViewModel by viewModels()

    private lateinit var binding: ActivityInfoUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.initUpdateInfo(getUserUuid())

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState2.collect(::initUi)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState1.collect(::updateUi)
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.logoutButton.setOnClickListener {
            logOut()
        }

        binding.imageView.setOnClickListener {
            onClickImage()
        }
    }

    private fun initUi(uiState: InfoInitUiState) {
        val storage: FirebaseStorage =
            FirebaseStorage.getInstance("gs://sangsangstagram.appspot.com/")
        val storageReference = storage.reference
        val userDetail = uiState.userDetail
        val pathReference = userDetail?.profileImageUrl?.let { storageReference.child(it) }

        if (userDetail != null) {
            binding.apply {
                pathReference?.downloadUrl?.addOnSuccessListener { uri ->
                    Glide.with(this@InfoUpdateActivity)
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .fallback(R.drawable.ic_baseline_person_pin_24)
                        .centerCrop()
                        .into(profileImage)
                }
                viewModel.bind(userDetail.name, userDetail.introduce)
                userNameEditText.setText(userDetail.name)
                userIntroduceEditText.setText(userDetail.introduce)

                userNameEditText.addTextChangedListener {
                    if (it != null) {
                        viewModel.updateName(it.toString())
                    }
                }
                userIntroduceEditText.addTextChangedListener {
                    if (it != null) {
                        viewModel.updateIntroduce(it.toString())
                    }
                }
            }
        }
    }

    private fun updateUserImage(bitmap: Bitmap?) {
        Glide.with(this@InfoUpdateActivity)
            .load(bitmap)
            .fallback(R.drawable.ic_baseline_person_pin_24)
            .circleCrop()
            .into(binding.profileImage)
    }

    private fun updateUi(uiState: InfoUpdateUiState) {
        binding.doneButton.apply {
            val canSave = viewModel.canSave
            isEnabled = canSave
            setOnClickListener {
                viewModel.sendChangedInfo()
                finish()
            }
        }
        if (uiState.isImageChanged) {
            updateUserImage(uiState.selectedImageBitmap)
        }

        if (uiState.successToSave) {
            showSnackBar(getString(R.string.changeImage))
        }
        if (uiState.userMessage != null) {
            showSnackBar(uiState.userMessage)
            viewModel.userMessageShown()
        }
    }

    private fun onClickImage() {
        val selectedImage = viewModel.uiState1.value.selectedImageBitmap
        val oldProfileImageUrl = viewModel.uiState2.value.userDetail?.profileImageUrl
        val isImageChanged = viewModel.uiState1.value.isImageChanged

        if (selectedImage == null && (oldProfileImageUrl == null || isImageChanged)) {
            showImagePicker()
        } else {
            MaterialAlertDialogBuilder(this)
                .setItems(R.array.image_options) { _, which ->
                    when (which) {
                        0 -> {
                            showImagePicker()
                        }
                        1 -> {
                            viewModel.updateImageBitmap(null)
                        }
                        else -> throw IllegalArgumentException()
                    }
                }.create()
                .show()
        }
    }

    private fun logOut() {
        Firebase.auth.signOut()
        finish()
        val intent = LoginActivity.getIntent(this)
        startActivity(intent)
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showImagePicker() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

}