package com.example.sangsangstagram.view.home.userpage.follwing

import androidx.recyclerview.widget.RecyclerView
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

        glide.load(uiState.profileImageUrl?.let { storageReference.child(it) })
            .fallback(R.drawable.ic_baseline_person_pin_24)
            .into(profileImage)

        name.text = uiState.name

        root.setOnClickListener {
            onClickUser(uiState)
        }
    }
}