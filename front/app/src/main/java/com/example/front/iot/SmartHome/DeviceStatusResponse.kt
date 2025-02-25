package com.example.front.iot.SmartHome

data class DeviceStatusResponse(
    val components: Map<String, ComponentStatus>
)

data class ComponentStatus(
    val switch: SwitchStatus?,
    val temperatureMeasurement: TemperatureStatus?,
    val contactSensor: ContactSensorStatus?
)

data class SwitchStatus(
    val switch: ValueStatus
)

data class TemperatureStatus(
    val temperature: ValueStatus
)

data class ContactSensorStatus(
    val contact: ValueStatus
)