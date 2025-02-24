package com.example.front.transportation.data.searchPath


data class RouteRequest(
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double
)


data class Route(
    val totalTime : Int,
    val transitCount : Int,
    val mainTransitType: String, // 1: 지하철, 2: 버스, 3: 버스+지하철 순서 나열
    val pathTransitType: List<Int>,  // (1: 지하철, 2: 버스, 3: 도보) 순서 나열
    val transitTypeNo: List<String>,  // 지하철, 버스, 도보 번호 순서 나열

    // 구조화된 처리
    val routeIds: List<RouteId> // 여러 버스의 버스 경로 ID 리스트를 구조화된 형태로 저장
)

data class RouteId(
    val busLocalBlID : List<Int>, // 버스 로컬 블 ID 리스트
    val startStationInfo : Int,
    val endStationInfo : Int,
    val stationInfo: List<Int>, // 정류장 정보 리스트
    val predictTimes1: List<String>, // 여러 버스의 predictTime1 리스트
    val predictTimes2: List<String> // 여러 버스의 predictTime2 리스트
)
