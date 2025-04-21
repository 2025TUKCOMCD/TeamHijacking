package com.example.front.iot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
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

    private lateinit var apiToken: String
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_iot)

        apiToken = "Bearer ${BuildConfig.SMARTTHINGS_API_TOKEN}"
        recyclerView = findViewById(R.id.recyclerViewMyDevices)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchDeviceList()
    }

    private fun fetchDeviceList() {
        RetrofitClient.instance.getDevices(apiToken).enqueue(object : Callback<DeviceResponse> {
            override fun onResponse(call: Call<DeviceResponse>, response: Response<DeviceResponse>) {
                if (response.isSuccessful) {
                    val devices = response.body()?.items.orEmpty()
                    val adapter = MyDeviceAdapter(devices) { device ->
                        showDeviceDetailDialog(device)
                    }
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(this@MyIotActivity, "기기 목록을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeviceResponse>, t: Throwable) {
                Toast.makeText(this@MyIotActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeviceDetailDialog(device: Device) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_device_detail, null)
        val statusText = view.findViewById<TextView>(R.id.textDeviceStatus)
        val seekBarBrightness = view.findViewById<SeekBar>(R.id.seekBarBrightness)
        val seekBarSaturation = view.findViewById<SeekBar>(R.id.seekBarSaturation)

        statusText.text = "기기 이름: ${device.label}\n기기 ID: ${device.deviceId}"

        seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setBrightness(device.deviceId, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarSaturation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setColor(device.deviceId, 50, progress) // Hue는 고정값 예시
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        AlertDialog.Builder(this)
            .setTitle("기기 상태 및 제어")
            .setView(view)
            .setPositiveButton("전원 ON") { _, _ ->
                sendDeviceCommand(device.deviceId, "switch", "on")
            }
            .setNegativeButton("전원 OFF") { _, _ ->
                sendDeviceCommand(device.deviceId, "switch", "off")
            }
            .setNeutralButton("닫기", null)
            .show()
    }

    private fun sendDeviceCommand(deviceId: String, capability: String, command: String) {
        val commandBody = CommandBody(commands = listOf(Command(capability, command)))
        RetrofitClient.instance.sendCommand(deviceId, commandBody, apiToken)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MyIotActivity, "명령 전송 완료", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MyIotActivity, "명령 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Toast.makeText(this@MyIotActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setBrightness(deviceId: String, level: Int) {
        val commandBody = CommandBody(commands = listOf(Command("switchLevel", level.toString())))
        RetrofitClient.instance.setBrightness(deviceId, commandBody, apiToken).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                Toast.makeText(this@MyIotActivity, "밝기 설정 완료", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(this@MyIotActivity, "밝기 설정 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setColor(deviceId: String, hue: Int, saturation: Int) {
        val colorJson = "{" + "\"hue\":$hue,\"saturation\":$saturation" + "}"
        val commandBody = CommandBody(commands = listOf(Command("colorControl", colorJson)))
        RetrofitClient.instance.setColor(deviceId, commandBody, apiToken).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                Toast.makeText(this@MyIotActivity, "색상 설정 완료", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(this@MyIotActivity, "색상 설정 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
