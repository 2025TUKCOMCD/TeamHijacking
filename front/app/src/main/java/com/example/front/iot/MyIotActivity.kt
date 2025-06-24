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
import com.example.front.presentation.userid
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyIotActivity : AppCompatActivity() {

    private lateinit var deviceControlHelper: DeviceControlHelper
    private lateinit var deviceAdapter: DeviceAdapter
    private val deviceList = mutableListOf<Device>()
    private val tokenapiService = RetrofitClient.tokenapiService
    private var apiToken:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        val app = this.applicationContext as? userid
        Log.d("현빈", app!!.receivedMessage)
        getSmartThingsToken(app.receivedMessage) { receivedToken ->
            // 이 람다 블록은 토큰을 비동기적으로 가져오는 작업이 완료된 후 실행됩니다.
            if (receivedToken != null) {
                // 토큰을 성공적으로 받은 경우
                apiToken = "Bearer $receivedToken"
                Log.d("현빈", "SmartThings API 토큰: $apiToken")
                deviceControlHelper = DeviceControlHelper(apiToken)
                fetchDeviceList()

                // 이제 이 apiToken을 사용하여 SmartThings 디바이스 제어 API를 호출할 수 있습니다.
                // 예: callSmartThingsDeviceCommand(apiToken)
                // UI 업데이트 (예: TextView에 토큰 표시) 등도 여기서 수행 가능
            } else {
                // 토큰을 가져오는 데 실패한 경우
                Log.e("현빈", "SmartThings 토큰을 가져오지 못했습니다.")
                // 사용자에게 오류 메시지를 표시하는 등의 적절한 에러 처리
                fetchDeviceList()
            }
        }
        Log.d("현빈", "oncreate 들어옴")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_iot)
        Log.d("현빈", "activity 할당")
        Log.d("현빈", "토큰 할당함")

        // RecyclerView 설정
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMyDevices)
        deviceAdapter = DeviceAdapter(deviceList) { device ->
            val name = device.label.lowercase()

            Log.d("DeviceClick", "클릭된 기기 label = ${device.label}")

            when {
                name.contains("galaxy home") -> {
                    Log.d("DeviceClick", "AI 스피커 인식")
                    showGalaxyHomeMiniControl(device)
                }
                name.contains("hejhome") || name.contains("무드등") || name.contains("mood light") -> {
                    Log.d("DeviceClick", "무드등 인식")
                    showRgbColorBulbControl(device)
                }
                else -> {
                    Log.w("DeviceClick", "분기 실패 → 지원 안 함: $name")
                    Toast.makeText(this, "지원 안함", Toast.LENGTH_SHORT).show()
                }
            }
        }


        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = deviceAdapter
        Log.d("현빈", "디바이스 불러오기 전")

        //디바이스 목록 불러오기
    }

    private fun fetchDeviceList() {
        val apiService = RetrofitClient.instance
        Log.d("현빈", "레트로핏 서비스 연결")
        Log.d("현빈", apiToken)

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
    private fun showGalaxyHomeMiniControl(device: Device) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_ai_speaker, null)
        val statusText = view.findViewById<TextView>(R.id.textSpeakerStatus)
        val btnPlayPause = view.findViewById<Button>(R.id.btnPlayPause)
        val btnVolumeUp = view.findViewById<Button>(R.id.btnVolumeUp)
        val btnVolumeDown = view.findViewById<Button>(R.id.btnVolumeDown)

        statusText.text = "기기 이름: ${device.label}\n상태 확인 중..."

        deviceControlHelper.getDeviceStatus(
            device.deviceId,
            onSuccess = { status ->
                val main = status.components["main"]
                val playback = main?.mediaPlayback
                val volume = main?.audioVolume

                val isPlaying = playback?.playbackStatus?.value.equals("playing", ignoreCase = true)
                val currentVolume = volume?.volume?.value?.toIntOrNull() ?: -1

                statusText.text = buildString {
                    append("재생 상태: ${playback?.playbackStatus?.value ?: "알 수 없음"}\n")
                    append("볼륨: ${if (currentVolume >= 0) "$currentVolume%" else "정보 없음"}")
                }

                btnPlayPause.setOnClickListener {
                    val command = if (isPlaying) "pause" else "play"
                    deviceControlHelper.sendMediaCommand(
                        device.deviceId,
                        command,
                        onSuccess = {
                            Toast.makeText(this@MyIotActivity, "명령 실행됨: $command", Toast.LENGTH_SHORT).show()
                        },
                        onError = {
                            Toast.makeText(this@MyIotActivity, "재생 제어 실패", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                btnVolumeUp.setOnClickListener {
                    val newVolume = (currentVolume + 10).coerceAtMost(100)
                    deviceControlHelper.setVolume(
                        device.deviceId, newVolume,
                        onSuccess = {
                            Toast.makeText(this@MyIotActivity, "볼륨 ↑", Toast.LENGTH_SHORT).show()
                        },
                        onError = {
                            Toast.makeText(this@MyIotActivity, "볼륨 조절 실패", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                btnVolumeDown.setOnClickListener {
                    val newVolume = (currentVolume - 10).coerceAtLeast(0)
                    deviceControlHelper.setVolume(
                        device.deviceId, newVolume,
                        onSuccess = {
                            Toast.makeText(this@MyIotActivity, "볼륨 ↓", Toast.LENGTH_SHORT).show()
                        },
                        onError = {
                            Toast.makeText(this@MyIotActivity, "볼륨 조절 실패", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

            },
            onError = { error ->
                statusText.text = "상태 불러오기 실패: $error"
                Toast.makeText(this, "기기 상태 가져오기 실패", Toast.LENGTH_SHORT).show()
            }
        )

        AlertDialog.Builder(this)
            .setTitle("AI 스피커 제어")
            .setView(view)
            .setNeutralButton("닫기", null)
            .show()
    }




    //무드등 제어 만약 device이름이 c2c-rgb-color-bulb이라면 여기로 이동시키면 됨 추후에 넣어야 할듯
    private fun showRgbColorBulbControl(device: Device) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_rgb_light, null)
        val statusText = view.findViewById<TextView>(R.id.textDeviceStatus)
        val btnBrightnessUp = view.findViewById<Button>(R.id.btnBrightnessUp)
        val btnBrightnessDown = view.findViewById<Button>(R.id.btnBrightnessDown)
        val btnSaturationUp = view.findViewById<Button>(R.id.btnSaturationUp)
        val btnSaturationDown = view.findViewById<Button>(R.id.btnSaturationDown)
        val btnTogglePower = view.findViewById<Button>(R.id.btnTogglePower)
        val textPowerStatus = view.findViewById<TextView>(R.id.textPowerStatus)
        val textBrightnessStatus = view.findViewById<TextView>(R.id.textBrightnessStatus)

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
                    Log.d("현빈", switchValue.toString())
                    isPowerOn = switchValue.equals("on", ignoreCase = true)
                    if (switchValue != null) {
                        Log.d("현빈", brightnessValue.toString())
                        textPowerStatus.text = "전원 상태: ${switchValue}"
                    } else {
                        textPowerStatus.text = "전원 상태: 정보 없음"
                    }




                    val level = mainComponent.switchLevels?.level?.value?.toIntOrNull()
                    if (level != null) {
                        brightnessValue = level
                        Log.d("현빈", brightnessValue.toString())
                        textBrightnessStatus.text = "현재 밝기: ${brightnessValue}%"
                    } else {
                        textBrightnessStatus.text = "현재 밝기: 정보 없음"
                    }


                    val saturation = mainComponent.colorControl?.saturation?.value?.toInt()
                    val textSaturationStatus = view.findViewById<TextView>(R.id.textSaturationStatus)
                    if (saturation != null) {
                        saturationValue = saturation
                        textSaturationStatus.text = "현재 채도: ${saturationValue}%"
                    } else {
                        textSaturationStatus.text = "현재 채도: 정보 없음"
                    }
                } else {
                    Toast.makeText(this, "Main 컴포넌트 없음", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(this, "상태 불러오기 실패: $error", Toast.LENGTH_SHORT).show()
                Log.d("현빈", error.toString())
            }
        )

        btnTogglePower.setOnClickListener {
            deviceControlHelper.sendSwitchCommand(
                device.deviceId, !isPowerOn,
                onSuccess = {
                    Toast.makeText(this, if (isPowerOn) "전원 OFF" else "전원 ON", Toast.LENGTH_SHORT).show()
                    isPowerOn = !isPowerOn
                    if (isPowerOn){
                        textPowerStatus.text = "전원 상태: on"
                    }
                    else{
                        textPowerStatus.text = "전원 상태: off"
                    }
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

    fun getSmartThingsToken(userId: String, callback: (String?) -> Unit) {
        tokenapiService.getSmartThingsToken(userId).enqueue(object : Callback<SmartThingsRequest> {
            override fun onResponse(call: Call<SmartThingsRequest>, response: Response<SmartThingsRequest>) {
                if (response.isSuccessful) {
                    Log.d("UserProcessor", "토큰 조회 성공: ${response.body()}")
                    callback(response.body()?.accessToken)
                } else {
                    Log.e("UserProcessor", "토큰 조회 실패 - 서버 오류: ${response.code()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<SmartThingsRequest>, t: Throwable) {
                Log.e("UserProcessor", "토큰 조회 실패 - 통신 오류: ${t.message}")
                callback(null)
            }
        })
    }
}
