package com.example.sangsangstagram.view.home.post.comment

import androidx.annotation.StringRes
import androidx.paging.PagingData
import com.example.sangsangstagram.domain.model.Comment

data class CommentListUiState(
    val pagingData: PagingData<CommentItemUiState> = PagingData.empty(),
    val selectedPostItem: CommentItemUiState? = null,
    val currentUserUuid: String,
    @StringRes
    val userMessage: Int? = null
)

data class CommentItemUiState(
    val uuid: String,
    val writerUuid: String,
    val writerName: String,
    val writerProfileImageUrl: String?,
    val content: String,
    val isMine: Boolean
)

fun Comment.toUiState() = CommentItemUiState(
    uuid = uuid,
    writerUuid = writerUuid,
    writerName = writerName,
    writerProfileImageUrl = writerProfileImageUrl,
    content = content,
    isMine = isMine
)
