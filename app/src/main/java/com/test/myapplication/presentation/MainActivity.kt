package com.test.myapplication.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import com.test.myapplication.R

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // XML 레이아웃 설정
        setContentView(R.layout.activity_main)

        // 뷰 초기화
        val btnTransport = findViewById<Button>(R.id.btn_transport)
        val btnAudioGuide = findViewById<Button>(R.id.btn_audio_guide)
        val btnIoTHome = findViewById<Button>(R.id.btn_iot_home)

        // 클릭 이벤트 설정
        btnTransport.setOnClickListener { navigateToActivity(TransportActivity::class.java) }
        btnAudioGuide.setOnClickListener { navigateToActivity(AudioGuideActivity::class.java) }
        btnIoTHome.setOnClickListener { navigateToActivity(IoTHomeActivity::class.java) }
    }

    // 화면 전환 메서드
    private fun navigateToActivity(activityClass: Class<*>) {
        Log.d("MainActivity", "${activityClass.simpleName}로 이동")
        startActivity(Intent(this, activityClass))
    }
}
