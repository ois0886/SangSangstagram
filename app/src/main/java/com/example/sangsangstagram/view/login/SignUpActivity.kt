package com.example.sangsangstagram.view.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.sangsangstagram.databinding.ActivitySignUpBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    lateinit var binding : ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signUpButton.setOnClickListener {
            val userEmail = binding.email.text.toString()
            val password = binding.password.text.toString()
            Firebase.auth.createUserWithEmailAndPassword(userEmail, password) // 계정 생성과 동시에 로그인 함.
                .addOnCompleteListener(this) {
                    if (it.isSuccessful) { // 로그인 성공
                        startActivity(
                            Intent(this, LoginActivity::class.java)
                        )
                        finish()
                    }
                    else {
                        Log.w("LoginActivity", "createUserWithEmail", it.exception)
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}