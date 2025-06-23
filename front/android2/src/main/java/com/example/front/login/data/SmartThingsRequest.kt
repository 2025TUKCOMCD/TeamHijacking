package com.example.front.Login.data

data class SmartThingsRequest(
    val accessToken : String = "",
    val tokenType : String = "Bearer",
    val expiresAt : String
)
