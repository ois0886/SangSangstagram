package com.example.sangsangstagram.view.home.userpage

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sangsangstagram.R
import com.example.sangsangstagram.databinding.ActivityUserPageBinding
import com.example.sangsangstagram.domain.model.UserDetail
import com.example.sangsangstagram.view.home.HomeActivity
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class UserPageActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context, userUuid: String): Intent {
            return Intent(context, UserPageActivity::class.java).apply {
                putExtra("userUuid", userUuid)
            }
        }
    }

    private fun getUserUuid(): String {
        return intent.getStringExtra("userUuid")!!
    }

    private val viewModel: UserPageViewModel by viewModels()
    private lateinit var binding: ActivityUserPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.profileUpdate(getUserUuid())

        binding.accountProfileButton.setOnClickListener {
            val isMe = viewModel.userPageUiState.value.userDetail!!.isMe
            if (isMe) {
                startInfoUpdateUi(viewModel.userPageUiState.value.userDetail!!)
            } else {
                viewModel.toggleFollow()
                setResult(RESULT_OK)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userPageUiState.collect(::updateUi)
            }
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.backButton.setOnClickListener {
            val intent = HomeActivity.getIntent(this)
            startActivity(intent)
            finish()
        }
    }

    private fun updateUi(uiState: UserPageUiState) {
        val storage: FirebaseStorage =
            FirebaseStorage.getInstance("gs://sangsangstagram.appspot.com/")
        val storageReference = storage.reference
        val userDetail = uiState.userDetail
        val pathReference = userDetail?.profileImageUrl?.let { storageReference.child(it) }

        if (userDetail != null) {
            binding.apply {
                pathReference?.downloadUrl?.addOnSuccessListener { uri ->
                    Glide.with(this@UserPageActivity)
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .fallback(R.drawable.ic_baseline_person_pin_24)
                        .circleCrop()
                        .into(accountProfileImageView)
                }
                accountProfileButton.text = getString(R.string.update)
                accountName.text = userDetail.name
                accountIntroduce.text = userDetail.introduce
                accountPostCount.text = userDetail.postCount.toString()
                accountFollowerCount.text = userDetail.followersCount.toString()
                accountFollowingCount.text = userDetail.followingCount.toString()

            }
        }
        viewModel.userMessageShown()
    }

    private fun startInfoUpdateUi(userDetail: UserDetail) {
        val intent = InfoUpdateActivity.getIntent(this, userDetail)
        startActivity(intent)
        finish()
    }
}
