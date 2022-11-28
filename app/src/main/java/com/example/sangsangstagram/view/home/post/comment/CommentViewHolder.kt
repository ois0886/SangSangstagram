package com.example.sangsangstagram.view.home.post.comment

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sangsangstagram.R
import com.example.sangsangstagram.databinding.ItemCommentBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class CommentViewHolder(
    private val binding: ItemCommentBinding,
    private val onClickUser: (CommentItemUiState) -> Unit,
    private val onClickDeleteButton: (CommentItemUiState) -> Unit,
    private val onClickEditButton: (CommentItemUiState) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val storageReference = Firebase.storage.reference

    @SuppressLint("UseCompatLoadingForDrawables")
    fun bind(uiState: CommentItemUiState) = with(binding) {
        val glide = Glide.with(root)

        val writerReference = uiState.writerProfileImageUrl?.let { storageReference.child(it) }

        if (writerReference != null) {
            writerReference.downloadUrl.addOnSuccessListener { uri ->
                glide
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .fallback(R.drawable.ic_baseline_person_pin_24)
                    .circleCrop()
                    .into(commentProfile)
            }
        } else {
            glide.load(uiState.writerProfileImageUrl?.let { storageReference.child(it) })
                .fallback(R.drawable.ic_baseline_person_pin_24)
                .into(commentProfile)
        }

        commentUserName.text = uiState.writerName
        commentText.text = uiState.content
        commentUserName.setOnClickListener {
            onClickUser(uiState)
        }
        commentProfile.setOnClickListener {
            onClickUser(uiState)
        }

        deleteButton.setOnClickListener {
            onClickDeleteButton
        }

        editButton.setOnClickListener {
            onClickEditButton
        }
    }
}
