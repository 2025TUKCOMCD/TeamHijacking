package com.example.front.transportation.data.realtimeStation.bus


data class RealtimeSeoulStation(
    val msgBody: MsgBody?
)

data class MsgBody(
    val itemList: List<Item>?
)

data class Item(
    val stNm: String?,
    val busRouteAbrv: String?,
    val rtNm: String?,
    val plainNo1: String?,
    val traTime1: String?,
    val isArrive1: String?,
    val arrmsg1: String?,
    val plainNo2: String?,
    val traTime2: String?,
    val isArrive2: String?,
    val arrmsg2: String?
)