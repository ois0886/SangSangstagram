package com.example.sangsangstagram.view.home.userpage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.sangsangstagram.databinding.ItemUserPostBinding
import com.example.sangsangstagram.view.home.post.PostItemUiState

class UserPagePostAdapter(
    private val onClickPost: (PostItemUiState) -> Unit
) : PagingDataAdapter<PostItemUiState, UserPagePostViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPagePostViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemUserPostBinding.inflate(layoutInflater, parent, false)
        return UserPagePostViewHolder(
            binding,
            onClickPost = onClickPost
        )
    }

    override fun onBindViewHolder(holder: UserPagePostViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<PostItemUiState>() {
            override fun areItemsTheSame(
                oldItem: PostItemUiState,
                newItem: PostItemUiState
            ): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(
                oldItem: PostItemUiState,
                newItem: PostItemUiState
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}