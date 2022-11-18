package com.example.sangsangstagram.view.home.mypage

import androidx.paging.PagingData
import com.example.sangsangstagram.domain.model.UserDetail
/*
data class MyPagePostUiState(
    val pagingData: PagingData<PostItemUiState> = PagingData.empty()
)
*/


data class MyPageDetailUiState(
    val userDetail: UserDetail? = null,
    val isLoading: Boolean = true,
    val userMessage: String? = null
)