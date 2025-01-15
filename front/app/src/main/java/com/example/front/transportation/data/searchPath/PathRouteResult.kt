package com.example.front.transportation.data.searchPath


data class PathRouteResult(
    val routeStationsAndBuses : List<Pair<Int,Int>>,
    val totalTime: Int,
    val transitCount: Int,
    val mainTransitTypes: String,
    val detailedPath: String,
)

