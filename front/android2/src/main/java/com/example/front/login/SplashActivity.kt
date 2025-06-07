package com.example.front.login

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.front.MainActivity
import com.example.front.login.processor.UserProcessor

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_splash) // 간단한 로고나 배경이 있는 레이아웃 띄울 수 있음

        // 예: SharedPreferences 에 로그인 상태 저장 되어 있다고 가정
        val sharedPrefs = getSharedPreferences("userPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPrefs.contains("loginId")
        val loginId = sharedPrefs.getString("loginId", null)
        val name = sharedPrefs.getString("name", null)


        // 로그인 여부에 따라 화면 전환
        if (!loginId.isNullOrEmpty() && !name.isNullOrEmpty()) {
            // 서버에 등록된 사용자 확인
            UserProcessor.getUserByLogin(loginId) { user ->
                if (user != null) {
                    // 등록된 사용자: 바로 메인으로 이동
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("userName", user.name)
                    startActivity(intent)
                    finish()
                } else {
                    // 서버에는 없음 → 로그인 화면으로 이동하여 재등록 유도
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        } else {
            // SharedPreferences에 정보가 없는 경우 → 로그인 화면으로 이동
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}