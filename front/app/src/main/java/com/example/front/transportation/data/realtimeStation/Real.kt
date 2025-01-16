package com.example.front.transportation.data.realtimeStation

data class Real(
    val updownFlag: String,
    val routeNm: String,
    val arrival1: Arrival1?,
    val arrival2: Arrival2?
)
