package com.example.front.transportation.data.realTime

data class RealtimeDTO(
    val  type : String,          // 버스, 지하철 도보 여부
    val  boarding : Boolean,        // 탑승 여부(true/false)
    val  id : String,            // 대중교통 ID (busId, lineId 등)
    val  stationId : String,     // 버스/지하철 역 ID
    val  stationName : String,   // 지하철일 경우만 지하철 역 이름
    val  direction : String
)
