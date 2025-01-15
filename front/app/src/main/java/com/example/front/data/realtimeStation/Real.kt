package com.example.front.data.realtimeStation

data class Real(
    val routeId: String,
    val updownFlag: String,
    val arrival1: Arrival?,
    val arrival2: Arrival?,
    val localRouteId: String,
    val stationSeq: String,
    val routeNm: String
)
