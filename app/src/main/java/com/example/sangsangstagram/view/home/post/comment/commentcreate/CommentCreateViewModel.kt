package com.example.sangsangstagram.view.home.post.comment.commentcreate

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sangsangstagram.R
import com.example.sangsangstagram.data.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommentCreateViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CommentCreateUiState())
    val uiState = _uiState.asStateFlow()

    fun changeToEditMode() {
        _uiState.update { it.copy(isCreating = false) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadContent(content: String, postUuid: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = CommentRepository.uploadComment(content, postUuid)
            if (result.isSuccess) {
                _uiState.update { it.copy(successToUpload = true, isLoading = false) }
            } else {
                _uiState.update {
                    it.copy(
                        userMessage = R.string.failed,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun editContent(commentUuid: String, content: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result: Result<Unit> = CommentRepository.editComment(commentUuid, content)
            if (result.isSuccess) {
                _uiState.update { it.copy(successToUpload = true, isLoading = false) }
            } else {
                _uiState.update {
                    it.copy(
                        userMessage = R.string.failed,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
