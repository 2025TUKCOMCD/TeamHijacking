package com.example.front.data.smartHome

data class DeviceResponse(
    val items: List<Device>
)

data class Device(
    val deviceId: String,
    val label: String,
    val deviceTypeName: String,
    val components: List<Component>
)

data class Component(
    val id: String,
    val capabilities: List<Capability>
)

data class Capability(
    val id: String
)
