package com.example.sangsangstagram.view.home.mypage

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

    private val _uiState1 = MutableStateFlow(InfoUpdateUiState())
    val uiState1: StateFlow<InfoUpdateUiState> = _uiState1.asStateFlow()

    private val _uiState2 = MutableStateFlow(InfoInitUiState())
    val uiState2: StateFlow<InfoInitUiState> = _uiState2.asStateFlow()

    private var didBound = false
    private lateinit var oldName: String
    private lateinit var oldIntroduce: String

    val isChanged
        get() = uiState1.value.isImageChanged ||
                oldName != uiState1.value.name ||
                oldIntroduce != uiState1.value.introduce

    val canSave: Boolean
        get() = uiState1.value.name.isNotEmpty() && !uiState1.value.isLoading && isChanged

    fun bind(oldName: String, oldIntroduce: String) {
        check(!didBound)
        didBound = true
        this.oldName = oldName
        this.oldIntroduce = oldIntroduce
        _uiState1.update {
            it.copy(name = oldName, introduce = oldIntroduce)
        }
    }

    fun initUpdateInfo(userUuid: String) {
        viewModelScope.launch {
            val result = UserRepository.getUserDetail(userUuid)
            if (result.isSuccess) {
                _uiState2.update { it.copy(userDetail = result.getOrNull()!!) }
            }
        }
    }

    fun updateName(name: String) {
        _uiState1.update { it.copy(name = name) }
    }

    fun updateIntroduce(introduce: String) {
        _uiState1.update { it.copy(introduce = introduce) }
    }

    fun updateImageBitmap(bitmap: Bitmap?) {
        _uiState1.update { it.copy(selectedImageBitmap = bitmap, isImageChanged = true) }
    }

    fun sendChangedInfo() {
        if (!isChanged) {
            _uiState1.update { it.copy(successToSave = true) }
            return
        }
        _uiState1.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val uiStateValue = uiState1.value
            val result = UserRepository.updateInfo(
                name = uiStateValue.name,
                introduce = uiStateValue.introduce,
                profileImage = uiStateValue.selectedImageBitmap,
                isChangedImage = uiStateValue.isImageChanged
            )
            if (result.isSuccess) {
                _uiState1.update {
                    it.copy(successToSave = true, isLoading = false)
                }
            } else {
                _uiState1.update {
                    it.copy(
                        userMessage = result.exceptionOrNull()!!.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun userMessageShown() {
        _uiState1.update { it.copy(userMessage = null) }
    }
}