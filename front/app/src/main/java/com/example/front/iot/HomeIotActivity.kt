package com.example.front.iot

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.front.BuildConfig
import com.example.front.R
import com.example.front.iot.data.DeviceResponse
import com.example.front.iot.processor.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeIotActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_iot)

        // 스마트싱스 API 토큰키 연동 (local.properties에 키값 저장)
        val apiToken = "Bearer ${BuildConfig.SMARTTHINGS_API_TOKEN}"

        val apiService = RetrofitClient.instance
        apiService.getDevices(apiToken).enqueue(object : Callback<DeviceResponse> {
            override fun onResponse(call: Call<DeviceResponse>, response: Response<DeviceResponse>) {
                if (response.isSuccessful) {
                    val devices = response.body()?.items
                    devices?.forEach { device ->
                        Log.d("SmartThings", "Device: ${device.label}, Type: ${device.deviceTypeName}")
                    }
                } else {
                    Log.e("SmartThings", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<DeviceResponse>, t: Throwable) {
                Log.e("SmartThings", "Failure: ${t.message}")
            }
        })
    }
}
