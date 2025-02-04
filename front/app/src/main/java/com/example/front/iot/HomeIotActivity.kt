package com.example.front.iot

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.front.BuildConfig
import com.example.front.R
import com.example.front.iot.SmartHome.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeIotActivity : AppCompatActivity() {

    private lateinit var apiToken: String
    private lateinit var deviceControlHelper: DeviceControlHelper
    private lateinit var voiceControlHelper: VoiceControlHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_iot)

        // ✅ SmartThings API 토큰 초기화
        apiToken = "Bearer ${BuildConfig.SMARTTHINGS_API_TOKEN}"
        deviceControlHelper = DeviceControlHelper(apiToken)
        voiceControlHelper = VoiceControlHelper(this) { command ->
            processVoiceCommand(command)
        }

        // 📡 기기 목록 가져오기
        fetchDeviceList()

        // 🎤 음성 명령 버튼 클릭 시 이벤트 처리
        findViewById<Button>(R.id.btnVoiceControl).setOnClickListener {
            voiceControlHelper.startVoiceRecognition()
        }
    }

    // 1️⃣ SmartThings API로 기기 목록 가져오기
    private fun fetchDeviceList() {
        RetrofitClient.instance.getDevices(apiToken).enqueue(object : Callback<DeviceResponse> {
            override fun onResponse(call: Call<DeviceResponse>, response: Response<DeviceResponse>) {
                if (response.isSuccessful) {
                    val devices = response.body()?.items.orEmpty()
                    if (devices.isEmpty()) {
                        Toast.makeText(this@HomeIotActivity, "등록된 기기가 없습니다.", Toast.LENGTH_SHORT).show()
                        Log.w("SmartThings", "No devices found.")
                    } else {
                        displayDeviceList(devices)
                        Log.d("SmartThings", "Device list loaded successfully.")
                    }
                } else {
                    handleApiError(response.code(), response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<DeviceResponse>, t: Throwable) {
                showToast("네트워크 오류: 기기 목록을 가져올 수 없습니다.")
                Log.e("SmartThings", "Network error: ${t.message}")
            }
        })
    }

    // 2️⃣ RecyclerView에 기기 목록 표시
    private fun displayDeviceList(devices: List<Device>) {
        val deviceItems = devices.map { DeviceItem(it, isOnline = true) } // ✅ Device → DeviceItem 변환
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewDevices)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = DeviceAdapter(deviceItems) { device, command ->
            sendDeviceCommand(device.deviceId, "switch", command)
        }
    }

    // 3️⃣ SmartThings API를 통해 기기 제어 명령 전송
    private fun sendDeviceCommand(deviceId: String, capability: String, command: String) {
        val commandBody = CommandBody(commands = listOf(Command(capability, command)))

        RetrofitClient.instance.sendCommand(deviceId, commandBody, apiToken)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        showToast("명령이 성공적으로 전송되었습니다.")
                        Log.d("SmartThings", "Command sent successfully to deviceId: $deviceId")
                    } else {
                        handleApiError(response.code(), response.errorBody()?.string())
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    showToast("네트워크 오류: 명령을 전송할 수 없습니다.")
                    Log.e("SmartThings", "Error sending command: ${t.message}")
                }
            })
    }

    // 4️⃣ 음성 명령 처리
    private fun processVoiceCommand(command: String) {
        val devices = (findViewById<RecyclerView>(R.id.recyclerViewDevices).adapter as? DeviceAdapter)?.getDeviceList()

        devices?.forEach { device ->
            when {
                command.contains("조명 켜", ignoreCase = true) && device.label.contains("조명") ->
                    sendDeviceCommand(device.deviceId, "switch", "on")

                command.contains("조명 꺼", ignoreCase = true) && device.label.contains("조명") ->
                    sendDeviceCommand(device.deviceId, "switch", "off")
            }
        } ?: showToast("기기 목록을 불러오지 못했습니다.")
    }

    // 📢 API 오류 처리
    private fun handleApiError(code: Int, errorMessage: String?) {
        val message = when (code) {
            401 -> "인증 오류: API 토큰을 확인하세요."
            403 -> "권한 오류: 접근 권한이 없습니다."
            404 -> "요청한 데이터를 찾을 수 없습니다."
            else -> "알 수 없는 오류: ${errorMessage ?: "No details available."}"
        }
        showToast(message)
        Log.e("SmartThings", "API Error ($code): $errorMessage")
    }

    // 🚀 Toast 메시지 표시
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
