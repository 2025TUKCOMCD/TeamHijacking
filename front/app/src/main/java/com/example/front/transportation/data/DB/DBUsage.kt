package com.example.front.transportation.data.DB

data class DBUsage(
    val startName : String? = null,
    val endName : String? = null,
    val startLng: Double,
    val startLat: Double,
    val endLng: Double,
    val endLat: Double,
    val routeId: String? = null
)
