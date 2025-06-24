package com.example.front.presentation

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class MyWearableListenerService : WearableListenerService() {

    private val TAG = "WatchListenerService"
    // 모바일 앱에서 PutDataMapRequest.create()에 사용한 경로와 동일해야 합니다.
    private val DATA_PATH = "/my_data"
    private val KEY_MESSAGE = "아무데이터" // 모바일 앱에서 보낸 데이터의 키와 동일해야 합니다.

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "onDataChanged: ${dataEvents.count} data events received.")

        for (event in dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem이 변경된 경우
                val dataItem: DataItem = event.getDataItem()
                // 데이터 경로가 우리가 기대하는 경로와 일치하는지 확인
                if (dataItem.getUri().getPath() == DATA_PATH) {
                    val dataMap: DataMap = DataMapItem.fromDataItem(dataItem).getDataMap()

                    // 모바일 앱에서 보낸 "my_message" 키의 문자열 값을 추출
                    val receivedMessage = dataMap.getString(KEY_MESSAGE)
                    val timestamp = dataMap.getLong("timestamp", 0L) // 타임스탬프도 추출

                    Log.d(TAG, "Received DataItem: Message='$receivedMessage', Timestamp=$timestamp")

                    // UI 업데이트를 위해 MainActivity로 메시지를 전달 (로컬 브로드캐스트 사용)
                    val messageIntent = Intent("com.example.front.DATA_RECEIVED")
                    messageIntent.putExtra("received_message", receivedMessage)
                    messageIntent.putExtra("received_timestamp", timestamp)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent)

                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem이 삭제된 경우 (필요하다면 처리)
                Log.d(TAG, "DataItem deleted: ${event.getDataItem().getUri()}")
            }
        }
    }

    // 메시지 수신 시 처리 (선택 사항, 모바일에서 MessageApi를 사용했다면 필요)
    /*
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.getPath() == "/my_message_path") {
            val message = String(messageEvent.getData(), StandardCharsets.UTF_8)
            Log.d(TAG, "Received message: $message")
            // UI 업데이트 등을 위한 로컬 브로드캐스트 전송
        }
    }
    */
}