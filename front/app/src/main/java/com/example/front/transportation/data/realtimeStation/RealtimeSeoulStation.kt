package com.example.front.transportation.data.realtimeStation

import com.google.gson.annotations.SerializedName

// 최상위 데이터 구조로, API 응답의 전체 데이터를 포함
data class RealtimeSeoulStation(
    val msgBody: MsgBody? // 메시지 본문 (실제 데이터)
)

// 메시지 본문으로 실제 데이터를 포함


// 버스 도착 정보를 나타내는 데이터 클래스
data class Item(
    val stNm: String?,       // 정류소 이름
    val busRouteAbrv: String?, // 버스 노선 이름 (약칭)
    val rtNm: String?,       // 노선 이름
    val plainNo1: String?,   // 첫 번째 차량 번호판
    val traTime1: String?,   // 첫 번째 차량의 남은 시간 (초 단위)
    val isArrive1: String?,  // 첫 번째 차량 도착 여부 ("Y"/"N")
    val arrmsg1: String?,    // 첫 번째 차량 도착 메시지
    val plainNo2: String?,   // 두 번째 차량 번호판
    val traTime2: String?,   // 두 번째 차량의 남은 시간 (초 단위)
    val isArrive2: String?,  // 두 번째 차량 도착 여부 ("Y"/"N")
    val arrmsg2: String?     // 두 번째 차량 도착 메시지
)
