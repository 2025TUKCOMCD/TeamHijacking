package com.example.front.transportation.data.searchPath

data class RouteRequest(
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double
)

data class Route(
    val totalTime: Int,
    val transitCount: Int,
    val mainTransitType: String,
    val detailedPath: String,
    val detailTrans: String,
    val predictTimes1: List<String>,
    val predictTimes2: List<String>,
    val busErrors: List<String>,
    val routeIds: List<List<Int>>
)