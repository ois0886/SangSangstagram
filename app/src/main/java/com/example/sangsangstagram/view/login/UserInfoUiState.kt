package com.example.sangsangstagram.view.login

sealed class UserInfoUiState {
    object None : UserInfoUiState()
    object Loading : UserInfoUiState()
    object SuccessToSave : UserInfoUiState()
    data class FailedToSave(val exception: Throwable) :UserInfoUiState()
}
