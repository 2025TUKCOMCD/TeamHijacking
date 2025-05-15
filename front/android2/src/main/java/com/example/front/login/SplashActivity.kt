package com.example.front.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.front.MainActivity
import com.example.front.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
// setContentView(R.layout.activity_splash) // 간단한 로고나 배경이 있는 레이아웃 띄울 수 있음

        // 예: SharedPreferences 에 로그인 상태 저장 되어 있다고 가정
        val sharedPrefs = getSharedPreferences("userPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPrefs.contains("loginId")

        // 로그인 여부에 따라 화면 전환
        if (isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}