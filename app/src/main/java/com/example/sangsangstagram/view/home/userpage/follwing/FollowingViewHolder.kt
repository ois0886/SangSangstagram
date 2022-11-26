package com.example.sangsangstagram.view.home.userpage.follwing

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sangsangstagram.R
import com.example.sangsangstagram.databinding.ItemUserBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class FollowingViewHolder(
    private val binding: ItemUserBinding,
    private val onClickUser: (UserItemUiState) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val storageReference = Firebase.storage.reference

    fun bind(uiState: UserItemUiState) = with(binding) {
        val glide = com.bumptech.glide.Glide.with(root)
        val reference = uiState.profileImageUrl?.let { storageReference.child(it) }

        reference?.downloadUrl?.addOnSuccessListener { uri ->
            glide
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .fallback(R.drawable.ic_baseline_person_pin_24)
                .circleCrop()
                .into(profileImage)
        }

        name.text = uiState.name
        introduce.text = uiState.introduce

        root.setOnClickListener {
            onClickUser(uiState)
        }
    }
}