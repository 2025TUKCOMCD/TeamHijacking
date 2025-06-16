package com.example.front // 스마트폰 앱의 실제 패키지 이름

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import java.nio.charset.StandardCharsets

/**
 * Wear OS 워치로부터 메시지를 수신하는 서비스입니다.
 * 특정 메시지 경로를 통해 워치 앱으로부터 스마트폰 앱 실행 요청을 받으면,
 * 스마트폰 앱의 메인 액티비티를 실행합니다.
 */
class PhoneMessageListenerService : WearableListenerService() {

    private val TAG = "PhoneMessageListener"
    // 워치 앱에서 스마트폰 앱을 열기 위해 보낼 메시지 경로와 동일해야 합니다.
    private val MESSAGE_PATH_OPEN_APP = "/kakao"

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "메시지 수신됨: ${messageEvent.getPath()}")

        // 수신된 메시지 경로가 앱 열기 요청 경로와 일치하는지 확인
        if (messageEvent.getPath() == MESSAGE_PATH_OPEN_APP) {
            val message = String(messageEvent.getData(), StandardCharsets.UTF_8)
            Log.d(TAG, "앱 열기 요청 메시지 수신: $message")

            // 스마트폰 앱의 메인 액티비티를 실행합니다.
            // packageManager.getLaunchIntentForPackage()를 사용하여 앱의 런처 액티비티 Intent를 가져옵니다.
            // "com.example.front"는 스마트폰 앱의 실제 applicationId로 변경해야 합니다.
            val launchIntent = packageManager.getLaunchIntentForPackage("com.example.front")
            // 서비스에서 액티비티를 시작할 때는 FLAG_ACTIVITY_NEW_TASK 플래그가 필요합니다.
            launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            if (launchIntent != null) {
                startActivity(launchIntent)
                Log.d(TAG, "스마트폰 앱 'com.example.front' 실행 시도")
            } else {
                Log.e(TAG, "스마트폰 앱 'com.example.front'을(를) 찾을 수 없거나 실행할 수 없습니다.")
            }
        }
    }
}
