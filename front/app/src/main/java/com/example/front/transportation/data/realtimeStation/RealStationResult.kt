package com.example.front.transportation.data.realtimeStation

data class RealStationResult(
    val leftStation : Int,
    val arrivalSec : Int,
    val busStatus : String,
    val endBusYn : String,
    val lowBusYn : String,
    val fulCarAt : String
)
