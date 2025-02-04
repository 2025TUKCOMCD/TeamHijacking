package com.example.front.iot.SmartHome

data class DeviceItem(
    val device: Device,           // 실제 API로부터 받은 기기 데이터
    var isSelected: Boolean = false, // 기기가 선택된 상태인지 표시
    var isOnline: Boolean = true     // 기기가 온라인 상태인지 표시
)
