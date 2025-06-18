package com.example.front.transportation.data.DB

data class DBSaveDTO(
    val departureName : String? = null,
    val destinationName : String? = null,
    val userRouteCount : Int,
    val isFavorite : Boolean = false,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val savedRouteName: String? = null,
    val loginId: String,
    val transportrouteKey: Int?

    )


