package com.example.front.iot.SmartHome

data class SmartThingsRequest(
    val accessToken : String = "",
    val tokenType : String = "Bearer",
    val expiresAt : String
)
