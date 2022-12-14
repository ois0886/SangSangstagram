package com.example.sangsangstagram.view.home.post

import androidx.annotation.StringRes
import androidx.paging.PagingData
import com.example.sangsangstagram.domain.model.Post


data class PostListUiState(
    val pagingData: PagingData<PostItemUiState> = PagingData.empty(),
    val selectedPostItem: PostItemUiState? = null,
    val currentUserUuid: String,
    @StringRes
    val userMessage: Int? = null
)

data class PostItemUiState(
    val uuid: String,
    val writerUuid: String,
    val writerName: String,
    val writerProfileImageUrl: String?,
    val content: String,
    val imageUrl: String,
    val likeCount: Int,
    val meLiked: Boolean,
    val isMine: Boolean,
    val bookMarkChecked: Boolean,
    val time: String
)

fun Post.toUiState() = PostItemUiState(
    uuid = uuid,
    writerUuid = writerUuid,
    writerName = writerName,
    writerProfileImageUrl = writerProfileImageUrl,
    content = content,
    imageUrl = imageUrl,
    likeCount = likeCount,
    meLiked = meLiked,
    isMine = isMine,
    bookMarkChecked = bookMarkChecked,
    time = time
)
