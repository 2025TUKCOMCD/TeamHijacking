package com.test.myapplication.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.test.myapplication.databinding.ActivityTransportBinding

class TransportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBus.setOnClickListener {
            // Add your bus-related logic here
        }

        binding.btnSubway.setOnClickListener {
            // Add your subway-related logic here
        }

        binding.btnBackTransport.setOnClickListener {
            finish()
        }
    }
}
