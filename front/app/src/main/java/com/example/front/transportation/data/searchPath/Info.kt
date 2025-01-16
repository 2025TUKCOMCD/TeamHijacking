package com.example.front.transportation.data.searchPath

data class Info(
    val totalTime: Int,
    val totalWalk: Int,
    val busTransitCount: Int,     // 버스 환승 횟수
    val subwayTransitCount: Int   // 지하철 환승 횟수
)
