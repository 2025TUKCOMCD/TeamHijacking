package com.example.front.iot.SmartHome

data class DeviceStatusResponse(
    val components: Map<String, ComponentStatus>
)

