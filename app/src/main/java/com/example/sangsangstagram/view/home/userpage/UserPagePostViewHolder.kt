package com.example.sangsangstagram.view.home.userpage

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sangsangstagram.databinding.ItemUserPostBinding
import com.example.sangsangstagram.view.home.post.PostItemUiState
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class UserPagePostViewHolder(
    private val binding: ItemUserPostBinding,
    private val onClickPost: (PostItemUiState) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val storageReference = Firebase.storage.reference

    fun bind(uiState: PostItemUiState) = with(binding) {
        val glide = Glide.with(root)

        glide.load(storageReference.child(uiState.imageUrl))
            .override(200, 200)
            .into(profilePostImage)

        root.setOnClickListener {
            onClickPost(uiState)
        }
    }
}