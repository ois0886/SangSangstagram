package com.example.sangsangstagram.view.home.bookmark

import androidx.annotation.StringRes
import androidx.paging.PagingData
import com.example.sangsangstagram.domain.model.Post


data class BookMarkUiState(
    val pagingData: PagingData<BookMarkItemUiState> = PagingData.empty(),
    val selectedPostItem: BookMarkItemUiState? = null,
    val currentUserUuid: String,
    @StringRes
    val userMessage: Int? = null
)

data class BookMarkItemUiState(
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

fun Post.toUiState() = BookMarkItemUiState(
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