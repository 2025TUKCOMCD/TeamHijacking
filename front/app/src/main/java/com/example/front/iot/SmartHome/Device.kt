package com.example.front.iot.SmartHome

data class Device(
    val deviceId: String,
    val name: String,
    val label: String,
    val deviceTypeName: String,
    val components: List<Component>,
    val manufacturer: String? = null,
    val location: String? = null,
)