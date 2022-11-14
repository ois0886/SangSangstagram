package com.example.sangsangstagram.view.postcreate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sangsangstagram.databinding.ActivityPostCreateBinding

class PostCreateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPostCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}