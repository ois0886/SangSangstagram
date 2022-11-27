package com.example.sangsangstagram.view.home.userpage

import androidx.paging.PagingData
import com.example.sangsangstagram.domain.model.UserDetail
import com.example.sangsangstagram.view.home.post.PostItemUiState

data class UserPageUiState(
    val userDetail: UserDetail? = null,
    val isLoading: Boolean = true,
    val userMessage: String? = null,
    val pagingData: PagingData<PostItemUiState> = PagingData.empty()
)

data class UserPagePostUiState(
    val pagingData: PagingData<PostItemUiState> = PagingData.empty()
)