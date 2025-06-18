package com.example.front.app // 워치 앱의 실제 패키지 이름으로 변경

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import java.nio.charset.StandardCharsets

/**
 * Wear OS 워치에서 연결된 스마트폰 앱을 여는 요청을 전송하는 유틸리티 클래스입니다.
 * MessageApi를 사용하여 스마트폰으로 특정 메시지를 보냅니다.
 */
class WatchAppOpener {

    private val TAG = "WatchAppOpener"
    // 스마트폰 앱을 열기 위한 메시지 경로 (스마트폰 앱의 PhoneMessageListenerService와 일치해야 합니다)
    private val MESSAGE_PATH_OPEN_APP = "/kakao" // 워치와 스마트폰이 약속한 경로

    /**
     * 연결된 스마트폰으로 앱 실행 요청 메시지를 보냅니다.
     * @param context 현재 Context (Activity, Service 등)
     * @param message 전송할 메시지 내용 (선택 사항, 주로 트리거 역할)
     */
    fun sendOpenAppRequestToPhone(context: Context, message: String = "") {
        // 연결된 모든 노드(스마트폰)를 비동기적으로 찾습니다.
        val getConnectedNodesTask: Task<List<Node>> = Wearable.getNodeClient(context).connectedNodes

        getConnectedNodesTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val nodes: List<Node>? = task.result
                if (nodes.isNullOrEmpty()) {
                    Log.w(TAG, "연결된 스마트폰 노드가 없습니다.")
                    Toast.makeText(context, "스마트폰이 연결되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }

                // 일반적으로 워치와 스마트폰은 한 쌍이므로, 찾은 노드 중 첫 번째에 메시지를 보냅니다.
                // 모든 연결된 노드에 메시지를 보내려면 for 루프를 사용할 수 있습니다.
                for (node in nodes) {
                    val sendMessageTask: Task<Int> = Wearable.getMessageClient(context).sendMessage(
                        node.id, MESSAGE_PATH_OPEN_APP, message.toByteArray(StandardCharsets.UTF_8)
                    )

                    sendMessageTask.addOnCompleteListener { sendTask ->
                        if (sendTask.isSuccessful) {
                            Log.d(TAG, "앱 열기 요청 메시지를 노드 '${node.displayName}'에 성공적으로 보냈습니다.")
                            Toast.makeText(context, "스마트폰 앱 열기 요청 성공!", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e(TAG, "앱 열기 요청 메시지 전송 실패: ${sendTask.exception?.message}")
                            Toast.makeText(context, "스마트폰 앱 열기 요청 실패: ${sendTask.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                Log.e(TAG, "연결된 노드를 가져오는데 실패했습니다: ${task.exception?.message}")
                Toast.makeText(context, "스마트폰 연결 상태 확인 불가: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}