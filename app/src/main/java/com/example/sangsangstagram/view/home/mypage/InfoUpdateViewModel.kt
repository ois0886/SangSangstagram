package com.example.sangsangstagram.view.home.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sangsangstagram.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InfoUpdateViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InfoUpdateUiState())
    val uiState: StateFlow<InfoUpdateUiState> = _uiState.asStateFlow()

}