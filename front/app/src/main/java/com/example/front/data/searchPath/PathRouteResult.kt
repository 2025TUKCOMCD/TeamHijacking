package com.example.front.data.searchPath

import com.example.front.data.realtimeBus.DetailInfo

data class PathRouteResult(
    val routeStationsAndBuses : List<Pair<Int,Int>>,
    val totalTime: Int,
    val transitCount: Int,
    val mainTransitTypes: String,
    val detailedPath: String,
)

