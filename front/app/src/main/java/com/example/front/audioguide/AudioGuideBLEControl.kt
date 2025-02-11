package com.example.front.audioguide

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
                //UART 방식의 형태로 되어있어서 데이터를 넣는 부분과 받아오는부분 개발 필요
                binding.button1.setOnClickListener {
                    sendDataToCharacteristic(byteArrayOf(0x31, 0x00, 0x01))
                    Log.d("현빈", "위치 유도")
                }

                binding.button2.setOnClickListener {
                    sendDataToCharacteristic(byteArrayOf(0x31, 0x00, 0x02))
                    Log.d("현빈", "신호 안내")
                }

                binding.button3.setOnClickListener {
                    sendDataToCharacteristic(byteArrayOf(0x31, 0x00, 0x03))
                    Log.d("현빈", "음성 안내")
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
    //gatt연결을 시도하는 함수 gatt함수에 연결을 시도한 후에 성공하면 gatt.discoverServices()<-고유 함수로 서비스 및 특성을 탐색한다.
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
                //일단 임시코드로 만약 연결이 완료 된 상태에선 찾을 필요 없음 서비스 및 특성을 찾으면 uuid를 로그로 찍어주는 코드
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

                        // RX 특성 구독 - UART방식 소통중 데이터를 받아오는 코드 TX가 입력될때 바로 반환 되기때문에 실시간으로 감지 필요
                        val rxCharacteristic = gatt.getService(UUID.fromString("0003cdd0-0000-1000-8000-00805f9b0131"))
                            ?.getCharacteristic(UUID.fromString("0003cdd2-0000-1000-8000-00805f9b0131"))
                        rxCharacteristic?.let {
                            //gatt.setCharateristicNotification = "지금부터 이 gatt 함수 중 rxCharacteristic의 변화를 감지 하겠다는 코드
                            gatt.setCharacteristicNotification(it, true)
                            //CCCD ->( Client Charateristic Configuration Descriptor) 의 역할을 함 즉 특정 Characteristic의 Notification 및 Indication을 활성화 및 비활성화 하는 코드
                            val descriptor = it.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                            descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE //이게 0x0001 즉 Notification 활성화 코드
                            gatt.writeDescriptor(descriptor)
                        }
                    } else {
                        Log.d("Bluetooth", "서비스 검색 실패: ${gatt.device.name}")
                    }
                }
                //데이터 감지 함수 데이터가 바뀐다면
                override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                    if (characteristic.uuid == UUID.fromString("0003cdd0-0002-1000-8000-00805f9b0131")) {
                        val data = characteristic.value
                        Log.d("BluetoothControl", "데이터 수신: ${data.joinToString { byte -> String.format("%02x", byte) }}")

                        // 데이터 유효성 검사 및 처리
                        processReceivedData(data)
                    }
                }

                // 수신된 데이터를 처리하는 함수
                private fun processReceivedData(data: ByteArray) {
                    if (data.size == 3 && data[0] == 0x32.toByte()) {  // HEADER 확인 및 데이터 길이 검증
                        val opcode = data[1]
                        val payload = data[2]

                        when (payload.toInt() and 0xFF) {  // 부호 없는 정수로 변환 후 처리
                            0x00 -> {
                                Log.d("BluetoothControl", "ACK 수신 완료")
                                runOnUiThread{
                                    Toast.makeText(this@AudioGuideBLEControl, "ACK 수신 완료",Toast.LENGTH_SHORT).show()
                                }
                            }
                            0x01 -> {
                                Log.d("BluetoothControl", "NAK 수신 완료")
                                runOnUiThread{
                                    Toast.makeText(this@AudioGuideBLEControl, "NAK 수신 완료",Toast.LENGTH_SHORT).show()
                                }
                            }
                            in 0x10..0xF0 -> {
                                Log.d(
                                    "BluetoothControl",
                                    "ACK + 사양 정보: ${String.format("0x%02X", payload)}"
                                )
                                runOnUiThread {
                                    Toast.makeText(
                                        this@AudioGuideBLEControl,
                                        "ACK + 사양 정보: ${String.format("0x%02X", payload)}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            in 0x11..0xF1 -> {
                                Log.d("BluetoothControl", "NAK + 사양 정보: ${String.format("0x%02X", payload)}")
                                runOnUiThread{
                                    Toast.makeText(this@AudioGuideBLEControl, "NAK + 사양 정보: ${String.format("0x%02X", payload)}",Toast.LENGTH_SHORT).show()
                                }
                            }
                            else -> {
                                Log.w("BluetoothControl", "알 수 없는 데이터 수신: ${String.format("0x%02X", payload)}")
                                runOnUiThread{
                                    Toast.makeText(this@AudioGuideBLEControl, "잘못된 데이터 수신: ${String.format("0x%02X", payload)}}",Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Log.e("BluetoothControl", "잘못된 데이터 형식 또는 HEADER 오류: ${data.joinToString { byte -> String.format("%02x", byte) }}")
                        runOnUiThread{
                            Toast.makeText(this@AudioGuideBLEControl, "잘못된 데이터 형식 또는 HEADER 오류: ${data.joinToString{byte->String.format("%02x",byte)}}",Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            })
        } else {
            Log.d("BluetoothControl", "기존 GATT 연결 사용: ${bluetoothGatt?.device?.name} - ${bluetoothGatt?.device?.address}")
        }
    }

    // 데이터 전송 함수
    private fun sendDataToCharacteristic(data: ByteArray) {
        Log.d("현빈", "함수입성")
        bluetoothGatt?.let { gatt ->
            val txCharacteristic = gatt.getService(UUID.fromString("0003cdd0-0000-1000-8000-00805f9b0131"))
                ?.getCharacteristic(UUID.fromString("0003cdd1-0000-1000-8000-00805f9b0131")) // UART TX Characteristic UUID 사용
            txCharacteristic?.let {
                it.value = data
                val success = gatt.writeCharacteristic(it)
                if (success) {
                    Log.d("BluetoothControl", "데이터 전송 성공")
                } else {
                    Log.d("BluetoothControl", "데이터 전송 실패")
                }
            } ?: Log.d("BluetoothControl", "UART TX Characteristic 찾기 실패")
        } ?: Log.d("BluetoothControl", "bluetoothGatt 초기화되지 않음")
    }
}
