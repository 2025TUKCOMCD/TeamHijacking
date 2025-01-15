package com.example.front.data.realtimeStation

data class RealtimeStation(
    val result: Result
)

data class Result(
    val real: List<Real>
)

data class Real(
    val routeId: String,
    val updownFlag: String,
    val arrival1: Arrival?,
    val arrival2: Arrival?,
    val localRouteId: String,
    val stationSeq: String,
    val routeNm: String
)

data class Arrival(
    val fulCarAt: String,
    val busPlateNo: String,
    val waitStatus: String,
    val busStatus: String,
    val endBusYn: String,
    val leftStation: Int,
    val lowBusYn: String,
    val congestion: Int,
    val arrivalSec: Int,
    val nmprType: Int
)
