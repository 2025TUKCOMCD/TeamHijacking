package com.example.front.transportation.data.DB

data class GetRouteResponseDTO(
    val transportrouteKey : Int,
    val departureName : String? = null,
    val destinationName : String? = null,
    val whenFirstGo : String? = null,
    val whenFirstBack : String? = null,
    val userRouteCount : Int,
    val isFavorite : Boolean = false,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val savedRouteName: String? = null,
    val loginId: String // 사용자 ID 추가
)
