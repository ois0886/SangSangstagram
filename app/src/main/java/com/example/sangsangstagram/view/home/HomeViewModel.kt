package com.example.sangsangstagram.view.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sangsangstagram.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun updateCurrentUserProfileImage(userUuid: String) {
        viewModelScope.launch {
            val result = UserRepository.getUserDetail(userUuid)
            if (result.isSuccess) {
                _uiState.update { it.copy(userDetail = result.getOrNull()!!) }
            }
        }
    }
}