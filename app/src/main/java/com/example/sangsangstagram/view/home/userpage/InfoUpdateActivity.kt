package com.example.sangsangstagram.view.home.userpage

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
import com.example.sangsangstagram.R
import com.example.sangsangstagram.databinding.ActivityInfoUpdateBinding
import com.example.sangsangstagram.domain.model.UserDetail
import com.example.sangsangstagram.view.login.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.io.Serializable

class InfoUpdateActivity : AppCompatActivity() {

    fun <T : Serializable?> Intent.getSerializable(key: String, m_class: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            this.getSerializableExtra(key, m_class)!!
        else
            this.getSerializableExtra(key) as T
    }

    private val viewModel: InfoUpdateViewModel by viewModels()

    private lateinit var binding: ActivityInfoUpdateBinding

    companion object {
        fun getIntent(
            context: Context,
            userDetail: UserDetail
        ): Intent {
            return Intent(context, InfoUpdateActivity::class.java).apply {
                putExtra("userDetail", userDetail)
            }
        }
    }

    fun Uri.toBitmap(context: Context): Bitmap =
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

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { imageUri ->
            if (imageUri != null) {
                val bitmap = imageUri.toBitmap(this)
                viewModel.updateImageBitmap(bitmap)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userDetail = intent.getSerializable("userDetail", UserDetail::class.java)
        viewModel.bind(userDetail.name, userDetail.introduce)
        initUi(userDetail)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.infoUpdateUiState.collect(::updateUi)
            }
        }

        binding.backButton.setOnClickListener {
            startUserPageActivity(userDetail)
            finish()
        }

        binding.logoutButton.setOnClickListener {
            logOut()
        }
        binding.doneButton.setOnClickListener {
            viewModel.sendChangedInfo()
        }
    }

    private fun initUi(userDetail: UserDetail) {
        val storage: FirebaseStorage =
            FirebaseStorage.getInstance("gs://sangsangstagram.appspot.com/")
        val storageReference = storage.reference
        val pathReference = userDetail.profileImageUrl?.let { storageReference.child(it) }


        binding.apply {
            imageView.setOnClickListener {
                onClickImage(userDetail)
            }
            pathReference?.downloadUrl?.addOnSuccessListener { uri ->
                Glide.with(this@InfoUpdateActivity)
                    .load(uri)
                    .fallback(R.drawable.ic_baseline_person_pin_24)
                    .circleCrop()
                    .into(binding.profileImage)
            }
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
        }
        if (uiState.isImageChanged) {
            updateUserImage(uiState.selectedImageBitmap)
        }

        if (uiState.successToSave) {
            showSnackBar(getString(R.string.chage_profile))
        }
        if (uiState.userMessage != null) {
            showSnackBar(uiState.userMessage)
            viewModel.userMessageShown()
        }
    }

    private fun onClickImage(userDetail: UserDetail) {
        val selectedImage = viewModel.infoUpdateUiState.value.selectedImageBitmap
        val oldProfileImageUrl = userDetail.profileImageUrl
        val isImageChanged = viewModel.infoUpdateUiState.value.isImageChanged

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

    private fun startUserPageActivity(userDetail: UserDetail) {
        val intent = UserPageActivity.getIntent(this, userDetail.uuid)
        startActivity(intent)
    }
}