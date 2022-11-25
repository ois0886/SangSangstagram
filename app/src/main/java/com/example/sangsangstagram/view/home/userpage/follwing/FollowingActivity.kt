package com.example.sangsangstagram.view.home.userpage.follwing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sangsangstagram.databinding.ActivityFollowingBinding

class FollowingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFollowingBinding
    companion object{

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}