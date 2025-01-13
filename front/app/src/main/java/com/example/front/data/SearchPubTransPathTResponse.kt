package com.example.front.data

data class SearchPubTransPathTResponse(
    val result: Result?
)

data class Result(
    val path: List<Path>?
)

data class Path(
    val info: Info,
    val subPath: List<SubPath>
)

data class Info(
    val totalTime: Int,
    val totalWalk: Int,
    val busTransitCount: Int,     // 버스 환승 횟수
    val subwayTransitCount: Int   // 지하철 환승 횟수
)
    data class SubPath(
    val trafficType: Int,
    val distance: Double?,
    val startName: String?,
    val endName: String?,
    val lane: List<Lane>?,
    val sectionTime: Int?
)

data class Lane(
    val name: String?,
    val busNo: String?
)

