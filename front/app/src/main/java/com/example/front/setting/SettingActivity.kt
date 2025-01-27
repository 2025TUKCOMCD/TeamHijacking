package com.example.front.setting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //view 바인딩
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}