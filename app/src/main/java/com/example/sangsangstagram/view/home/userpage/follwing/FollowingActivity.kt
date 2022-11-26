package com.example.sangsangstagram.view.home.userpage.follwing

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sangsangstagram.databinding.ActivityFollowingBinding
import com.example.sangsangstagram.view.home.post.PagingLoadStateAdapter
import com.example.sangsangstagram.view.home.userpage.UserPageActivity

class FollowingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFollowingBinding
    companion object{
        fun getIntent(context: Context, userUuid: String): Intent {
            return Intent(context, FollowingActivity::class.java).apply {
                putExtra("userUuid", userUuid)
                putExtra("type", type)
            }
        }
    }

    private val viewModel: FollowingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initRecyclerView(adapter: FollowingAdapter) {
        binding.apply {
            recyclerView.adapter = adapter.withLoadStateFooter(
                PagingLoadStateAdapter { adapter.retry() }
            )
            recyclerView.layoutManager = LinearLayoutManager(this@FollowingActivity)
        }
    }

    private fun onClickUser(uiState: UserItemUiState) {
        val intent = UserPageActivity.getIntent(this, userUuid = uiState.uuid)
        startActivity(intent)
    }

    private fun updateUi(uiState: UserListUiState, adapter: FollowingAdapter) {
        adapter.submitData(lifecycle, uiState.pagingData)
    }
}