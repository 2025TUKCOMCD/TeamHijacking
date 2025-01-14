package com.example.front.data.searchPath

import com.example.front.data.realtimeBus.DetailInfo

data class PathRouteResult(
    val totalTime: Int,
    val transitCount: Int,
    val mainTransitTypes: String,
    val detailedPath: String,
    val startStationIDs: Array<Int>,
    val busIDs: Array<Int>
)

