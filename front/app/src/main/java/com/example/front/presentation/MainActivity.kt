package com.example.front.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.ImageButton
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.front.R
import com.example.front.audioguide.AudioGuideBLEConnectActivity
import com.example.front.databinding.ActivityMainBinding
import com.example.front.iot.HomeIotActivity
import com.example.front.setting.SettingActivity
import com.example.front.transportation.TransportationMainActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // LocalBroadcastManager로부터 메시지를 수신할 Receiver 정의
    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val receivedMessage = it.getStringExtra("received_message")
                val receivedTimestamp = it.getLongExtra("received_timestamp", 0L)

                Log.d("현빈","Message: $receivedMessage")
                Log.d("현빈","Time: ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date(receivedTimestamp))}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        // activity_main.xml 레이아웃 설정
        setContentView(binding.main)

        // XML 에서 정의된 버튼 들을 연결
        val transportButton: ImageButton = binding.btnTransport
        val audioGuideButton: ImageButton = binding.btnAudioGuide
        val iotHomeButton: ImageButton = binding.btnIotHome
        val settingButton: ImageButton = binding.btnSetting

        // 각 버튼의 클릭 이벤트 처리
        transportButton.setOnClickListener {
            // 대중 교통 버튼 클릭 시 실행할 로직
            val intent = Intent(this, TransportationMainActivity::class.java)
            startActivity(intent)
        }

        audioGuideButton.setOnClickListener {
            // 음향 유도기 버튼 클릭 시 실행할 로직
            val intent = Intent(this, AudioGuideBLEConnectActivity::class.java)
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

    // 액티비티가 다시 시작될 때 브로드캐스트 리시버 등록
    override fun onResume() {
        super.onResume()
        // 워치 앱의 MyWearableListenerService에서 보낸 액션과 동일해야 합니다.
        // 예시: "com.example.yourpackage.app.DATA_RECEIVED"
        LocalBroadcastManager.getInstance(this).registerReceiver(
            dataReceiver,
            IntentFilter("com.example.yourpackage.app.DATA_RECEIVED") // 워치 앱의 정확한 패키지 이름으로 변경!
        )
    }

    // 액티비티가 일시 정지될 때 브로드캐스트 리시버 해제
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataReceiver)
    }

}
