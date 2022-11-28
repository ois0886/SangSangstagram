package com.example.sangsangstagram.view.home.bookmark

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sangsangstagram.R
import com.example.sangsangstagram.databinding.FragmentBookmarkBinding
import com.example.sangsangstagram.view.RefreshStateContract
import com.example.sangsangstagram.view.home.BaseFragment
import com.example.sangsangstagram.view.home.post.*
import com.example.sangsangstagram.view.home.post.comment.CommentActivity
import com.example.sangsangstagram.view.home.post.postcreate.PostCreateActivity
import com.example.sangsangstagram.view.home.userpage.UserPageActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class BookmarkFragment : BaseFragment<FragmentBookmarkBinding>(
) {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBookmarkBinding
        get() = FragmentBookmarkBinding::inflate

    private val viewModel: BookMarkViewModel by activityViewModels()

    private var launcher: ActivityResultLauncher<Intent>? = null
    private val initBookMarkPagingData: PagingData<BookMarkItemUiState>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.bind(initBookMarkPagingData)

        val adapter = BookMarkAdapter(
            onClickLikeButton = ::onClickLikeButton,
            onClickUser = ::onClickUser,
            onClickDeleteButton = ::onClickDeleteButton,
            onClickEditButton = ::onClickEditButton,
            onClickCommentButton = ::onClickCommentButton
        )

        initRecyclerView(adapter)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    updateUi(it, adapter)
                }
            }
        }

        launcher = registerForActivityResult(RefreshStateContract()) {
            if (it != null) {
                adapter.refresh()
                it.message?.let { message -> showSnackBar(message) }
            }
        }
    }

    private fun initRecyclerView(adapter: BookMarkAdapter) = with(binding) {
        recyclerView.adapter = adapter.withLoadStateFooter(
            PagingLoadStateAdapter { adapter.retry() }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadState.setListeners(adapter, swipeRefreshLayout)
        adapter.registerObserverForScrollToTop(recyclerView, whenItemRangeMoved = true)
        adapter.refresh()
    }

    private fun updateUi(uiState: BookMarkUiState, adapter: BookMarkAdapter) {
        adapter.submitData(viewLifecycleOwner.lifecycle, uiState.pagingData)
        if (uiState.userMessage != null) {
            viewModel.userMessageShown()
            showSnackBar(getString(uiState.userMessage))
        }
    }

    private fun onClickUser(uiState: BookMarkItemUiState) {
        startProfileActivity(uiState.writerUuid)
        viewModel.userMessageShown()
    }

    private fun onClickLikeButton(uiState: BookMarkItemUiState) {
        viewModel.toggleLike(postUuid = uiState.uuid)
        viewModel.userMessageShown()
    }

    private fun onClickCommentButton(uiState: BookMarkItemUiState) {
        startCommentActivity(uiState)
    }

    private fun startCommentActivity(uiState: BookMarkItemUiState) {
        val intent = CommentActivity.getIntent(requireContext(), uiState.uuid)
        launcher?.launch(intent)
    }

    private fun startProfileActivity(userUuid: String) {
        val intent = UserPageActivity.getIntent(requireContext(), userUuid)
        launcher?.launch(intent)
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun onClickDeleteButton(uiState: BookMarkItemUiState) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(getString(R.string.delete_post))
            setMessage(R.string.delete_message)
            setNegativeButton(R.string.cancel) { _, _ -> }
            setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteSelectedPost(uiState)
            }
        }.show()
    }

    private fun onClickEditButton(uiState: BookMarkItemUiState) {
        val postContent = uiState.content
        val postImage = uiState.imageUrl
        val postUuid = uiState.uuid

        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(getString(R.string.delete_post))
            setMessage(R.string.edit_post)
            setNegativeButton(R.string.cancel) { _, _ -> }
            setPositiveButton(R.string.edit) { _, _ ->
                val intent = PostCreateActivity.getIntent(
                    requireContext(),
                    postContent = postContent,
                    postImage = postImage,
                    postUuid = postUuid
                )

                launcher?.launch(intent)
            }
        }.show()
    }
}
