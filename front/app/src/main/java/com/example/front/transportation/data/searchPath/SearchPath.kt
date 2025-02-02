package com.example.front.transportation.data.searchPath


data class SearchPath(
    val result: Result?
)

data class Result(
    val path: List<Path>?
)

data class Path(
    val pathType : Int, // 사용
    val info: Info, // 사용
    val subPath: List<SubPath> // 사용
)

data class Info(
    val totalTime: Int, //사용
    val totalWalk: Int, // 사용
    val busTransitCount: Int,     // 사용
    val subwayTransitCount: Int   // 사용
)

data class SubPath(
    val trafficType: Int, // 사용
    val stationCount : Int, // 사용
    val distance: Double?,
    val startName: String?,
    val endName: String?,
    val startID: Int?,
    val endID: Int?,
    val lane: List<Lane>?, // 사용
    val sectionTime: Int?, //사용
    val startLocalStationID : String?,
    val passStopList: PassStopList?,
    val endLocalStationID : String?
)
data class Lane(
    val name: String?, // 사용
    val busNo: String?, // 사용
    val type: Int?,
    val busID : Int?, // 사용
    val busCityCode: Int?, // 사용
    val passStopList: List<PassStopList>
)

data class PassStopList(
    val Stations : List<Stations> // 사용
)
data class Stations(
    val stationID: Int, // 사용
    val stationName : String,
    val stationNumber: String,
    val stationType: String,
    val localStationID : String // 사용
)

data class PathRouteResult(
    val totalTime: Int, // 사용
    val transitCount: Int, // 사용
    val mainTransitTypes: String, // 사용
    val detailedPath: String, // 사용
    val busDetails : List<String>, // 사용
)
