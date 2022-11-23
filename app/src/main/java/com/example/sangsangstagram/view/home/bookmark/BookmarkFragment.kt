package com.example.sangsangstagram.view.home.bookmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sangsangstagram.databinding.FragmentBookmarkBinding
import com.example.sangsangstagram.view.home.BaseFragment


class BookmarkFragment : BaseFragment<FragmentBookmarkBinding>(
) {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBookmarkBinding
        get() = FragmentBookmarkBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
}