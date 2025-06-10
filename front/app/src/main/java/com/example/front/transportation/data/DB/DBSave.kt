package com.example.front.transportation.data.DB

import java.sql.Time

data class DBSave(
    val departureName : String? = null,
    val destinationName : String? = null,
    val whenFirstGo : String,
    val whenLastGo : String,
    val userouteCount : Int,
    val isFavorite : Boolean = false,
    val startLng: Double,
    val startLat: Double,
    val endLng: Double,
    val endLat: Double,
    val savedRouteName: String? = null
)


