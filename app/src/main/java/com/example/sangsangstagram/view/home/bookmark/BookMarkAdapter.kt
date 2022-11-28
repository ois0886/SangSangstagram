package com.example.sangsangstagram.view.home.bookmark

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.sangsangstagram.databinding.ItemPostBinding

class BookMarkAdapter(
    private val onClickUser: (BookMarkItemUiState) -> Unit,
    private val onClickLikeButton: (BookMarkItemUiState) -> Unit,
    private val onClickDeleteButton: (BookMarkItemUiState) -> Unit,
    private val onClickEditButton: (BookMarkItemUiState) -> Unit,
    private val onClickCommentButton: (BookMarkItemUiState) -> Unit
) : PagingDataAdapter<BookMarkItemUiState, BookMarkVIewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookMarkVIewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPostBinding.inflate(layoutInflater, parent, false)
        return BookMarkVIewHolder(
            binding,
            onClickLikeButton = onClickLikeButton,
            onClickUser = onClickUser,
            onClickDeleteButton = onClickDeleteButton,
            onClickEditButton = onClickEditButton,
            onClickCommentButton = onClickCommentButton
        )
    }

    override fun onBindViewHolder(holder: BookMarkVIewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<BookMarkItemUiState>() {
            override fun areItemsTheSame(
                oldItem: BookMarkItemUiState,
                newItem: BookMarkItemUiState
            ): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(
                oldItem: BookMarkItemUiState,
                newItem: BookMarkItemUiState
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
