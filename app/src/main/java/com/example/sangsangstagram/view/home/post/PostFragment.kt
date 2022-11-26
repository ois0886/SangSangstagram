package com.example.sangsangstagram.view.home.post

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
import com.example.sangsangstagram.databinding.FragmentPostBinding
import com.example.sangsangstagram.view.RefreshStateContract
import com.example.sangsangstagram.view.home.BaseFragment
import com.example.sangsangstagram.view.home.post.postcreate.PostCreateActivity
import com.example.sangsangstagram.view.home.userpage.UserPageActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PostFragment : BaseFragment<FragmentPostBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPostBinding
        get() = FragmentPostBinding::inflate

    private val viewModel: PostViewModel by activityViewModels()

    private var launcher: ActivityResultLauncher<Intent>? = null
    private val targetUserUuid: String? = null
    private val initPostPagingData: PagingData<PostItemUiState>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.bind(targetUserUuid, initPostPagingData)

        val adapter = PostAdapter(
            onClickLikeButton = ::onClickLikeButton,
            onClickUser = ::onClickUser,
            onClickDeleteButton = ::onClickDeleteButton,
            onClickEditButton = ::onClickEditButton
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

    private fun initRecyclerView(adapter: PostAdapter) = with(binding) {
        recyclerView.adapter = adapter.withLoadStateFooter(
            PagingLoadStateAdapter { adapter.retry() }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadState.setListeners(adapter, swipeRefreshLayout)
        adapter.registerObserverForScrollToTop(recyclerView, whenItemRangeMoved = true)
        adapter.refresh()
    }

    private fun updateUi(uiState: PostListUiState, adapter: PostAdapter) {
        adapter.submitData(viewLifecycleOwner.lifecycle, uiState.pagingData)
        if (uiState.userMessage != null) {
            viewModel.userMessageShown()
            showSnackBar(getString(uiState.userMessage))
        }
    }

    private fun onClickUser(uiState: PostItemUiState) {
        startProfileActivity(uiState.writerUuid)
        viewModel.userMessageShown()
    }

    private fun onClickLikeButton(uiState: PostItemUiState) {
        viewModel.toggleLike(postUuid = uiState.uuid)
        viewModel.userMessageShown()
    }

    private fun startProfileActivity(userUuid: String) {
        val intent = UserPageActivity.getIntent(requireContext(), userUuid)
        launcher?.launch(intent)
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun onClickDeleteButton(uiState: PostItemUiState) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(getString(R.string.delete_post))
            setMessage(R.string.delete_message)
            setNegativeButton(R.string.cancel) { _, _ -> }
            setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteSelectedPost(uiState)
            }
        }.show()
    }

    private fun onClickEditButton(uiState: PostItemUiState) {
        val postContent = uiState.content
        val postImage = uiState.imageUrl
        val postUuid = uiState.uuid

        val intent = PostCreateActivity.getIntent(
            requireContext(),
            postContent = postContent,
            postImage = postImage,
            postUuid = postUuid
        )

        launcher?.launch(intent)
    }
}
