package com.example.front.data.realtimeLocation

//1. 최상위 노드
data class RealtimeRouteResponse(
    val result: Result?
)

//2. 노선 기반 정보 포함 노드
data class Result(
    val base: List<Base>?,
    val real: List<Real>?,
)

//3. 노선 정보 노드
data class Base(
    val busID: Int,
    val station: List<Station>?
)
//4. 정류장 정보 노드
data class Station(
    val idx: Int,
    val stationId: Int,
    val stationName: String,
    val x: Double,
    val y: Double
)
//5. 실시간 버스 위치 정보 노드
data class Real(
    val fromStationId: String,
    val toStationId: String,
    val positionX: Double?,
    val positionY: Double?
)

data class DetailInfo(
    val fromStationId: Int,
    val toStationId: Int,
    val positionX: Int,
    val positionY: Int
)

