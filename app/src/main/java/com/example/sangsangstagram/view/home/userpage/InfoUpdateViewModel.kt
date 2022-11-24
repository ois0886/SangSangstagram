package com.example.sangsangstagram.view.home.userpage

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sangsangstagram.data.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InfoUpdateViewModel : ViewModel() {

    private val _infoUpdateUiState = MutableStateFlow(InfoUpdateUiState())
    val infoUpdateUiState: StateFlow<InfoUpdateUiState> = _infoUpdateUiState.asStateFlow()

    private var didBound = false
    private lateinit var oldName: String
    private lateinit var oldIntroduce: String

    val isChanged
        get() = infoUpdateUiState.value.isImageChanged ||
                oldName != infoUpdateUiState.value.name ||
                oldIntroduce != infoUpdateUiState.value.introduce

    val canSave: Boolean
        get() = infoUpdateUiState.value.name.isNotEmpty() && !infoUpdateUiState.value.isLoading && isChanged

    fun bind(oldName: String, oldIntroduce: String) {
        check(!didBound)
        didBound = true
        this.oldName = oldName
        this.oldIntroduce = oldIntroduce
        _infoUpdateUiState.update {
            it.copy(name = oldName, introduce = oldIntroduce)
        }
    }

    fun updateName(name: String) {
        _infoUpdateUiState.update { it.copy(name = name) }
    }

    fun updateIntroduce(introduce: String) {
        _infoUpdateUiState.update { it.copy(introduce = introduce) }
    }

    fun updateImageBitmap(bitmap: Bitmap?) {
        _infoUpdateUiState.update { it.copy(selectedImageBitmap = bitmap, isImageChanged = true) }
    }

    fun sendChangedInfo() {
        if (!isChanged) {
            _infoUpdateUiState.update { it.copy(successToSave = true) }
            return
        }
        _infoUpdateUiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val uiStateValue = infoUpdateUiState.value
            val result = UserRepository.updateInfo(
                name = uiStateValue.name,
                introduce = uiStateValue.introduce,
                profileImage = uiStateValue.selectedImageBitmap,
                isChangedImage = uiStateValue.isImageChanged
            )
            if (result.isSuccess) {
                _infoUpdateUiState.update {
                    it.copy(successToSave = true, isLoading = false)
                }
            } else {
                _infoUpdateUiState.update {
                    it.copy(
                        userMessage = result.exceptionOrNull()!!.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun userMessageShown() {
        _infoUpdateUiState.update { it.copy(userMessage = null) }
    }
}