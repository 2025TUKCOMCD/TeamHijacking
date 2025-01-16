package com.example.front.iot

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        // 스마트싱스 API 토큰키 연동 (local.properties에 키값 저장)
        val apiToken = "Bearer ${BuildConfig.SMARTTHINGS_API_TOKEN}"
        deviceControlHelper = DeviceControlHelper(apiToken)
        voiceControlHelper = VoiceControlHelper(this) { command ->
            processVoiceCommand(command)
        }

        //기기 목록 가져오기
        fetchDeviceList()

        //음성 명령 버튼
        findViewById<Button>(R.id.btnVoiceControl).setOnClickListener {
            voiceControlHelper.startVoiceRecognition()
        }
    }



    private fun fetchDeviceList() {
        val apiService = RetrofitClient.instance
        apiService.getDevices(apiToken).enqueue(object : retrofit2.Callback<DeviceResponse> {
            override fun onResponse(call: Call<DeviceResponse>, response: Response<DeviceResponse>) {
                if (response.isSuccessful) {
                    val devices = response.body()?.items.orEmpty()
                    if (devices.isEmpty()) {
                        Toast.makeText(this@HomeIotActivity, "등록된 기기가 없습니다.", Toast.LENGTH_SHORT)
                            .show()
                        Log.w("SmartThings", "No devices found")
                    } else {
                        displayDeviceList(deivces)
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@HomeIotActivity, "기기 목록을 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("SmartThings", "Failed to fetch devices: $errorMessage")
                }
            }

            override fun onFailure(call: Call<DeviceResponse>, t: Throwable) {
                Toast.makeText(this@HomeIotActivity, "네트워크 오류: 기기 목록을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                Log.e("SmartThings", "Network error: ${t.message}")
            }
        })
    }


}
