package com.example.front.transportation.data.busLaneDetail

import com.google.gson.annotations.SerializedName

data class BusLaneDetail(
    val result: Result?
)

data class Result(
    val busNo: String,                // 버스 번호
    val busID: Int,                   // 버스 ID
    val type: Int,                    // 버스 타입
    val busCityCode: Int,             // 버스 도시 코드
    val busLocalBlID: String,
    @SerializedName("stations")// 버스 지역 ID
    val laneStations: List<Station>    // 정류장 목록
)
data class Station(
    val idx: Int,                     // 정류장 인덱스
    val localStationID: String        // 정류장 지역 ID
)
