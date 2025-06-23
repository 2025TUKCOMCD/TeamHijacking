package com.example.front.iot.smartHome

data class Device(
    val deviceId: String,
    val name: String, // ex. 무드등
    val label: String,
    val deviceTypeName: String, // 기기 종류 ex. Light
    val components: List<Component>,  //iot 기기의 아이디 받아 오는 부분, 리스트 형식 기기 받아옴
    val manufacturer: String? = null, // 기기 제조사
    val location: String? = null, // 기기 설치 위치
)

//디바이스 객체 하나에 id 전부 받아옴?