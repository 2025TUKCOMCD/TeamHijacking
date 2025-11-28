package kr.io.seemore // 스마트폰 앱의 실제 패키지 이름

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import java.nio.charset.StandardCharsets
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem

/**
 * Wear OS 워치로부터 메시지 및 데이터 이벤트를 수신하는 서비스입니다.
 */
class PhoneMessageListenerService : WearableListenerService() {

    private val TAG = "PhoneMessageListener"
    private val MESSAGE_PATH_OPEN_APP = "/kakao"


    // 워치에서 DataMap으로 전송한 데이터 경로
    private val DATA_PATH_ALL_TRANS_DATA = "/all_trans_data"

    override fun onMessageReceived(messageEvent: MessageEvent) {
        try {
            Log.d(TAG, "메시지 수신됨: ${messageEvent.getPath()}")

            when (messageEvent.getPath()) {
                MESSAGE_PATH_OPEN_APP -> {
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
                DATA_PATH_ALL_TRANS_DATA->{
                    Log.d("현빈", "들어옴${messageEvent}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onMessageReceived 처리 중 예외 발생: ${e.message}", e)
        }
    }


    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "onDataChanged() 호출: ${dataEvents.count}개의 이벤트 수신")

        for (event in dataEvents) {
            val dataItem: DataItem = event.dataItem
            if (event.type == DataEvent.TYPE_CHANGED && dataItem.uri.path == DATA_PATH_ALL_TRANS_DATA) {
                try {
                    val dataMapItem = DataMapItem.fromDataItem(dataItem)
                    val dataMap = dataMapItem.dataMap

                    // DataMap에서 각 데이터 추출
                    val pathTransitType = dataMap.getIntegerArrayList("pathTransitType")
                    val transitTypeNo = dataMap.getStringArrayList("transitTypeNo")
                    val startLat = dataMap.getDouble("startLat")
                    val startLng = dataMap.getDouble("startLng")
                    val endLat = dataMap.getDouble("endLat")
                    val endLng = dataMap.getDouble("endLng")
                    val departureName = dataMap.getString("departureName")
                    val destinationName = dataMap.getString("destinationName")

                    Log.d(TAG, "DataMap 데이터 수신: $pathTransitType, $transitTypeNo, $departureName -> $destinationName")

                    // 수신된 데이터를 Intent에 담아 브로드캐스트로 전송
                    val dataIntent = Intent("ACTION_ALL_TRANS_DATA")
                    dataIntent.putIntegerArrayListExtra("pathTransitType", pathTransitType)
                    dataIntent.putStringArrayListExtra("transitTypeNo", transitTypeNo)
                    dataIntent.putExtra("startLat", startLat)
                    dataIntent.putExtra("startLng", startLng)
                    dataIntent.putExtra("endLat", endLat)
                    dataIntent.putExtra("endLng", endLng)
                    dataIntent.putExtra("departureName", departureName)
                    dataIntent.putExtra("destinationName", destinationName)

                    LocalBroadcastManager.getInstance(this).sendBroadcast(dataIntent)
                    Log.d(TAG, "모든 교통 데이터를 브로드캐스트로 전송했습니다.")

                } catch (e: Exception) {
                    Log.e(TAG, "DataMap 데이터 처리 중 오류 발생: ${e.message}", e)
                }
            }
        }
    }
}