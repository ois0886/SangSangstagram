package com.example.sangsangstagram.view.home.userpage.follwing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.sangsangstagram.databinding.ItemUserBinding

class FollowingAdapter(
    private val onClickUser: (UserItemUiState) -> Unit
) : PagingDataAdapter<UserItemUiState, FollowingViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemUserBinding.inflate(layoutInflater, parent, false)
        return FollowingViewHolder(
            binding,
            onClickUser = onClickUser
        )
    }

    override fun onBindViewHolder(holder: FollowingViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<UserItemUiState>() {
            override fun areItemsTheSame(
                oldItem: UserItemUiState,
                newItem: UserItemUiState
            ): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(
                oldItem: UserItemUiState,
                newItem: UserItemUiState
            ): Boolean {
                return oldItem == newItem
            }
        }
    }


}