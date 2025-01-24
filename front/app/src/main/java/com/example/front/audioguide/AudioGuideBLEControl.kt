package com.example.front.audioguide

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.front.databinding.ActivityAudioGuideBlecontrolBinding
import java.util.UUID

class AudioGuideBLEControl : AppCompatActivity() {

    private lateinit var binding: ActivityAudioGuideBlecontrolBinding
    private var bluetoothGatt: BluetoothGatt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAudioGuideBlecontrolBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("BluetoothControl", "화면 시작")

        val device: BluetoothDevice? = intent.getParcelableExtra("EXTRA_BLUETOOTH_DEVICE")
        Log.d("BluetoothControl", "객체 받아오기")
        Log.d("BluetoothControl", device.toString())

        device?.let {
            checkBluetoothPermission(it)
        }

        Log.d("현빈", "하고 있는건가3")
        binding.button1.setOnClickListener {
            Log.d("현빈", "클릭은 먹나?")
        }
        // 0.5초의 지연 후 클릭 리스너 설정
        Handler(Looper.getMainLooper()).postDelayed({
            if (bluetoothGatt != null) {
                binding.button1.setOnClickListener {
                    sendDataToCharacteristic(byteArrayOf(0x00, 0x31, 0x01))
                    Log.d("현빈", "위치 유도")
                }

                binding.button2.setOnClickListener {
                    sendDataToCharacteristic(byteArrayOf(0x00, 0x31, 0x10))
                    Log.d("현빈", "신호 안내")
                }

                binding.button3.setOnClickListener {
                    sendDataToCharacteristic(byteArrayOf(0x00, 0x31, 0x00))
                    Log.d("현빈", "신호 안내")
                }
            } else {
                Log.d("Bluetooth", "bluetoothGatt 초기화되지 않음")
            }
        }, 500)
    }

    private fun checkBluetoothPermission(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한 요청을 처리합니다.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
            Log.d("BluetoothControl", "권한 요청")
            connectToBluetoothGatt(device)
        } else {
            Log.d("BluetoothControl", "권한 있음")
            connectToBluetoothGatt(device)
        }
    }

    private fun connectToBluetoothGatt(device: BluetoothDevice) {
        if (bluetoothGatt == null || bluetoothGatt?.device?.address != device.address) {
            bluetoothGatt = device.connectGatt(this, false, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.d("BluetoothControl", "GATT 연결 성공: ${gatt.device.name} - ${gatt.device.address}") // GATT 연결 성공
                        Log.d("현빈", "하고 있는건가1")
                        gatt.discoverServices() // 서비스 찾기 시작
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.d("BluetoothControl", "GATT 연결 해제: ${gatt.device.name} - ${gatt.device.address}")
                        bluetoothGatt = null
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d("BluetoothControl", "서비스 검색 성공: ${gatt.device.name}")
                        Log.d("현빈", "하고 있는건가2")
                        for (service in gatt.services) {
                            Log.d("BluetoothControl", "서비스: ${service.uuid}") // 서비스 출력
                            for (characteristic in service.characteristics) {
                                Log.d("BluetoothControl", "  특성: ${characteristic.uuid}") // 특성 출력
                            }
                        }
                    } else {
                        Log.d("Bluetooth", "서비스 검색 실패: ${gatt.device.name}")
                    }
                }
            })
        } else {
            Log.d("BluetoothControl", "기존 GATT 연결 사용: ${bluetoothGatt?.device?.name} - ${bluetoothGatt?.device?.address}")
        }
    }

    // 데이터 전송 함수
    private fun sendDataToCharacteristic(data: ByteArray) {
        Log.d("BluetoothControl", "함수입성")
        bluetoothGatt?.let { gatt ->
            Log.d("BluetoothControl", "함수 실행전")
            val characteristic = getTargetCharacteristic()
            Log.d("BluetoothControl", "변수지정")
            Log.d("BluetoothControl", "characteristic: $characteristic")
            if (characteristic != null) {
                characteristic.value = data
                val success = gatt.writeCharacteristic(characteristic)
                Log.d("BluetoothControl", "데이터 전송 시도: $success")
                if (success) {
                    Log.d("BluetoothControl", "데이터 전송 성공")
                } else {
                    Log.d("BluetoothControl", "데이터 전송 실패")
                }
            } else {
                Log.d("BluetoothControl", "특성 찾기 실패")
            }
        } ?: Log.d("BluetoothControl", "bluetoothGatt 초기화되지 않음")
    }

    // 원하는 특성을 찾는 함수
    private fun getTargetCharacteristic(): BluetoothGattCharacteristic? {
        Log.d("BluetoothControl", "Target함수쪽으로 들어옴")
        // 여기서 원하는 특성 UUID를 사용하여 찾습니다.
        val targetUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb") // 실제 UUID 값으로 변경

        Log.d("BluetoothControl", "UUID지정")
        bluetoothGatt?.let { gatt ->
            Log.d("BluetoothControl", "GATT 서비스 수: ${gatt.services.size}")
            for (service in gatt.services) {
                Log.d("BluetoothControl", "서비스: ${service.uuid}")
                for (characteristic in service.characteristics) {
                    Log.d("BluetoothControl", "  특성: ${characteristic.uuid}")
                    if (characteristic.uuid == targetUUID) {
                        return characteristic
                    }
                }
            }
        }
        return null
    }
}
