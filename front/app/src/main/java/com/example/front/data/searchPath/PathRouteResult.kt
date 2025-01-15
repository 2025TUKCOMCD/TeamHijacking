package com.example.front.data.searchPath


data class PathRouteResult(
    val routeStationsAndBuses : List<Pair<Int,Int>>,
    val totalTime: Int,
    val transitCount: Int,
    val mainTransitTypes: String,
    val detailedPath: String,
)

