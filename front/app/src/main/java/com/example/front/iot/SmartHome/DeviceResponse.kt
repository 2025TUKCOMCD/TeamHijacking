package com.example.front.iot.SmartHome

data class DeviceResponse(
    val items: List<Device>
)

data class Command(
    val capability: String,
    val command: String
)
