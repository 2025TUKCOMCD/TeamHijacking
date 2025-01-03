package com.example.front.data

data class AccessibleResponse(
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
    val totalWalk: Int
)

data class SubPath(
    val trafficType: Int,
    val distance: Double?,
    val startName: String?,
    val endName: String?,
    val lane: List<Lane>?
)

data class Lane(
    val name: String?,
    val busNo: String?
)