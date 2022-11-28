package com.example.sangsangstagram.view.home.post.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.sangsangstagram.R
import com.example.sangsangstagram.data.AuthRepository
import com.example.sangsangstagram.data.CommentRepository
import com.example.sangsangstagram.data.PostRepository
import com.example.sangsangstagram.view.home.post.PostItemUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        CommentListUiState(currentUserUuid = requireNotNull(AuthRepository.currentUserUuid))
    )
    val uiState = _uiState.asStateFlow()

    private var bounded = false

    fun bind(
        initCommentPagingData: PagingData<CommentItemUiState>?,
        postUUid: String
    ) {
        if (bounded) return
        bounded = true
        if (initCommentPagingData != null) {
            _uiState.update { it.copy(pagingData = initCommentPagingData) }
        }
        viewModelScope.launch(Dispatchers.IO) {
            val pagingFlow = CommentRepository.getComment(postUUid)
            pagingFlow.cachedIn(viewModelScope)
                .collect { pagingData ->
                    _uiState.update { uiState ->
                        uiState.copy(pagingData = pagingData.map { it.toUiState() })
                    }
                }
        }
    }

    fun deleteSelectedComment(uiState: CommentItemUiState) {
        viewModelScope.launch {
            check(true)
            val result = CommentRepository.deleteComment(uiState.uuid)
            _uiState.update {
                it.copy(
                    userMessage = if (result.isSuccess) {
                        R.string.post_deleted
                    } else {
                        R.string.failed
                    }
                )
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
