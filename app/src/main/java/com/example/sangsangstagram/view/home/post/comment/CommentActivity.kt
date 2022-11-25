package com.example.sangsangstagram.view.home.post.comment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sangsangstagram.databinding.ActivityCommentBinding

class CommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}