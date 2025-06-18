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
        try {
            Log.d(TAG, "메시지 수신됨: ${messageEvent.getPath()}")

            if (messageEvent.getPath() == MESSAGE_PATH_OPEN_APP) {
                val message = String(messageEvent.getData(), StandardCharsets.UTF_8)
                Log.d(TAG, "앱 열기 요청 메시지 수신: $message")

                val launchIntent = packageManager.getLaunchIntentForPackage("com.example.front")
                launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                if (launchIntent != null) {
                    startActivity(launchIntent)
                    Log.d(TAG, "스마트폰 앱 'com.example.front' 실행 시도")
                } else {
                    Log.e(TAG, "스마트폰 앱 'com.example.front'을(를) 찾을 수 없거나 실행할 수 없습니다.")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onMessageReceived 처리 중 예외 발생: ${e.message}", e)
        }
    }
}
