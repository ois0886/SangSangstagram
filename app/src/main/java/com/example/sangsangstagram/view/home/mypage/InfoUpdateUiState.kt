package com.example.sangsangstagram.view.home.mypage

import android.graphics.Bitmap
import com.example.sangsangstagram.domain.model.UserDetail

data class InfoUpdateUiState(
    val name: String = "",
    val selectedImageBitmap: Bitmap? = null,
    val isImageChanged: Boolean = false,
    val introduce: String = "",
    val successToSave: Boolean = false,
    val isLoading: Boolean = false,
    val userMessage: String? = null,
)

data class _infoInitUiState(
    val userDetail: UserDetail? = null
)