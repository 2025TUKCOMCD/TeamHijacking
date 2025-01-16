package com.example.front.transportation.data.realtimeStation

data class Arrival(
    val fulCarAt: String,
    val busPlateNo: String,
    val waitStatus: String,
    val busStatus: String,
    val endBusYn: String,
    val leftStation: Int,
    val lowBusYn: String,
    val congestion: Int,
    val arrivalSec: Int,
    val nmprType: Int
)

