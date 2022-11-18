package com.example.sangsangstagram.view.home.mypage

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sangsangstagram.databinding.ActivityInfoUpdateBinding
import com.example.sangsangstagram.databinding.ActivityUserPageBinding

class InfoUpdateActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, InfoUpdateActivity::class.java)
        }
    }

    private lateinit var binding: ActivityInfoUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}