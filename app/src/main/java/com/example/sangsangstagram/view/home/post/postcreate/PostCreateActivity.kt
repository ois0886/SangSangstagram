package com.example.sangsangstagram.view.home.post.postcreate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sangsangstagram.databinding.ActivityPostCreateBinding

class PostCreateActivity : AppCompatActivity() {
    lateinit var binding: ActivityPostCreateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.postBackButton.setOnClickListener {
            finish()
        }
    }
}