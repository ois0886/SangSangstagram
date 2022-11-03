package com.example.sangsangstagram

import android.app.Activity
import android.os.Bundle
import com.example.sangsangstagram.databinding.ActivityWearableBinding

class WearableActivity : Activity() {

    private lateinit var binding: ActivityWearableBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWearableBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}