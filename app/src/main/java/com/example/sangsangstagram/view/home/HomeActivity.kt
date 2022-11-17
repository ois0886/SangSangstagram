package com.example.sangsangstagram.view.home

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.sangsangstagram.R
import com.example.sangsangstagram.databinding.ActivityHomeBinding
import androidx.navigation.ui.setupWithNavController
import com.example.sangsangstagram.view.home.post.postcreate.PostCreateActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomeActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, HomeActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.bottom_nav)
            .setupWithNavController(navController)
        binding.bottomNav.setupWithNavController(navController)

        binding.postCreateButton.setOnClickListener {
            startActivity(Intent(this, PostCreateActivity::class.java))
        }
    }
}