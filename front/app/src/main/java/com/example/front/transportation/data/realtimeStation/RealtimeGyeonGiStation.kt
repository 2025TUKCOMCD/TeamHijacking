package com.example.front.transportation.data.realtimeStation

data class RealtimeGyeonGiStation(
    val response : Response?
)

data class Response(
    val msgBody: MsgBody?
)



data class BusArrivalItem(
    val stationNm1: String?,
    val routeName: String?,
    val predictTimeSec1: String?,
    val predictTime2: String?,
)