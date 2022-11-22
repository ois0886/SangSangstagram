package com.example.sangsangstagram.view.home.post.postcreate

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sangsangstagram.R
import com.example.sangsangstagram.data.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostCreateViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PostCreateUiState())
    val uiState = _uiState.asStateFlow()

    fun selectImage(uri: Uri) {
        _uiState.update { it.copy(selectedImage = uri) }
    }

    fun changeToEditMode() {
        _uiState.update { it.copy(isCreating = false) }
    }

    fun uploadContent(content: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = PostRepository.uploadPost(content, uiState.value.selectedImage!!)
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

    fun editContent(uuid: String, content: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result: Result<Unit> = if (_uiState.value.selectedImage != null) {
                PostRepository.editPost(uuid, content, uiState.value.selectedImage!!)
            } else {
                PostRepository.editPostOnlyContent(uuid, content)
            }
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