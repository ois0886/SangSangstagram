package com.example.sangsangstagram.view.home.post

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sangsangstagram.R
import com.example.sangsangstagram.databinding.FragmentPostBinding
import com.example.sangsangstagram.databinding.ItemLoadStateBinding
import com.example.sangsangstagram.view.home.BaseFragment
import com.example.sangsangstagram.view.home.mypage.UserPageActivity
import com.example.sangsangstagram.view.home.post.postcreate.PostCreateActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostFragment(
    private val targetUserUuid: String? = null,
    private val initPostPagingData: PagingData<PostItemUiState>? = null,
) : BaseFragment<FragmentPostBinding>() {

    private val viewModel: PostViewModel by viewModels()

    private lateinit var launcher: ActivityResultLauncher<Intent>

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPostBinding
        get() = FragmentPostBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.bind(targetUserUuid, initPostPagingData)

        val adapter = PostAdapter(
            onClickLikeButton = ::onClickLikeButton,
            onClickUser = ::onClickUser
        )
        initRecyclerView(adapter)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                adapter.refresh()
            }
        }
        setFragmentResultListener("refreshPosts") { _, _ ->
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                delay(300)
                withContext(Dispatchers.Main) {
                    adapter.refresh()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    updateUi(it, adapter)
                }
            }
        }
    }

    private fun initRecyclerView(adapter: PostAdapter) {
        binding.apply {
            recyclerView.adapter = adapter.withLoadStateFooter(
                PagingLoadStateAdapter { adapter.retry() }
            )
            recyclerView.layoutManager = LinearLayoutManager(context)

        }
    }

    private fun updateUi(uiState: PostListUiState, adapter: PostAdapter) {
        adapter.submitData(viewLifecycleOwner.lifecycle, uiState.pagingData)
        if (uiState.userMessage != null) {
            showSnackBar(getString(uiState.userMessage))
            viewModel.userMessageShown()
        }
    }

    private fun onClickLikeButton(uiState: PostItemUiState) {
        viewModel.toggleLike(postUuid = uiState.uuid)
    }

    private fun onClickUser(uiState: PostItemUiState) {
        startProfileActivity(uiState.writerUuid)
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun startProfileActivity(userUuid: String) {
        val intent = UserPageActivity.getIntent(requireContext(), userUuid)
        launcher.launch(intent)
    }

    inner class PagingLoadStateAdapter(
        private val retry: () -> Unit,
    ) : LoadStateAdapter<PagingLoadStateViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            loadState: LoadState
        ): PagingLoadStateViewHolder = PagingLoadStateViewHolder(parent, retry)

        override fun onBindViewHolder(holder: PagingLoadStateViewHolder, loadState: LoadState) {
            holder.bind(loadState)
        }
    }

    inner class PagingLoadStateViewHolder(
        parent: ViewGroup,
        retry: () -> Unit
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_load_state, parent, false)
    ) {
        private val binding = ItemLoadStateBinding.bind(itemView)
        private val progressBar: ProgressBar = binding.progressBar
        private val errorMsg: TextView = binding.errorMsg
        private val retry: Button = binding.retryButton.also {
            it.setOnClickListener { retry() }
        }

        fun bind(loadState: LoadState) {
            progressBar.isVisible = loadState is LoadState.Loading
            retry.isVisible = loadState is LoadState.Error
            errorMsg.isVisible = loadState is LoadState.Error
        }
    }
}