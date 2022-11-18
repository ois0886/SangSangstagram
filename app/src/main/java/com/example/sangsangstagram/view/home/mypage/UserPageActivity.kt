package com.example.sangsangstagram.view.home.mypage

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import com.example.sangsangstagram.R
import com.example.sangsangstagram.data.model.UserDto
import com.example.sangsangstagram.databinding.ActivityUserPageBinding
import com.example.sangsangstagram.view.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

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

        viewModel

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    //액션버튼 메뉴 액션바에 집어 넣기
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_menu, menu)
        return true
    }

    //액션버튼 클릭 했을 때
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.userInfoUpdate -> {
                val intent = InfoUpdateActivity.getIntent(this)
                startActivity(intent)
                super.onOptionsItemSelected(item)
            }
            R.id.userLogOut -> {
                Firebase.auth.signOut()
                val intent = LoginActivity.getIntent(this)
                startActivity(intent)
                finish()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
