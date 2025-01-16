package com.example.front.transportation.data.searchPath

data class Lane(
    val name: String?,
    val busNo: String?,
    val type: Int?,
    val busID : Int?,
    val passStopList: List<PassStopList>
)
