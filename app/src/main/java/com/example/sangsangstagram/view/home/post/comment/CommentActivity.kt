package com.example.sangsangstagram.view.home.post.comment

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sangsangstagram.R
import com.example.sangsangstagram.databinding.ActivityCommentBinding
import com.example.sangsangstagram.view.home.post.*
import com.example.sangsangstagram.view.home.post.comment.commentcreate.CommentCreateActivity
import com.example.sangsangstagram.view.home.post.postcreate.PostCreateActivity
import com.example.sangsangstagram.view.home.userpage.UserPageActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch


class CommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentBinding

    companion object {
        fun getIntent(context: Context, postUuid: String): Intent {
            return Intent(context, CommentActivity::class.java).apply {
                putExtra("postUuid", postUuid)
            }
        }
    }

    private fun getPostUuid(): String {
        return intent.getStringExtra("postUuid")!!
    }


    private val viewModel: CommentViewModel by viewModels()
    private val initCommentPagingData: PagingData<CommentItemUiState>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.bind(initCommentPagingData, getPostUuid())

        val adapter = CommentAdapter(
            onClickUser = ::onClickUser,
            onClickDeleteButton = ::onClickDeleteButton,
            onClickEditButton = ::onClickEditButton,
        )

        binding.backButton.setOnClickListener {
            finish()
        }

        println(getPostUuid())

        binding.createButton.setOnClickListener {
            val intent = CommentCreateActivity.getIntent(
                this,
                postUuid = getPostUuid()
            )
            startActivity(intent)
        }

        initRecyclerView(adapter)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    updateUi(it, adapter)
                }
            }
        }
    }

    private fun initRecyclerView(adapter: CommentAdapter) = with(binding) {
        recyclerView.adapter = adapter.withLoadStateFooter(
            PagingLoadStateAdapter { adapter.retry() }
        )
        recyclerView.layoutManager = LinearLayoutManager(this@CommentActivity)

        loadState.setListeners(adapter, swipeRefreshLayout)
        adapter.registerObserverForScrollToTop(recyclerView, whenItemRangeMoved = true)
        adapter.refresh()
    }

    private fun updateUi(uiState: CommentListUiState, adapter: CommentAdapter) {
        adapter.submitData(lifecycle, uiState.pagingData)
    }


    private fun onClickUser(uiState: CommentItemUiState) {
        startProfileActivity(uiState.writerUuid)
        viewModel.userMessageShown()
    }

    private fun startProfileActivity(userUuid: String) {
        val intent = UserPageActivity.getIntent(this, userUuid)
        startActivity(intent)
    }

    private fun onClickDeleteButton(uiState: CommentItemUiState) {
        MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.delete_post))
            setMessage(R.string.delete_message)
            setNegativeButton(R.string.cancel) { _, _ -> }
            setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteSelectedComment(uiState)
            }
        }.show()
    }

    private fun onClickEditButton(uiState: CommentItemUiState) {
        val commentContent = uiState.content
        val commentUuid = uiState.uuid

    }
}