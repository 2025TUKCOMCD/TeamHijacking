package com.example.front.transportation.data.realtimeStation

data class Arrival2(
    val leftStation : Int,
    val arrivalSec : Int,
    val busStatus : String,
    val endBusYn : String,
    val lowBusYn : String,
    val fulCarAt : String
)
