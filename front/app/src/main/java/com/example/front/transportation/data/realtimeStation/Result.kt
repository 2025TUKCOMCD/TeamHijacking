package com.example.front.transportation.data.realtimeStation

import com.example.front.transportation.data.realtimeLocation.Base

data class Result(
    val base : List<Base>,
    val real: List<Real>
)
