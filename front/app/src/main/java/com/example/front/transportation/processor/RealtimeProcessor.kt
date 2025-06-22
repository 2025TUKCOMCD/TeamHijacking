package com.example.front.transportation.processor

import com.example.front.BuildConfig
import com.example.front.transportation.data.realTime.RealtimeDTO
import com.example.front.transportation.data.realTime.RealtimeResponseDTO
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object RealtimeProcessor {

    private const val Host_URL = BuildConfig.Host_URL
    private const val POLLING_INTERVAL_MS = 10 * 1000L // 30초 (밀리초 단위)
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private var pollingJob: Job? = null
    var currentRealtimeData: RealtimeDTO? = null
    private var onResponseReceived: ((RealtimeResponseDTO) -> Unit)? = null
    private var onErrorOccurred: ((String, Int?) -> Unit)? = null // <-- onError 콜백 추가
    private var currentEndpoint: String = "/api/realTime" // 현재 사용 중인 엔드포인트 저장

    // 즉시 요청을 보내는 중인지 확인하는 플래그 (중복 요청 방지)
    private val isImmediateRequestInProgress = AtomicBoolean(false)


    /**
     * 주기적인 POST 요청을 시작합니다.
     * @param endpoint API 엔드포인트 (예: "/realtime")
     * @param initialRealtimeData 초기 전송할 RealtimeDTO 객체
     * @param onResponse 콜백 함수: API 응답을 RealtimeActivity로 전달
     * @param onError 콜백 함수 (선택적): API 오류 발생 시 오류 메시지와 코드 전달
     */
    fun startPolling(
        endpoint: String = "/api/realTime",
        initialRealtimeData: RealtimeDTO,
        onResponse: ((RealtimeResponseDTO) -> Unit),
        onError: ((String, Int?) -> Unit)? = null // <-- 파라미터로 onError 추가
    ) {
        if (pollingJob?.isActive == true) {
            println("RealtimeProcessor: Polling is already active. Stopping old job and starting new one.")
            stopPolling()
        }

        this.currentEndpoint = endpoint // 엔드포인트 저장
        this.currentRealtimeData = initialRealtimeData
        this.onResponseReceived = onResponse
        this.onErrorOccurred = onError // <-- onError 콜백 저장

        println("RealtimeProcessor: Starting polling with initial JSON data: ${gson.toJson(currentRealtimeData)}")

        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                // 즉시 요청이 진행 중일 때는 주기적 폴링을 건너뜙니다.
                if (!isImmediateRequestInProgress.get()) {
                    performApiRequest()
                } else {
                    println("RealtimeProcessor: Immediate request in progress, skipping periodic poll.")
                }
                delay(POLLING_INTERVAL_MS)
            }
        }
        println("RealtimeProcessor: Polling for $endpoint started every ${POLLING_INTERVAL_MS / 1000} seconds.") // 1분 -> 30초로 변경
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        currentRealtimeData = null
        onResponseReceived = null
        onErrorOccurred = null // <-- onError 콜백도 초기화
        isImmediateRequestInProgress.set(false) // 폴링 중지 시 플래그 초기화
        println("RealtimeProcessor: Stopped polling.")
    }

    /**
     * RealtimeProcessor 내부의 RealtimeDTO를 업데이트하고,
     * 특정 조건(boarding == 2)일 경우 즉시 API 요청을 트리거합니다.
     * @param updatedRealtimeData 새롭게 업데이트할 RealtimeDTO 객체
     */
    fun requestUpdate(updatedRealtimeData: RealtimeDTO) {
        val oldBoarding = this.currentRealtimeData?.boarding
        this.currentRealtimeData = updatedRealtimeData
        println("RealtimeProcessor: RealtimeDTO updated to: ${gson.toJson(updatedRealtimeData)}")

        // 지하철이고 boarding이 1에서 2로 바뀌었을 경우 즉시 API 요청을 트리거합니다.
        // 그리고 현재 즉시 요청이 진행 중이 아닐 때만 실행합니다.
        if (updatedRealtimeData.type == 1 && updatedRealtimeData.boarding == 2 && oldBoarding == 1 &&
            !isImmediateRequestInProgress.getAndSet(true)) { // 플래그 설정 및 검사
            println("RealtimeProcessor: Boarding changed to 2. Triggering immediate API request.")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    performApiRequest()
                } finally {
                    isImmediateRequestInProgress.set(false) // 요청 완료 후 플래그 해제
                    println("RealtimeProcessor: Immediate API request finished, flag reset.")
                }
            }
        }
    }

    /**
     * 실제 API 요청을 수행하고 응답을 콜백으로 전달하는 내부 함수
     */
    private suspend fun performApiRequest() {
        currentRealtimeData?.let { data ->
            try {
                val jsonString: String = gson.toJson(data)
                val requestBody = jsonString.toRequestBody(JSON_MEDIA_TYPE)

                val request = Request.Builder()
                    .url("$Host_URL$currentEndpoint") // 저장된 엔드포인트 사용
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        println("RealtimeProcessor: API 요청 성공. 응답: $responseBody")
                        responseBody?.let {
                            val parsedResponse = gson.fromJson(it, RealtimeResponseDTO::class.java)
                            withContext(Dispatchers.Main) {
                                onResponseReceived?.invoke(parsedResponse)
                            }
                        }
                    } else {
                        // API 요청 실패 시 onError 콜백 호출
                        val errorMessage = response.body?.string() ?: response.message
                        println("RealtimeProcessor: API 요청 실패. 코드: ${response.code}, 메시지: $errorMessage")
                        withContext(Dispatchers.Main) { // UI 스레드에서 콜백 호출
                            onErrorOccurred?.invoke(errorMessage, response.code)
                        }
                    }
                }
            } catch (e: IOException) {
                // 네트워크 오류 발생 시 onError 콜백 호출
                println("RealtimeProcessor: API 요청 중 IO 오류 발생: ${e.message}")
                withContext(Dispatchers.Main) { // UI 스레드에서 콜백 호출
                    onErrorOccurred?.invoke("네트워크 연결 오류: ${e.message}", null)
                }
            } catch (e: Exception) {
                // 기타 알 수 없는 오류 발생 시 onError 콜백 호출
                println("RealtimeProcessor: API 요청 중 알 수 없는 오류 발생: ${e.message}")
                withContext(Dispatchers.Main) { // UI 스레드에서 콜백 호출
                    onErrorOccurred?.invoke("알 수 없는 오류 발생: ${e.message}", null)
                }
            }
        } ?: run {
            println("RealtimeProcessor: currentRealtimeData가 null이므로 폴링을 중지합니다.")
            stopPolling()
        }
    }
}