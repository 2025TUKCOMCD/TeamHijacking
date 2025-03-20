package com.example.front.transportation.data.realtimeStation.bus

data class RealtimeGyeonGiStation(
    val response: Response?
)

data class Response(
    val msgBody: GyeonGiMsgBody?
)

data class GyeonGiMsgBody(
    val busArrivalItem: BusArrivalItem?
)

data class BusArrivalItem(
    val stationNm1: String?,
    val routeName: String?,
    val predictTime1: String?,
    val predictTime2: String?
)