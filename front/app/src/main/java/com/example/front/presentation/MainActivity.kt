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
import com.example.front.app.WatchAppOpener
import com.example.front.audioguide.AudioGuideBLEConnectActivity
import com.example.front.databinding.ActivityMainBinding
import com.example.front.iot.HomeIotActivity
import com.example.front.setting.SettingActivity
import com.example.front.transportation.TransportationMainActivity
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItemBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val TAG = "워치_MainActivity"
    private val DATA_PATH = "/my_data" // 모바일 앱에서 사용한 데이터 경로
    private val LOGIN_PATH = "/kakao" // 모바일 앱에서 사용한 데이터 경로
    private val KEY_MESSAGE = "아무데이터" // 모바일 앱에서 보낸 데이터의 키


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
        checkExistingData(DATA_PATH)

//        if(checkExistingData(LOGIN_PATH) == null){
//            // 요청 코드 여기에 함수를 더 적어야함 ex 어떻게 하면 핸드폰으로 보낼 껀지 등등
//            val opener = WatchAppOpener() // WatchAppOpener 인스턴스 생성
//            opener.sendOpenAppRequestToPhone(this, "워치에서 앱 열기 요청!") // 스마트폰 앱 열기 요청 전송
//        }
        // 앱 시작 시 로그인 상태 (DATA_PATH) 확인 및 UI 업데이트
        CoroutineScope(Dispatchers.Main).launch {
            val existingData = checkExistingData(LOGIN_PATH) // 데이터 경로로 DATA_PATH 전달
            if (existingData != null) {
                val message = existingData
                Log.d(TAG, "초기 로드 (기존 데이터): 메시지='$message'")
            } else {
                Log.d(TAG, "초기 로드 (기존 데이터 없음): 메시지='기존 데이터 없음'")

                // DATA_PATH에 데이터가 비어있을 경우 (로그인 필요 상황 가정)
                // 커스텀 다이얼로그 띄우기
                val dialog = PhoneLoginPromptDialog(this@MainActivity)
                dialog.show()

                // 그리고 로그인 요청 메시지 보내기
                val opener = WatchAppOpener() // WatchAppOpener 인스턴스 생성
                opener.sendOpenAppRequestToPhone(this@MainActivity, "워치에서 앱 열기 요청!") // 스마트폰 앱 열기 요청 전송
            }
        }
    }

    // 액티비티가 다시 시작될 때 브로드캐스트 리시버 등록
    override fun onResume() {
        super.onResume()
        // 워치 앱의 MyWearableListenerService에서 보낸 액션과 동일해야 합니다.
        // 예시: "com.example.yourpackage.app.DATA_RECEIVED"
        LocalBroadcastManager.getInstance(this).registerReceiver(
            dataReceiver,
            IntentFilter("com.example.front.presentation") // 워치 앱의 정확한 패키지 이름으로 변경!
        )
    }

    // 액티비티가 일시 정지될 때 브로드캐스트 리시버 해제
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataReceiver)
    }

    private fun checkExistingData(path : String) : String? {
        // 비동기적으로 데이터 조회를 수행하기 위해 코루틴을 사용합니다.
        // IO 디스패처는 네트워크 또는 디스크 I/O 작업에 적합합니다.
        var getMessage: String? = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataClient: DataClient = Wearable.getDataClient(this@MainActivity)
                // 현재 연결된 노드로부터 모든 DataItem을 조회합니다.
                // 특정 경로의 데이터만 조회하려면 Uri.parse("wear://*/my_data")와 같이 사용할 수도 있습니다.
                val dataItemsTask = dataClient.getDataItems()
                val dataItemBuffer: DataItemBuffer = Tasks.await(dataItemsTask) // Task가 완료될 때까지 대기

                var foundData = false
                // 조회된 각 DataItem을 반복하여 확인합니다.
                for (dataItem in dataItemBuffer) {
                    // DataItem의 경로가 우리가 원하는 경로와 일치하는지 확인합니다.
                    if (dataItem.uri.path == DATA_PATH) {
                        val dataMap = DataMapItem.fromDataItem(dataItem).getDataMap()
                        // DataMap에서 "my_message" 키와 "timestamp" 키의 값을 추출합니다.
                        val message = dataMap.getString(KEY_MESSAGE, null.toString())
                        val timestamp = dataMap.getLong("timestamp", 0L)

                        Log.d(TAG, "기존 데이터 발견: 메시지='$message', 타임스탬프=$timestamp")
                        getMessage = message

                        // UI 업데이트는 메인 스레드에서 수행되어야 합니다.
                        withContext(Dispatchers.Main) {
                            Log.d("현빈","초기 로드: $message" )
                            Log.d("현빈", "시간: ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date(timestamp))}")
                        }
                        foundData = true
                        break // 필요한 데이터 하나를 찾았으므로 더 이상 검색할 필요가 없습니다.
                    }
                }
                dataItemBuffer.release() // DataItemBuffer는 사용 후 반드시 릴리스해야 합니다.
                // 만약 특정 경로의 데이터를 찾지 못했다면 UI에 해당 상태를 표시합니다.
                if (!foundData) {
                    withContext(Dispatchers.Main) {
                        Log.d("현빈", "메시지: 기존 데이터 없음")
                        Log.d("현빈", "시간: N/A")
                    }
                    Log.d(TAG, "경로 $DATA_PATH 에서 기존 데이터 없음")
                }

            } catch (e: Exception) {
                // 데이터 조회 중 오류 발생 시 처리
                Log.e(TAG, "기존 데이터 확인 중 오류 발생: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Log.d("현빈", "메시지: 데이터 로드 오류")
                    Log.d("현빈", "시간: N/A")
                }
            }

        }
        Log.d("현빈", getMessage.toString())
        return getMessage
    }

}
