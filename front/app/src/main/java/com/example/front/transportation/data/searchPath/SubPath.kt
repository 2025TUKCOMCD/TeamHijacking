package com.example.front.transportation.data.searchPath

data class SubPath(
    val trafficType: Int,
    val distance: Double?,
    val startName: String?,
    val endName: String?,
    val startID: Int?,
    val lane: List<Lane>?,
    val sectionTime: Int?,
    val startLocalStationID : String?,
    val passStopList: PassStopList?
)