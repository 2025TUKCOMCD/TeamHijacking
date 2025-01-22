package com.example.front.audioguide

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.front.R
import java.util.UUID

class AudioGuideBLEControl : AppCompatActivity() {

    private lateinit var bluetoothGatt: BluetoothGatt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_guide_blecontrol)

        // 인텐트에서 BluetoothGatt 객체를 받아옵니다.
        val device: BluetoothDevice? = intent.getParcelableExtra("EXTRA_BLUETOOTH_DEVICE")
        device?.let {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            bluetoothGatt = it.connectGatt(this, false, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.d("Bluetooth", "GATT 연결 성공: ${gatt.device.name} - ${gatt.device.address}") // GATT 연결 성공
                        gatt.discoverServices() // 서비스 찾기 시작
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.d("Bluetooth", "GATT 연결 해제: ${gatt.device.name} - ${gatt.device.address}")
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d("Bluetooth", "서비스 검색 성공: ${gatt.device.name}")
                        for (service in gatt.services) {
                            Log.d("Bluetooth", "서비스: ${service.uuid}") // 서비스 출력
                            for (characteristic in service.characteristics) {
                                Log.d("Bluetooth", "  특성: ${characteristic.uuid}") // 특성 출력
                            }
                        }
                    } else {
                        Log.d("Bluetooth", "서비스 검색 실패: ${gatt.device.name}")
                    }
                }
            })
        }

        // 버튼 클릭 리스너 설정
        findViewById<Button>(R.id.button1).setOnClickListener {
            sendDataToCharacteristic(byteArrayOf(0x00, 0x31, 0x01))
        }

        findViewById<Button>(R.id.button2).setOnClickListener {
            sendDataToCharacteristic(byteArrayOf(0x00, 0x31, 0x10))
        }

        findViewById<Button>(R.id.button3).setOnClickListener {
            sendDataToCharacteristic(byteArrayOf(0x00, 0x31, 0x00))
        }
    }

    // 데이터 전송 함수
    private fun sendDataToCharacteristic(data: ByteArray) {
        val characteristic = getTargetCharacteristic()
        characteristic?.let {
            it.value = data
            bluetoothGatt.writeCharacteristic(it)
        }
    }

    // 원하는 특성을 찾는 함수
    private fun getTargetCharacteristic(): BluetoothGattCharacteristic? {
        // 여기서 원하는 특성 UUID를 사용하여 찾습니다.
        for (service in bluetoothGatt.services) {
            for (characteristic in service.characteristics) {
                if (characteristic.uuid == UUID.fromString("특정 특성 UUID")) {
                    return characteristic
                }
            }
        }
        return null
    }
}
