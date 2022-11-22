package com.example.sangsangstagram.view.home.post.postcreate

import android.net.Uri
import androidx.annotation.StringRes

data class PostCreateUiState(
    val selectedImage: Uri? = null,
    @StringRes
    val userMessage: Int? = null,
    val isCreating: Boolean = true,
    val isLoading: Boolean = false,
    val successToUpload: Boolean = false
)