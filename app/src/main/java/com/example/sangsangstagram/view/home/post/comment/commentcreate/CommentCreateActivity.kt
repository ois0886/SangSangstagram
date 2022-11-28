package com.example.sangsangstagram.view.home.post.comment.commentcreate

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sangsangstagram.R
import com.example.sangsangstagram.databinding.ActivityCommentCreateBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CommentCreateActivity : AppCompatActivity() {
    lateinit var binding: ActivityCommentCreateBinding

    private val viewModel: CommentCreateViewModel by viewModels()

    companion object {
        fun getIntent(
            context: Context,
            postUuid: String
        ): Intent {
            return Intent(context, CommentCreateActivity::class.java)
                .putExtra("postUuid", postUuid)
        }


        fun getIntent(
            context: Context,
            Content: String,
            commentUuid: String,
            postUuid: String
        ): Intent {
            return Intent(context, CommentCreateActivity::class.java)
                .putExtra("content", Content)
                .putExtra("commentUuid", commentUuid)
                .putExtra("postUuid", postUuid)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val content = intent.getStringExtra("content")
        val commentUuid = intent.getStringExtra("commentUuid")
        val postUuid = intent.getStringExtra("postUuid")!!


        if (commentUuid != null && content != null) {
            viewModel.changeToEditMode()

            binding.appTitle.text = getString(R.string.post_edit)
            binding.postButton.text = getString(R.string.post_editting)
            binding.contentExpression.setText(content)
        }

        binding.postButton.setOnClickListener {
            if (!viewModel.uiState.value.isCreating) {
                viewModel.editContent(
                    commentUuid.toString(),
                    binding.contentExpression.text.toString()
                )
            } else {
                viewModel.uploadContent(binding.contentExpression.text.toString(), postUuid)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::updateUi)
            }
        }
    }

    private fun updateUi(uiState: CommentCreateUiState) {

        if (uiState.userMessage != null) {
            showSnackBar(getString(uiState.userMessage))
            viewModel.userMessageShown()
        }
        if (uiState.successToUpload) {
            Toast.makeText(this, "댓글 업로드에 성공했습니다.", Toast.LENGTH_LONG).show()
            setResult(RESULT_OK)
            finish()
        }

        binding.postButton.apply {
            isEnabled = !uiState.isLoading
            alpha = if (uiState.isLoading) 0.5F else 1.0F
        }
    }


    private fun showSnackBar(message: String) {
        val root = binding.root
        Snackbar.make(root, message, Snackbar.LENGTH_LONG).show()
    }
}