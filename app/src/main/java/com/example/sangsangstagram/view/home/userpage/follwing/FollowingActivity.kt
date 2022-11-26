package com.example.sangsangstagram.view.home.userpage.follwing

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sangsangstagram.databinding.ActivityFollowingBinding
import com.example.sangsangstagram.view.home.post.PagingLoadStateAdapter
import com.example.sangsangstagram.view.home.post.registerObserverForScrollToTop
import com.example.sangsangstagram.view.home.post.setListeners
import com.example.sangsangstagram.view.home.userpage.UserPageActivity
import kotlinx.coroutines.launch
import java.io.Serializable

class FollowingActivity : AppCompatActivity() {

    private fun <T : Serializable?> Intent.getSerializable(key: String, m_class: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            this.getSerializableExtra(key, m_class)!!
        else
            this.getSerializableExtra(key) as T
    }

    private lateinit var binding: ActivityFollowingBinding

    companion object {
        fun getIntent(context: Context, userUuid: String, type: UserListPageType): Intent {
            return Intent(context, FollowingActivity::class.java).apply {
                putExtra("userUuid", userUuid)
                putExtra("type", type)
            }
        }
    }

    private val viewModel: FollowingViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userUuid = requireNotNull(intent.getStringExtra("userUuid"))
        val type = intent.getSerializable("type", UserListPageType::class.java)
        viewModel.bind(userUuid, type)

        val adapter = FollowingAdapter(onClickUser = ::onClickUser)
        initRecyclerView(adapter)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    updateUi(it, adapter)
                }
            }
        }
    }

    private fun initRecyclerView(adapter: FollowingAdapter) {
        binding.apply {
            recyclerView.adapter = adapter.withLoadStateFooter(
                PagingLoadStateAdapter { adapter.retry() }
            )
            recyclerView.layoutManager = LinearLayoutManager(this@FollowingActivity)

            loadState.setListeners(adapter, swipeRefreshLayout)
            adapter.registerObserverForScrollToTop(recyclerView, whenItemRangeMoved = true)
            adapter.refresh()
        }
    }

    private fun onClickUser(uiState: UserItemUiState) {
        val intent = UserPageActivity.getIntent(this, userUuid = uiState.uuid)
        finish()
        startActivity(intent)

    }

    private fun updateUi(uiState: FollowingUiState, adapter: FollowingAdapter) {
        adapter.submitData(lifecycle, uiState.pagingData)
    }
}