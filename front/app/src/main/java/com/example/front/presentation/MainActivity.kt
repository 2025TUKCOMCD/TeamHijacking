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
        val scrollView: ScrollView = binding.root.findViewById(R.id.scrollView) // 스크롤뷰 가져오기

        // 🚀 스크롤 끝 감지 및 소리 재생 기능 추가
        detectScrollEnd(scrollView)

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

    // ✅ 스크롤이 끝에 도달하면 소리 재생 및 TalkBack 이벤트 트리거
    private fun detectScrollEnd(scrollView: ScrollView) {
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val view = scrollView.getChildAt(scrollView.childCount - 1)
            val diff = view.bottom - (scrollView.height + scrollView.scrollY)

            if (diff == 0 && !hasReachedEnd) {
                hasReachedEnd = true // 이미 도달했는지 확인하여 중복 실행 방지
                playEndSound()
                announceAccessibility("화면의 끝에 도달했습니다.") // TalkBack 지원 추가
            } else if (diff > 0) {
                hasReachedEnd = false // 스크롤이 다시 올라가면 다시 감지 가능하도록 설정
            }
        }
    }


    // ✅ 1~3초 길이의 wav 파일 재생 (res/raw/end_reached.wav 파일 필요)
    private fun playEndSound() {
        mediaPlayer?.release() // 기존 재생 중인 미디어 플레이어 해제
        mediaPlayer = MediaPlayer.create(this, R.raw.end_reached) // res/raw/end_reached.wav 파일 재생
        mediaPlayer?.start()
    }

    // ✅ TalkBack을 통해 "화면의 끝에 도달했습니다"를 읽어줌
    private fun announceAccessibility(message: String) {
        val accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (accessibilityManager.isEnabled) {

        }
    }
}
