package com.example.sangsangstagram.view.home.post

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.ToggleButton
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sangsangstagram.R
import com.example.sangsangstagram.databinding.ItemPostBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class PostViewHolder(
    private val binding: ItemPostBinding,
    private val onClickUser: (PostItemUiState) -> Unit,
    private val onClickLikeButton: (PostItemUiState) -> Unit,
    private val onClickDeleteButton: (PostItemUiState) -> Unit,
    private val onClickEditButton: (PostItemUiState) -> Unit,
    private val onClickBookMarkButton: (PostItemUiState) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val storageReference = Firebase.storage.reference

    @SuppressLint("UseCompatLoadingForDrawables")
    fun bind(uiState: PostItemUiState) = with(binding) {
        val glide = Glide.with(root)

        val writerReference = uiState.writerProfileImageUrl?.let { storageReference.child(it) }
        val postReference = uiState.imageUrl.let { storageReference.child(it) }

        if (writerReference != null) {
            writerReference.downloadUrl.addOnSuccessListener { uri ->
                glide
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .fallback(R.drawable.ic_baseline_person_pin_24)
                    .circleCrop()
                    .into(profileImage)
            }
        } else {
            glide.load(uiState.writerProfileImageUrl?.let { storageReference.child(it) })
                .fallback(R.drawable.ic_baseline_person_pin_24)
                .into(profileImage)
        }

        userName.text = uiState.writerName
        userName.setOnClickListener {
            onClickUser(uiState)
        }
        profileImage.setOnClickListener {
            onClickUser(uiState)
        }

        editButton.isVisible = uiState.isMine
        editButton.setOnClickListener {
            onClickEditButton(uiState)
        }

        deleteButton.isVisible = uiState.isMine
        deleteButton.setOnClickListener {
            onClickDeleteButton(uiState)
        }

        postReference.downloadUrl.addOnSuccessListener { uri ->
            glide
                .load(uri)
                .into(postImage)
        }

        likeToggleButton.isChecked = uiState.meLiked
        if (likeToggleButton.isChecked) {
            likeToggleButton.setBackgroundDrawable(
                root.context.getDrawable(
                    R.drawable.ic_favorite
                )
            )
        } else {
            likeToggleButton.setBackgroundDrawable(
                root.context.getDrawable(
                    R.drawable.ic_favorite_border
                )
            )
        }
        likeToggleButton.setOnClickListener {
            val isChecked = (it as ToggleButton).isChecked
            if (isChecked) {
                likeToggleButton.setBackgroundDrawable(
                    root.context.getDrawable(
                        R.drawable.ic_favorite
                    )
                )
            } else {
                likeToggleButton.setBackgroundDrawable(
                    root.context.getDrawable(
                        R.drawable.ic_favorite_border
                    )
                )
            }
            val likeCountText = uiState.likeCount +
                    (if (uiState.meLiked) -1 else 0) +
                    (if (isChecked) 1 else 0)

            likeCount.text = root.context.getString(R.string.likeCount, likeCountText)
            onClickLikeButton(uiState)
        }

        BookmarkButton.isChecked = uiState.meLiked
        if (BookmarkButton.isChecked) {
            BookmarkButton.setBackgroundDrawable(
                root.context.getDrawable(
                    R.drawable.ic_baseline_bookmark_24
                )
            )
        } else {
            BookmarkButton.setBackgroundDrawable(
                root.context.getDrawable(
                    R.drawable.ic_baseline_bookmark_border_24
                )
            )
        }

        BookmarkButton.setOnClickListener {
            val isChecked = (it as ToggleButton).isChecked
            if (isChecked) {
                BookmarkButton.setBackgroundDrawable(
                    root.context.getDrawable(
                        R.drawable.ic_baseline_bookmark_24
                    )
                )
                Snackbar.make(binding.root, "북마크 등록!", Snackbar.LENGTH_LONG).show()
            } else {
                BookmarkButton.setBackgroundDrawable(
                    root.context.getDrawable(
                        R.drawable.ic_baseline_bookmark_border_24
                    )
                )
                Snackbar.make(binding.root, "북마크 해제!", Snackbar.LENGTH_LONG).show()
            }
            onClickBookMarkButton(uiState)
        }

        likeCount.text = root.context.getString(R.string.likeCount, uiState.likeCount)

        @SuppressLint("SetTextI18n")
        val spannable = SpannableString("${uiState.writerName} ${uiState.content}")
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            uiState.writerName.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        content.text = spannable
        content.isVisible = uiState.content.isNotEmpty()

        timeAgo.text = uiState.time
    }
}
