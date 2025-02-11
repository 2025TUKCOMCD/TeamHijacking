package com.example.front.presentation

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.ViewTreeObserver
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.ImageButton
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.front.audioguide.AudioGuideActivity
import com.example.front.databinding.ActivityMainBinding
import com.example.front.iot.HomeIotActivity
import com.example.front.setting.SettingActivity
import com.example.front.transportation.TransportationMainActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mediaPlayer: MediaPlayer? = null
    private var hasReachedEnd = false // 스크롤 끝 도달 여부 확인 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        // activity_main.xml 레이아웃 설정
        setContentView(binding.main)

        // XML에서 정의된 버튼들을 연결
        val transportButton: ImageButton = binding.btnTransport
        val audioGuideButton: ImageButton = binding.btnAudioGuide
        val iotHomeButton: ImageButton = binding.btnIotHome
        val settingButton: ImageButton = binding.btnSetting

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
            val intent = Intent(this, HomeIotActivity::class.java)
            startActivity(intent)
        }

        settingButton.setOnClickListener{
            // Setting 버튼 클릭 시 실행할 로직
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
    }
}
