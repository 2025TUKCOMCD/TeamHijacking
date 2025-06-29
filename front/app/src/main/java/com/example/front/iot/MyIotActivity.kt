package com.example.front.iot

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.front.BuildConfig
import com.example.front.R
import com.example.front.iot.SmartHome.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyIotActivity : AppCompatActivity() {

    private lateinit var deviceControlHelper: DeviceControlHelper
    private lateinit var deviceAdapter: DeviceAdapter
    private val deviceList = mutableListOf<Device>()
    private val apiToken = "Bearer ${BuildConfig.SMARTTHINGS_API_TOKEN}"

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("현빈", "oncreate 들어옴")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_iot)
        Log.d("현빈", "activity 할당")
        deviceControlHelper = DeviceControlHelper(apiToken)
        Log.d("현빈", "토큰 할당함")

        // RecyclerView 설정
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMyDevices)
        deviceAdapter = DeviceAdapter(deviceList) { device ->
            when(device.label){
                "Galaxy Home Mini (3NPH)" -> {showGalaxyHomeMiniControl(device)}
                "c2c-rgb-color-bulb" -> {showRgbColorBulbControl(device)}
                "Hejhome Smart Mood Light" -> {showRgbColorBulbControl(device)}
                // 무드등 추가 예정
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = deviceAdapter
        Log.d("현빈", "디바이스 불러오기 전")

        //디바이스 목록 불러오기
        fetchDeviceList()
    }

    private fun fetchDeviceList() {
        val apiService = RetrofitClient.instance
        Log.d("현빈", "레트로핏 서비스 연결")

        apiService.getDevices(apiToken).enqueue(object : Callback<DeviceResponse> {

        override fun onResponse(call: Call<DeviceResponse>, response: Response<DeviceResponse>) {
            if (response.isSuccessful) {
                val devices = response.body()?.items ?: emptyList()
                deviceList.clear()
                deviceList.addAll(devices)
                Log.d("현빈", deviceList[0].name)
                deviceAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this@MyIotActivity, "기기 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<DeviceResponse>, t: Throwable) {
            Toast.makeText(this@MyIotActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    })
    }
    private fun showGalaxyHomeMiniControl(device: Device){

    }



    //무드등 제어 만약 device이름이 c2c-rgb-color-bulb이라면 여기로 이동시키면 됨 추후에 넣어야 할듯
    private fun showRgbColorBulbControl(device: Device) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_device_detail, null)
        val statusText = view.findViewById<TextView>(R.id.textDeviceStatus)
        val btnBrightnessUp = view.findViewById<Button>(R.id.btnBrightnessUp)
        val btnBrightnessDown = view.findViewById<Button>(R.id.btnBrightnessDown)
        val btnSaturationUp = view.findViewById<Button>(R.id.btnSaturationUp)
        val btnSaturationDown = view.findViewById<Button>(R.id.btnSaturationDown)
        val btnTogglePower = view.findViewById<Button>(R.id.btnTogglePower)

        statusText.text = "기기 이름: ${device.label}\n기기 ID: ${device.deviceId}"

        var isPowerOn = false

        var brightnessValue = 50
        var saturationValue = 50

        deviceControlHelper.getDeviceStatus(
            device.deviceId,
            onSuccess = { status ->
                val mainComponent = status.components["main"]

                if (mainComponent != null) {
                    val switchValue = mainComponent.switch?.switch?.value
                    isPowerOn = switchValue.equals("on", ignoreCase = true)

                    //val brightnessValue = mainComponent.switchLevel?.value?.toIntOrNull()
                    //brightnessValue?.let {
                        //seekBarBrightness.progress = it
                    //}

                    //val saturationValue = mainComponent.colorControl?.saturation?.value?.toInt()
                    //saturationValue?.let {
                        //seekBarSaturation.progress = it
                    //}
                } else {
                    Toast.makeText(this, "Main 컴포넌트 없음", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(this, "상태 불러오기 실패: $error", Toast.LENGTH_SHORT).show()
            }
        )

        btnTogglePower.setOnClickListener {
            deviceControlHelper.sendSwitchCommand(
                device.deviceId, !isPowerOn,
                onSuccess = {
                    Toast.makeText(this, if (isPowerOn) "전원 OFF" else "전원 ON", Toast.LENGTH_SHORT).show()
                    isPowerOn = !isPowerOn
                },
                onError = {
                    Toast.makeText(this, "전원 제어 실패", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 밝기, 채도 각각 +- 버튼 총 4개 만듦. 추후, 테스트 예정
        btnBrightnessUp.setOnClickListener {
            brightnessValue = (brightnessValue + 10).coerceAtMost(100)
            deviceControlHelper.setBrightness(device.deviceId, brightnessValue,
                onSuccess = {},
                onError = { Toast.makeText(this, "밝기 조절 실패", Toast.LENGTH_SHORT).show() }
            )
        }

        btnBrightnessDown.setOnClickListener {
            brightnessValue = (brightnessValue - 10).coerceAtLeast(0)
            deviceControlHelper.setBrightness(device.deviceId, brightnessValue,
                onSuccess = {},
                onError = { Toast.makeText(this, "밝기 조절 실패", Toast.LENGTH_SHORT).show() }
            )
        }

        btnSaturationUp.setOnClickListener {
            saturationValue = (saturationValue + 10).coerceAtMost(100)
            deviceControlHelper.setColorWithAutoMode(device.deviceId, hue = 50, saturation = saturationValue,
                onSuccess = {},
                onError = { Toast.makeText(this, "채도 조절 실패", Toast.LENGTH_SHORT).show() }
            )
        }

        btnSaturationDown.setOnClickListener {
            saturationValue = (saturationValue - 10).coerceAtLeast(0)
            deviceControlHelper.setColorWithAutoMode(device.deviceId, hue = 50, saturation = saturationValue,
                onSuccess = {},
                onError = { Toast.makeText(this, "채도 조절 실패", Toast.LENGTH_SHORT).show() }
            )
        }

        AlertDialog.Builder(this)
            .setTitle("기기 상태 및 제어")
            .setView(view)
            .setNeutralButton("닫기", null)
            .show()
    }
}
