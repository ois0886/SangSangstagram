package com.example.sangsangstagram.view.home.mypage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.sangsangstagram.databinding.FragmentMyPageBinding
import com.example.sangsangstagram.view.home.BaseFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MyPageFragment() : BaseFragment<FragmentMyPageBinding>(FragmentMyPageBinding::inflate) {

    private val viewModel: MyPageViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.bindProfile(getUserUuid())

        binding.accountName.text

    }

    private fun getUserUuid(): String {
        return Firebase.auth.currentUser?.uid.toString()
    }
}