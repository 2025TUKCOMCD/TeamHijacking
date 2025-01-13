package com.example.front.audioguide

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.front.R
import com.example.front.databinding.ActivityAudioGuideBinding

class AudioGuideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioGuideBinding.inflate(layoutInflater)

        setContentView(binding.main)
    }
}