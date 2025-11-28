package kr.io.seemore.login.data

data class SmartThingsRequest(
    val accessToken : String = "",
    val tokenType : String = "Bearer",
    val expiresAt : String
)
