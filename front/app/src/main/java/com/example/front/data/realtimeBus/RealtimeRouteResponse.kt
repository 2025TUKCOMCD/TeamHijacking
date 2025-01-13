package com.example.front.data.realtimeBus

data class RealtimeRouteResponse(
    val result: RealtimeResult?
)

data class RealtimeResult(
    val path: List<RealtimePath>?
)

data class RealtimePath(
    val info: RealtimeInfo,
    val subPath: List<RealtimeSubPath>
)

data class RealtimeInfo(
    val totalTime: Int,
    val totalWalk: Int,
    val busTransitCount: Int,
    val subwayTransitCount: Int
)

data class RealtimeSubPath(
    val trafficType: Int,
    val distance: Double?,
    val startName: String?,
    val endName: String?,
    val lane: List<RealtimeLane>?,
    val sectionTime: Int?
)

data class RealtimeLane(
    val name: String?,
    val busNo: String?
)