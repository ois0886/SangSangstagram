package com.example.sangsangstagram.view.home.mypage

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
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

class InfoUpdateActivity : AppCompatActivity() {

    private val viewModel: InfoUpdateViewModel by viewModels()

    private lateinit var binding: ActivityInfoUpdateBinding

    companion object {
        fun getIntent(context: Context, userDetail: UserDetail): Intent {
            return Intent(context, InfoUpdateActivity::class.java).apply {
                putExtra("userDetail", userDetail)
            }
        }
    }

    private fun getUserDetail(): UserDetail {
        return intent.getSerializableExtra("userDetail") as UserDetail
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { imageUri ->
            if (imageUri != null) {
                @Suppress("DEPRECATION")
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            contentResolver,
                            imageUri
                        )
                    )
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                }
                viewModel.updateImageBitmap(bitmap)
                binding.profileImage.setImageBitmap(bitmap)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userDetail = getUserDetail()
        viewModel.bind(userDetail.name, userDetail.introduce)
        initUi(userDetail) // TODO : userDetail uiState 어찌 넣을지 고민하기

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.infoUpdateUiState.collect(::updateUi)
            }
        }

        binding.backButton.setOnClickListener {
            startUserPageView()
        }

        binding.logoutButton.setOnClickListener {
            logOut()
        }
    }

    private fun initUi(userdata: UserDetail) {

        binding.apply {
            imageView.setOnClickListener {
                onClickImage()
            }
            val storage: FirebaseStorage =
                FirebaseStorage.getInstance("gs://sangsangstagram.appspot.com/")
            val storageReference = storage.reference
            userdata.profileImageUrl?.let {
                Glide.with(this@InfoUpdateActivity)
                    .load(storageReference.child(it))
                    .fallback(R.drawable.ic_baseline_person_pin_24)
                    .circleCrop()
                    .into(binding.profileImage)
            }
            viewModel.bind(userdata.name, userdata.introduce)
            userNameEditText.setText(userdata.name)
            userIntroduceEditText.setText(userdata.introduce)

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
            setOnClickListener {
                viewModel.sendChangedInfo()
                finish()
                startUserPageView()
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
        val selectedImage = viewModel.infoUpdateUiState.value.selectedImageBitmap
        val oldProfileImageUrl = viewModel.infoInitUiState.value.userDetail?.profileImageUrl
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

    private fun startUserPageView() {
        val intent = UserPageActivity.getIntent(this, Firebase.auth.currentUser?.uid.toString())
        finish()
        startActivity(intent)
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