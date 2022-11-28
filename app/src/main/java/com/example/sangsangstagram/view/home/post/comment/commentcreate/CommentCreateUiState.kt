package com.example.sangsangstagram.view.home.post.comment.commentcreate

import androidx.annotation.StringRes

data class CommentCreateUiState(
    @StringRes
    val userMessage: Int? = null,
    val isCreating: Boolean = true,
    val isLoading: Boolean = false,
    val successToUpload: Boolean = false
)