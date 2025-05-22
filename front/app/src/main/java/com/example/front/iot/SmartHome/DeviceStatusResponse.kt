package com.example.front.iot.SmartHome

data class DeviceStatusResponse(
    val components: Map<String, ComponentStatus>
)

data class ComponentStatus(
    val switch: SwitchStatus?,
    val temperatureMeasurement: TemperatureStatus?,
    val contactSensor: ContactSensorStatus?,
    val switchLevel: CapabilityStatus?,
    val colorControl: ColorControlStatus?,
    val custom: CustomStatus? // <- 새로 추가한 모드 정보 커스텀상태
)

data class CustomStatus(
    val lightMode: LightModeStatus?
)

data class LightModeStatus(
    val lightMode: ValueStatus
)

data class CapabilityStatus(
    val value: String
)
data class ColorControlStatus(
    val hue: AttributeValue?,
    val saturation: AttributeValue?
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

data class ValueStatus(
    val value: String
)

data class AttributeValue(
    val value: Double
)