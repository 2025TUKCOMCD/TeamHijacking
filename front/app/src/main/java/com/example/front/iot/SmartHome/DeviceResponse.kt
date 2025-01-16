package com.example.front.iot.SmartHome

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

data class CommandBody(
    val commands: List<Command>
)

data class Command(
    val capability: String,
    val command: String
)
