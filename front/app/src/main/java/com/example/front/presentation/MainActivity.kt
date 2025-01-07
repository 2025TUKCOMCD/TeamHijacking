package com.example.front.presentation

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.front.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // activity_main.xml 레이아웃 설정
        setContentView(R.layout.activity_main)

        // XML에서 정의된 버튼들을 연결
        val transportButton: Button = findViewById(R.id.btn_transport)
        val audioGuideButton: Button = findViewById(R.id.btn_audio_guide)
        val iotHomeButton: Button = findViewById(R.id.btn_iot_home)

        // 각 버튼의 클릭 이벤트 처리
        transportButton.setOnClickListener {
            // 대중교통 버튼 클릭 시 실행할 로직
        }

        audioGuideButton.setOnClickListener {
            // 음향 유도기 버튼 클릭 시 실행할 로직
        }

        iotHomeButton.setOnClickListener {
            // IoT 스마트 홈 버튼 클릭 시 실행할 로직
        }
    }
}
