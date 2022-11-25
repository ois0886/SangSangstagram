package com.example.sangsangstagram.view.home.userpage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sangsangstagram.databinding.FragmentUserPostBinding
import com.example.sangsangstagram.view.home.BaseFragment
import com.example.sangsangstagram.view.home.post.PostItemUiState

class UserPostFragment : BaseFragment<FragmentUserPostBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUserPostBinding
        get() = FragmentUserPostBinding::inflate

    private var launcher: ActivityResultLauncher<Intent>? = null
    private val initPostPagingData: PagingData<PostItemUiState>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun initGridRecyclerView(adapter: UserPagePostAdapter) = with(binding) {
        recyclerView.layoutManager = GridLayoutManager(activity, 3)


    }
}