package com.example.front.iot.SmartHome

class DeviceControlHelper(private val apiToken: String) {
    private val apiService = RetrofitClient.instance

    // 기기 명령 전송
    fun sendDeviceCommand(deviceId: String, capability: String, command: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val commandBody = CommandBody(
            commands = listOf(Command(capability, command))
        )


}