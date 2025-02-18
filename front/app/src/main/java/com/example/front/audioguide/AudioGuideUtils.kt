package com.example.front.audioguide

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.util.UUID


//사용할 서비스 및 특성을 기능에 따라 변수 이름 지정
private var serviceUUID = UUID.fromString("0003cdd0-0000-1000-8000-00805f9b0131")
private var txCharacteristicUUID = UUID.fromString("0003cdd2-0000-1000-8000-00805f9b0131")
private var rxCharacteristicUUID = UUID.fromString("0003cdd1-0000-1000-8000-00805f9b0131")
private var cccdUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")


var bluetoothGatt: BluetoothGatt? = null

//----------------------------권한 요청용 코드 -------------------------------
//필요한 권한들을 쭉 적어놓음 나중에 사용 예정
//api30을 기준으로 요구해야 하는 permission 구분
private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_ADMIN,  //관리자 정도의 기능 제공 일단 ADMIN으로 씀
        //  Manifest.permission.BLUETOOTH,  //간단한 기능 제공
        // Manifest.permission.ACCESS_FINE_LOCATION // 필요에 따라 추가
    )
} else {
    arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_ADMIN
        //Manifest.permission.BLUETOOTH,
    )
}

/*
기능 1. 블루투스 권한을 체크해 준다. 크게 Api30을 기준으로 각각 요구하는 권한에 대해 요청하고 또 받아온다.
기능 2. 권한을 다 검사 한 후 권한이 다 부여되어있다면 true 값을 아니라면 false 값을 반환한다.
 */
fun checkPermissions(activity: Activity) : Boolean {
    val rejectedPermissionList = ArrayList<String>()
    //권한들을 하나씩 검사
    for (permission in requiredPermissions) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                permission
            ) != PackageManager.PERMISSION_GRANTED   //만약 권한이 부여되어있지 않다면
        ) {
            Log.d("bluetoothConnect", permission.toString())
            rejectedPermissionList.add(permission) //rejectedPermissionList에 추가해 둠
        }
    }
    if (rejectedPermissionList.isNotEmpty()) {  //만약 거부된 권한 이 있다면
        val array = arrayOfNulls<String>(rejectedPermissionList.size)
        Log.d("bluetoothConnect", "권한요청")
        ActivityCompat.requestPermissions(activity, rejectedPermissionList.toArray(array), 2)  //권한을 요청함
        return false
    } else {
        return true  // 거부된 권한이 없다면 블루투스 권한성공을 출력
    }
}

/*
기능 1. 기기를 클릭했을 시에 AudioGuideBLEControl.kt 로 화면 이동을 시켜주는 함수
 */
fun navigateToNextActivity(device: BluetoothDevice,activity: Activity) {
    val intent = Intent(activity, AudioGuideBLEControl::class.java)
    intent.putExtra("EXTRA_BLUETOOTH_DEVICE", device)
    Log.d("bludtooth", "navigatetonext호출")
    activity.startActivity(intent)
}

/*
기능 1. Uart방식중 tx 부분을 담당해 준다. 특정 uuid의 특성(여기선 tx)에 데이터를 입력 해 주는 역할을 한다.
 */
fun sendDataToCharacteristic(data: ByteArray, bluetoothGatt : BluetoothGatt) {
    Log.d("현빈", "함수입성")
    bluetoothGatt.let { gatt ->
        val txCharacteristic = gatt.getService(serviceUUID)
            ?.getCharacteristic(txCharacteristicUUID) // UART TX Characteristic UUID 사용
        txCharacteristic?.let {
            it.value = data
            val success = gatt.writeCharacteristic(it)
            if (success) {
                Log.d("BluetoothControl", "데이터 전송 성공")
            } else {
                Log.d("BluetoothControl", "데이터 전송 실패")
            }
        } ?: Log.d("BluetoothControl", "UART TX Characteristic 찾기 실패")
    }
}


/*
기능 1. bluetooth 기기의 bluetoothGatt 연결을 시도한다. 실패시 재시도
기능 2. bluetooth 기기의 기능중 Uart 방식 의 rx 부분에 대해 데이터 변화를 감지 해 준다.
기능 3. bluetooth 기기의 기능중 rx 데이터 변화가 감지 되면 Toast 및 로그로 그 결과를 출력 해 준다.
 */
fun connectToBluetoothGatt(device: BluetoothDevice, activity: Activity) {
    if (bluetoothGatt == null || bluetoothGatt?.device?.address != device.address) {
        bluetoothGatt = device.connectGatt(activity, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BluetoothControl", "GATT 연결 성공: ${gatt.device.name} - ${gatt.device.address}") // GATT 연결 성공
                    Log.d("현빈", "하고 있는건가1")
                    gatt.discoverServices() // 서비스 찾기 시작
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BluetoothControl", "GATT 연결 해제: ${gatt.device.name} - ${gatt.device.address} 및 재연결 시도" )
                    bluetoothGatt = null
                    connectToBluetoothGatt(device, activity)
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
                    val rxCharacteristic = gatt.getService(serviceUUID)
                        ?.getCharacteristic(rxCharacteristicUUID)
                    rxCharacteristic?.let {
                        //gatt.setCharateristicNotification = "지금부터 이 gatt 함수 중 rxCharacteristic의 변화를 감지 하겠다는 코드
                        gatt.setCharacteristicNotification(it, true)
                        //CCCD ->( Client Charateristic Configuration Descriptor) 의 역할을 함 즉 특정 Characteristic의 Notification 및 Indication을 활성화 및 비활성화 하는 코드
                        val descriptor = it.getDescriptor(cccdUUID)
                        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE //이게 0x0001 즉 Notification 활성화 코드
                        gatt.writeDescriptor(descriptor)
                    }
                } else {
                    Log.d("Bluetooth", "서비스 검색 실패: ${gatt.device.name}")
                }
            }
            //데이터 감지 함수 데이터가 바뀐다면
            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                if (characteristic.uuid == rxCharacteristicUUID) {
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
                            Toast.makeText(activity, "ACK 수신 완료",Toast.LENGTH_SHORT).show()
                        }
                        0x01 -> {
                            Log.d("BluetoothControl", "NAK 수신 완료")
                            Toast.makeText(activity, "NAK 수신 완료",Toast.LENGTH_SHORT).show()
                        }
                        in 0x10..0xF0 -> {
                            Log.d("BluetoothControl", "ACK + 사양 정보: ${String.format("0x%02X", payload)}"
                            )
                            Toast.makeText(activity, "ACK + 사양 정보: ${String.format("0x%02X", payload)}", Toast.LENGTH_SHORT).show()
                        }
                        in 0x11..0xF1 -> {
                            Log.d("BluetoothControl", "NAK + 사양 정보: ${String.format("0x%02X", payload)}")
                            Toast.makeText(activity, "NAK + 사양 정보: ${String.format("0x%02X", payload)}",Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Log.w("BluetoothControl", "알 수 없는 데이터 수신: ${String.format("0x%02X", payload)}")
                            Toast.makeText(activity, "잘못된 데이터 수신: ${String.format("0x%02X", payload)}}",Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("BluetoothControl", "잘못된 데이터 형식 또는 HEADER 오류: ${data.joinToString { byte -> String.format("%02x", byte) }}")
                    Toast.makeText(activity, "잘못된 데이터 형식 또는 HEADER 오류: ${data.joinToString{byte->String.format("%02x",byte)}}",Toast.LENGTH_SHORT).show()
                }
            }

        })
    } else {
        Log.d("BluetoothControl", "기존 GATT 연결 사용: ${bluetoothGatt?.device?.name} - ${bluetoothGatt?.device?.address}")
    }
}