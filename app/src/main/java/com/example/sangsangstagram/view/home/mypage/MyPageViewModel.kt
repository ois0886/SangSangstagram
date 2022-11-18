package com.example.sangsangstagram.view.home.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.sangsangstagram.data.PostRepository
import com.example.sangsangstagram.data.UserRepository
import com.example.sangsangstagram.domain.model.UserDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyPageViewModel : ViewModel() {
    fun bindProfile(targetUuid: String) {
        //loadPostByUser(targetUuid)
        getProfileDetail(targetUuid)
    }

    /* Post Ui State 구역
    private val _myPagePostUiState = MutableStateFlow(MyPagePostUiState())
    val myPagePostUiState = _myPagePostUiState.asStateFlow()

    private fun loadPostByUser(targetUuid: String) {
        viewModelScope.launch {
            PostRepository.getPostDetailsByUser(targetUuid).cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    _myPagePostUiState.update { myPagePostUiState ->
                        myPagePostUiState.copy(pagingData = pagingData.map { it.toUiState() })
                    }
                }
        }
    }
    */


    /* UserDetail Ui State 구역 */
    private val _myPageDetailUiState: MutableStateFlow<MyPageDetailUiState> =
        MutableStateFlow(MyPageDetailUiState())
    val myPageDetailUiState = _myPageDetailUiState.asStateFlow()

    fun getProfileDetail(targetUuid: String) {
        _myPageDetailUiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            UserRepository.getUserDetail(targetUuid)
                .onSuccess { userDetails ->
                    _myPageDetailUiState.update {
                        it.copy(userDetail = userDetails, isLoading = false)
                    }
                }
                .onFailure { e ->
                    _myPageDetailUiState.update {
                        it.copy(userMessage = e.localizedMessage, isLoading = false)
                    }
                }
        }
    }

    fun toggleFollow() {
        val userDetail = _myPageDetailUiState.value.userDetail
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
            _myPageDetailUiState.update {
                it.copy(
                    userDetail = userDetail.copy(
                        followersCount = userDetail.followersCount + 1,
                        isCurrentUserFollowing = true
                    )
                )
            }
        } else {
            _myPageDetailUiState.update {
                it.copy(userMessage = result.exceptionOrNull()!!.message)
            }
        }
    }

    private suspend fun unfollow(userDetail: UserDetail) {
        val result = UserRepository.unfollow(userDetail.uuid)
        if (result.isSuccess) {
            _myPageDetailUiState.update {
                it.copy(
                    userDetail = userDetail.copy(
                        followersCount = userDetail.followersCount - 1,
                        isCurrentUserFollowing = false
                    )
                )
            }
        } else {
            _myPageDetailUiState.update {
                it.copy(userMessage = result.exceptionOrNull()!!.message)
            }
        }
    }

    fun userMessageShown() {
        _myPageDetailUiState.update {
            it.copy(userMessage = null)
        }
    }
}
