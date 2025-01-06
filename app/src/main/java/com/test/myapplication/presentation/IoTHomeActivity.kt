package com.test.myapplication.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.test.myapplication.databinding.ActivityIotHomeBinding

class IoTHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIotHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIotHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackIot.setOnClickListener {
            finish()
        }
    }
}
