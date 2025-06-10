package com.example.front.iot.SmartHome

data class Device(
    val deviceId: String,
    val name: String, // ex. 무드등
    val label: String,
    val deviceTypeName: String, // 기기 종류 ex. Light
    val components: List<Component>,
    val manufacturer: String? = null, // 기기 제조사
    val location: String? = null, // 기기 설치 위치
)