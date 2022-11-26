package com.example.sangsangstagram.view.home.userpage.follwing

import androidx.paging.PagingData
import com.example.sangsangstagram.data.model.UserDto

enum class UserListPageType : java.io.Serializable {
    FOLLOWING,
    FOLLOWER
}

data class FollowingUiState(
    val pagingData: PagingData<UserItemUiState> = PagingData.empty()
)

data class UserItemUiState(
    val uuid: String,
    val name: String,
    val profileImageUrl: String?,
    val introduce: String = ""
)

fun UserDto.toUiState() = UserItemUiState(
    uuid = uuid,
    name = name,
    profileImageUrl = profileImageUrl,
    introduce = introduce
)