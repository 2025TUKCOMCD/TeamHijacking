package com.example.front.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.front.audioguide.AudioGuideActivity
import com.example.front.databinding.ActivityMainBinding
import com.example.front.transportation.TransportationMainActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        // activity_main.xml 레이아웃 설정
        setContentView(binding.main)

        // XML에서 정의된 버튼들을 연결
        val transportButton: Button = binding.btnTransport
        val audioGuideButton: Button = binding.btnAudioGuide
        val iotHomeButton: Button = binding.btnIotHome

        // 각 버튼의 클릭 이벤트 처리
        transportButton.setOnClickListener {
            // 대중교통 버튼 클릭 시 실행할 로직
            val intent = Intent(this, TransportationMainActivity::class.java)
            startActivity(intent)
        }

        audioGuideButton.setOnClickListener {
            // 음향 유도기 버튼 클릭 시 실행할 로직
            val intent = Intent(this, AudioGuideActivity::class.java)
            startActivity(intent)
        }

        iotHomeButton.setOnClickListener {
            // IoT 스마트 홈 버튼 클릭 시 실행할 로직
        }
    }
}
