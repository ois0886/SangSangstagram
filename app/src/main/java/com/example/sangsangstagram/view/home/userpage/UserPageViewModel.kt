package com.example.sangsangstagram.view.home.userpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.sangsangstagram.data.PostRepository
import com.example.sangsangstagram.data.UserRepository
import com.example.sangsangstagram.domain.model.UserDetail
import com.example.sangsangstagram.view.home.post.toUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UserPageViewModel() : ViewModel() {

    fun bindProfile(targetUuid: String) {
        loadPostByUser(targetUuid)
        profileUpdate(targetUuid)
    }

    private val _userPageUiState = MutableStateFlow(UserPageUiState())
    val userPageUiState: StateFlow<UserPageUiState> = _userPageUiState.asStateFlow()

    private fun profileUpdate(userUuid: String) {
        viewModelScope.launch {
            val result = UserRepository.getUserDetail(userUuid)
            if (result.isSuccess) {
                _userPageUiState.update { it.copy(userDetail = result.getOrNull()!!) }
                PostRepository.getPostDetailsByUser(userUuid).cachedIn(viewModelScope)
                    .collectLatest { pagingData ->
                        _userPagePostUiState.update { userPagePostUiState ->
                            userPagePostUiState.copy(pagingData = pagingData.map { it.toUiState() })
                        }
                    }
            }
        }
    }

    private val _userPagePostUiState = MutableStateFlow(UserPagePostUiState())
    val userPagePostUiState = _userPagePostUiState.asStateFlow()

    private fun loadPostByUser(targetUuid: String) {
        viewModelScope.launch {
            PostRepository.getPostDetailsByUser(targetUuid).cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    _userPagePostUiState.update { userPagePostUiState ->
                        userPagePostUiState.copy(pagingData = pagingData.map { it.toUiState() })
                    }
                }
        }
    }

    fun toggleFollow() {
        val userDetail = _userPageUiState.value.userDetail
        check(userDetail != null)
        viewModelScope.launch {
            if (userDetail.isCurrentUserFollowing) {
                unfollow(userDetail)
            } else {
                follow(userDetail)
            }
        }
    }

    private suspend fun follow(userDetail: UserDetail) {
        val result = UserRepository.follow(userDetail.uuid)
        if (result.isSuccess) {
            _userPageUiState.update {
                it.copy(
                    userDetail = userDetail.copy(
                        followersCount = userDetail.followersCount + 1,
                        isCurrentUserFollowing = true
                    )
                )
            }
        } else {
            _userPageUiState.update {
                it.copy(userMessage = result.exceptionOrNull()!!.message)
            }
        }
    }

    private suspend fun unfollow(userDetail: UserDetail) {
        val result = UserRepository.unfollow(userDetail.uuid)
        if (result.isSuccess) {
            _userPageUiState.update {
                it.copy(
                    userDetail = userDetail.copy(
                        followersCount = userDetail.followersCount - 1,
                        isCurrentUserFollowing = false
                    )
                )
            }
        } else {
            _userPageUiState.update {
                it.copy(userMessage = result.exceptionOrNull()!!.message)
            }
        }
    }

    fun userMessageShown() {
        _userPageUiState.update {
            it.copy(userMessage = null)
        }
    }
}