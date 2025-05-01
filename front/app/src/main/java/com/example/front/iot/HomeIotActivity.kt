package com.example.front.iot

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.example.front.BuildConfig
import com.example.front.R
import com.example.front.iot.SmartHome.*

class HomeIotActivity : AppCompatActivity() {

    private lateinit var apiToken: String
    private lateinit var voiceControlHelper: VoiceControlHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_iot)

        apiToken = "Bearer ${BuildConfig.SMARTTHINGS_API_TOKEN}"
        voiceControlHelper = VoiceControlHelper(this) { command ->
            processVoiceCommand(command)
        }

        // 🟦 IoT 추가 버튼 → SmartThings 앱 이동
        findViewById<Button>(R.id.btnAddDevice).setOnClickListener {
            openSmartThingsApp()
        }

        // 🟧 My IoT 페이지로 이동
        findViewById<Button>(R.id.btnMyIot).setOnClickListener {
            Log.d("현빈", "들어옴")
            val intent = Intent(this, MyIotActivity::class.java)
            startActivity(intent)
        }

        // 🎤 음성 명령 시작
        findViewById<Button>(R.id.btnVoiceControl).setOnClickListener {
            voiceControlHelper.startVoiceRecognition()
        }
    }

    // 🔗 SmartThings 앱 열기 (설치되지 않았으면 Play Store로 이동)
    private fun openSmartThingsApp() {
        try {
            val intent = packageManager.getLaunchIntentForPackage("com.samsung.android.oneconnect")
            if (intent != null) {
                startActivity(intent)
            } else {
                val playStoreIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.samsung.android.oneconnect")
                )
                startActivity(playStoreIntent)
            }
        } catch (e: ActivityNotFoundException) {
            showToast("SmartThings 앱을 실행할 수 없습니다.")
        }
    }

    // 🎤 음성 명령 예시 처리
    private fun processVoiceCommand(command: String) {
        showToast("음성 명령: \"$command\" 인식됨 (기능 연동 필요)")
        // 실제 제어는 MyIotActivity에서 처리
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
