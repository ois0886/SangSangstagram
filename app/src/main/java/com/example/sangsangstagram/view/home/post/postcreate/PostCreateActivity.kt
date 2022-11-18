package com.example.sangsangstagram.view.home.post.postcreate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sangsangstagram.data.model.PostDto
import com.example.sangsangstagram.databinding.ActivityPostCreateBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class PostCreateActivity : AppCompatActivity() {
    lateinit var binding: ActivityPostCreateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.postBackButton.setOnClickListener {
            finish()
        }

        binding.postButton.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date())
            val postContent = binding.imageExpression.text.toString()
            val uuid = Firebase.auth.currentUser?.uid.toString()
            val postId = Firebase.auth.currentUser?.uid.toString()
            val imageUrl = binding.addImage.toString()

            val post = PostDto(uuid, postContent, imageUrl, timestamp)

            val db = FirebaseFirestore.getInstance().collection("posts")
            db.document(postId)
                .set(post)
        }
    }
}