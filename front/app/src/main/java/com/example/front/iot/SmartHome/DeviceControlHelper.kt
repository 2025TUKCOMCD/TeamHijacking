package com.example.front.iot.SmartHome

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeviceControlHelper(private val apiToken: String) {
    private val apiService = RetrofitClient.instance

    // 기기 명령 전송
    fun sendDeviceCommand(deviceId: String, capability: String, command: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val commandBody = CommandBody(
            commands = listOf(Command(capability, command))
        )
        apiService.sendCommand(deviceId, commandBody, apiToken).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    Log.d("SmartThings", "Command sent successfully!")
                    onSuccess()
                } else {
                    val error = "Failed to send command: ${response.code()} - ${
                        response.errorBody()?.string()
                    }"
                    Log.e("SmartThings", error)
                    onError(error)
                }
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                val error = "Error sending command: ${t.message}"
                Log.e("SmartThings", error)
                onError(error)
            }
        })
    }
    private fun sendCommand(deviceId: String, commandBody: CommandBody, onSuccess: () -> Unit, onError: (String) -> Unit) {
        apiService.sendCommand(deviceId, commandBody, apiToken).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val error = "Failed to send command: ${response.code()} - ${response.errorBody()?.string()}"
                    onError(error)
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                onError("Error sending command: ${t.message}")
            }
        })
    }


    // 전원 명령 보내기
    fun sendSwitchCommand(deviceId: String, turnOn: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val command = if (turnOn) "on" else "off"
        sendDeviceCommand(deviceId, "switch", command, onSuccess, onError)
    }

    // 밝기 조절
    fun setBrightness(deviceId: String, brightness: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val commandBody = CommandBody(
            commands = listOf(
                Command(
                    capability = "switchLevel",
                    command = "setLevel",
                    arguments = listOf(brightness)
                )
            )
        )
        sendCommand(deviceId, commandBody, onSuccess, onError)
    }

    // 색상 조절
    fun setColor(deviceId: String, hue: Int, saturation: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val commandBody = CommandBody(
            commands = listOf(
                Command(
                    capability = "colorControl",
                    command = "setColor",
                    arguments = listOf(
                        mapOf(
                            "hue" to hue,
                            "saturation" to saturation
                        )
                    )
                )
            )
        )
        sendCommand(deviceId, commandBody, onSuccess, onError)
    }

    // 얘로 원래 기기의 초기 상태 값을 불러오려고 했는데 잘 안되는거 같음 => 수정예정
    fun getDeviceStatus(deviceId: String, onSuccess: (DeviceStatusResponse) -> Unit, onError: (String) -> Unit) {
        apiService.getDeviceStatus(deviceId, apiToken).enqueue(object : Callback<DeviceStatusResponse> {
            override fun onResponse(call: Call<DeviceStatusResponse>, response: Response<DeviceStatusResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    onSuccess(response.body()!!)
                } else {
                    onError("Failed to fetch device status: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<DeviceStatusResponse>, t: Throwable) {
                onError("Error fetching device status: ${t.message}")
            }
        })
    }
}