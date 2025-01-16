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
}