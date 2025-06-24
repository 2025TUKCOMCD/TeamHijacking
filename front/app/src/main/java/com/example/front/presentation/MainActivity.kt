package com.example.front.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.front.app.PhoneLoginPromptDialog
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
    private val KEY_MESSAGE = "login_id" // 모바일 앱에서 보낸 데이터의 키
    private var loginPromptDialog: PhoneLoginPromptDialog? = null



    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val receivedMessage = it.getStringExtra("received_message")
                val receivedTimestamp = it.getLongExtra("received_timestamp", 0L)

                val app = context?.applicationContext as? userid
                app?.receivedMessage = receivedMessage.toString()
                app?.receivedTimestamp = receivedTimestamp

                Log.d(TAG,"실시간 Message: $receivedMessage")
                Log.d(TAG,"실시간 Time: ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date(receivedTimestamp))}")

                // ... rest of your existing logic
                if (!receivedMessage.isNullOrEmpty()) {
                    loginPromptDialog?.updateLoginStatus(receivedMessage)
                    loginPromptDialog?.dismiss()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val app = this.applicationContext as? userid

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
        //checkExistingData(DATA_PATH)

//        if(checkExistingData(LOGIN_PATH) == null){
//            // 요청 코드 여기에 함수를 더 적어야함 ex 어떻게 하면 핸드폰으로 보낼 껀지 등등
//            val opener = WatchAppOpener() // WatchAppOpener 인스턴스 생성
//            opener.sendOpenAppRequestToPhone(this, "워치에서 앱 열기 요청!") // 스마트폰 앱 열기 요청 전송
//        }
        // 앱 시작 시 로그인 상태 (DATA_PATH) 확인 및 UI 업데이트
        CoroutineScope(Dispatchers.Main).launch {
            val existingData = checkExistingData(LOGIN_PATH) // 데이터 경로로 DATA_PATH 전달
            Log.d(TAG, existingData.toString())
            if (existingData != null) {
                val message = existingData
                Log.d(TAG, "초기 로드 (기존 데이터): 메시지='$message'")
                app?.receivedMessage = message

            } else {
                Log.d(TAG, "초기 로드 (기존 데이터 없음): 메시지='기존 데이터 없음'")

                // DATA_PATH에 데이터가 비어있을 경우 (로그인 필요 상황 가정)
                // 커스텀 다이얼로그 띄우기
                loginPromptDialog = PhoneLoginPromptDialog(this@MainActivity) // 다이얼로그 인스턴스 생성 및 참조 저장
                loginPromptDialog?.show() // 다이얼로그 표시

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
            IntentFilter("com.example.front.DATA_RECEIVED") // 워치 앱의 정확한 패키지 이름으로 변경!
        )
        Log.d(TAG, "BroadcastReceiver 등록 완료")
    }

    // 액티비티가 일시 정지될 때 브로드캐스트 리시버 해제
    override fun onPause() {
        super.onPause()
        // BroadcastReceiver 해제
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataReceiver)
        Log.d(TAG, "BroadcastReceiver 해제 완료")
        // 액티비티가 일시 정지될 때 다이얼로그가 열려있다면 닫기
        loginPromptDialog?.dismiss() // **다이얼로그 닫기 추가**
        loginPromptDialog = null // **다이얼로그 참조 해제**
    }

    /**
     * Wear OS 데이터 레이어에 특정 경로의 데이터가 있는지 확인하고 메시지 문자열을 반환하는 함수입니다.
     * 데이터가 있으면 문자열 메시지를 반환하고, 없거나 오류 발생 시 null을 반환합니다.
     * @param path 확인할 데이터 아이템의 경로 (예: "/my_data", "/kakao")
     * @return String? (메시지) 또는 null
     */
    private suspend fun checkExistingData(path: String): String? { // **suspend fun으로 변경, 반환 타입 String?**
        return try {
            // withContext(Dispatchers.IO)를 사용하여 백그라운드 스레드에서 Tasks.await()를 호출합니다.
            val message = withContext(Dispatchers.IO) { // **이 부분 추가/변경됨**
                val dataClient: DataClient = Wearable.getDataClient(this@MainActivity)
                val dataItemsTask = dataClient.getDataItems()
                val dataItemBuffer: DataItemBuffer = Tasks.await(dataItemsTask) // 비동기 작업이 완료될 때까지 대기

                var foundMessage: String? = null // 찾은 메시지를 저장할 변수

                for (dataItem in dataItemBuffer) {
                    // DataItem의 경로가 인자로 받은 'path'와 일치하는지 확인
                    if (dataItem.uri.path == path) { // **path 인자를 사용하도록 수정**
                        val dataMap = DataMapItem.fromDataItem(dataItem).getDataMap()
                        val currentMessage = dataMap.getString(KEY_MESSAGE, null.toString()) // **KEY_MESSAGE 사용**
                        // val timestamp = dataMap.getLong("timestamp", 0L) // 타임스탬프는 반환하지 않으므로 사용하지 않음

                        Log.d(TAG, "경로 '$path'에서 데이터 발견: 메시지='$currentMessage'")

                        foundMessage = currentMessage // 메시지 결과만 저장
                        break // 필요한 데이터 하나를 찾았으므로 더 이상 검색할 필요가 없습니다.
                    }
                }
                dataItemBuffer.release() // DataItemBuffer는 사용 후 반드시 릴리스해야 합니다.
                foundMessage // withContext 블록의 최종 결과로 메시지 반환
            }
            message // try 블록의 최종 결과로 메시지 반환

        } catch (e: Exception) {
            Log.e(TAG, "경로 '$path'의 기존 데이터 확인 중 오류 발생: ${e.message}", e)
            null // 오류 발생 시 null 반환
        }
    }

}
