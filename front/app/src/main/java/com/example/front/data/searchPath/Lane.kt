package com.example.front.data.searchPath

data class Lane(
    val name: String?,
    val busNo: String?,
    val busID : Int?,
    val passStopList: List<PassStopList>
)
