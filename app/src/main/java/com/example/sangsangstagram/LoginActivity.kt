package com.example.sangsangstagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sangsangstagram.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}