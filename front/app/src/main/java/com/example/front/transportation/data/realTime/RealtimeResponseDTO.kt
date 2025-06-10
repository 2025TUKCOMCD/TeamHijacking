package com.example.front.transportation.data.realTime

data class RealtimeResponseDTO(
    val trainNo : String = "0", // 기차 번호;
    val vehId: String? = null,
    val nextRequest : Int = 0, // 1: 현재 대중교통 2: 다음 대중교통
    val location: String? = null, // 현재 위치
    val predictTimes1: String? = null,
    val predictTimes2: String? = null
)
