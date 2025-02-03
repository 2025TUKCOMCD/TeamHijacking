package com.example.front.iot.SmartHome

data class Command(
    val capability: String,
    val command: String,
    val arguments: List<Any>? = null
)

