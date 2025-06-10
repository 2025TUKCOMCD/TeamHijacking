package com.example.front.iot.smartthingsOauth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface YourBackendApiService {

    /**
     * SmartThings에서 받은 인증 코드를 Spring 백엔드로 전송하여 토큰 교환을 요청합니다.
     *
     * @param codeData Map<String, String> 형태의 요청 바디 (authorization code와 state를 포함)
     * @return 백엔드로부터의 응답 (BackendAuthResponse)
     */
    @POST("/api/auth/smartthings-callback") // <-- TODO: 당신의 Spring 백엔드에서 이 코드를 받을 API 엔드포인트 경로로 변경하세요
    suspend fun exchangeSmartThingsCode(@Body codeData: Map<String, String?>): Response<BackendAuthResponse>

    // --- 여기에 Spring 백엔드가 제공할 다른 API 엔드포인트들을 추가할 수 있습니다. ---

    // 예시: SmartThings 기기 목록을 백엔드로부터 가져오는 API (백엔드가 SmartThings API 호출 후 전달)
    // @GET("/api/devices")
    // suspend fun getSmartThingsDevices(): Response<List<DeviceDto>> // DeviceDto는 안드로이드 앱에서 정의할 기기 데이터 클래스

    // 예시: 특정 기기의 최신 온도/습도 데이터를 가져오는 API
    // @GET("/api/devices/{deviceId}/data")
    // suspend fun getDeviceData(@Path("deviceId") deviceId: String): Response<DeviceDataDto>
}

// TODO: DeviceDto, DeviceDataDto 등 다른 API 응답에 대한 데이터 클래스도 이 api 패키지 내에 정의할 수 있습니다.