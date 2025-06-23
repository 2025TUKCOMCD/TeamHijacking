package com.example.front.login.data

data class SmartThingsRequest(
    val accessToken : String = "",
    val tokenType : String = "Bearer",
    val expiresAt : String
)
