package com.example.sangsangstagram.view.home.mypage

import android.graphics.Bitmap

data class InfoUpdateUiState(
    val name: String = "",
    val selectedImageBitmap: Bitmap? = null,
    val isImageChanged: Boolean = false,
    val introduce: String = "",
    val successToSave: Boolean = false,
    val isLoading: Boolean = false,
    val userMessage: String? = null,
)