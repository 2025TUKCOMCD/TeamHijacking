package kr.io.seemore.iot.SmartHome

data class Command(
    val capability: String,
    val command: String,
    val arguments: List<Any>? = null
)