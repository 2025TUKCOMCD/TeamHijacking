package com.example.front.transportation.data.searchPath

import java.io.Serializable

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
    val pathTransitType: List<Int>,
    val transitTypeNo: List<String>,
    val routeIds: List<RouteId>
)

data class RouteId(
    val transportLocalID: Int,
    val startStationInfo: Int,
    val endStationInfo: Int,
    val subwayLineName: String,
    val trainDirection: String,
    val startX : Double,
    val startY : Double,
    val transferStations: List<String>,
    val stationInfo: List<Int>,
    val predictTimes1: String,
    val predictTimes2: String
) : Serializable

