package com.example.sangsangstagram.view.home.post.comment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.sangsangstagram.databinding.ItemCommentBinding

class CommentAdapter(
    private val onClickUser: (CommentItemUiState) -> Unit,
    private val onClickDeleteButton: (CommentItemUiState) -> Unit,
    private val onClickEditButton: (CommentItemUiState) -> Unit,
) : PagingDataAdapter<CommentItemUiState, CommentViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCommentBinding.inflate(layoutInflater, parent, false)
        return CommentViewHolder(
            binding,
            onClickUser = onClickUser,
            onClickDeleteButton = onClickDeleteButton,
            onClickEditButton = onClickEditButton
        )
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<CommentItemUiState>() {
            override fun areItemsTheSame(
                oldItem: CommentItemUiState,
                newItem: CommentItemUiState
            ): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(
                oldItem: CommentItemUiState,
                newItem: CommentItemUiState
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
