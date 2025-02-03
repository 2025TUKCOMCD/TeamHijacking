package com.example.front.transportation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.front.R
import com.example.front.databinding.ActivityTransNewPathDetatilBinding
import com.example.front.databinding.ActivityTransRealTimeLocationBinding

class TransRealtimeLocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransRealTimeLocationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransRealTimeLocationBinding.inflate(layoutInflater)
        setContentView(binding.main)


    }
}