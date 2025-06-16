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
                Command("switchLevel", "setLevel", listOf(brightness))
            )
        )
        sendCommand(deviceId, commandBody, onSuccess, onError)
    }


    // 색상 및 채도 조절
    fun setColor(deviceId: String, hue: Int, saturation: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val commandBody = CommandBody(
            commands = listOf(
                Command(
                    "colorControl", "setColor",
                    listOf(mapOf("hue" to hue, "saturation" to saturation))
                )
            )
        )
        sendCommand(deviceId, commandBody, onSuccess, onError)
    }


    // 모드 전환: WHITE <-> COLOUR, 추후 만들 모드 전환 스위치를 위한 함수
    fun setLightMode(deviceId: String, mode: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val commandBody = CommandBody(
            commands = listOf(
                Command("custom.lightMode", "setLightMode", listOf(mode))
            )
        )
        sendCommand(deviceId, commandBody, onSuccess, onError)
    }


    // 모드를 자동으로 확인 후, 컬러 설정 함 -> 테스트하기 용이하기 위함
    fun setColorWithAutoMode(deviceId: String, hue: Int, saturation: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        getDeviceStatus(deviceId,
            onSuccess = { status ->
                val currentMode = status.components["main"]?.custom?.lightMode?.lightMode?.value
                if (currentMode.equals("COLOUR", ignoreCase = true)) {
                    setColor(deviceId, hue, saturation, onSuccess, onError)
                } else {
                    setLightMode(deviceId, "COLOUR",
                        onSuccess = {
                            setColor(deviceId, hue, saturation, onSuccess, onError)
                        },
                        onError = onError
                    )
                }
            },
            onError = onError
        )
    }


    // AI 스피커 제어: 재생 / 일시정지
    fun sendMediaCommand(deviceId: String, command: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val commandBody = CommandBody(
            commands = listOf(
                Command("mediaPlayback", command)
            )
        )
        sendCommand(deviceId, commandBody, onSuccess, onError)
    }





    // 기기 상태 불러옴, Online, Offline 확인 가능하게 추후 수정
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