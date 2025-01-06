package com.test.myapplication.presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.test.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 버튼 클릭 이벤트 설정
        binding.btnTransport.setOnClickListener {
            startActivity(Intent(this, TransportActivity::class.java))
        }

        binding.btnAudioGuide.setOnClickListener {
            startActivity(Intent(this, AudioGuideActivity::class.java))
        }

        binding.btnIotHome.setOnClickListener {
            startActivity(Intent(this, IoTHomeActivity::class.java))
        }
    }
}
