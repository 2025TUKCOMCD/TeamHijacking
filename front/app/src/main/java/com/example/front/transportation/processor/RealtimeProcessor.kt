package com.example.front.transportation.processor

import com.example.front.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.*
import okio.ByteString

object RealtimeProcessor {

    private const val Host_URL = BuildConfig.Host_URL // BuildConfig에서 Host_URL을 가져옴

    fun main() {
        // WebSocket을 위한 OkHttpClient 생성
        val client = OkHttpClient()

        // WebSocket 연결 요청
        val request = Request.Builder()
            .url("$Host_URL/realtime") // Host_URL을 활용하여 WebSocket URL 설정
            .build()

        // WebSocket Listener 설정
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WebSocket 연결 성공")
                webSocket.send("Hello, Server!") // 메시지 전송
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("서버로부터 메시지: $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                println("서버로부터 바이너리 메시지: $bytes")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("WebSocket 연결 종료: $reason")
                webSocket.close(code, reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket 오류 발생: ${t.message}")
            }
        }

        // WebSocket 연결 실행
        client.newWebSocket(request, listener)

        // WebSocket 사용 후 클라이언트 종료
        client.dispatcher.executorService.shutdown()
    }

}
