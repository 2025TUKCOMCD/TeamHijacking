package com.test.myapplication.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.test.myapplication.databinding.ActivityAudioGuideBinding

class AudioGuideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAudioGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackAudio.setOnClickListener {
            finish()
        }
    }
}
