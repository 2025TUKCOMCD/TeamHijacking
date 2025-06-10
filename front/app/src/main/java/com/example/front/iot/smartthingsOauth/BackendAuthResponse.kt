package com.example.front.iot.smartthingsOauth

data class BackendResponse(
    val message: String, // 백엔드에서 보내는 간단한 메시지 (예: "SmartThings 연동 완료!")
    val success: Boolean, // 작업의 성공 여부 (true/false)
    // TODO: 만약 백엔드에서 SmartThings 연동 성공 후 사용자 세션 토큰 (예: JWT)을 발급하여 안드로이드 앱에 준다면,
    // 여기에 해당 토큰 필드를 추가할 수 있습니다.
    // 예: val authToken: String? = null
)