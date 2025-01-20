package com.example.front.audioguide

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.front.R
import java.io.IOException
import java.util.*

class AudioGuideBLEConnectActivity : AppCompatActivity() {
    private val bluetoothManager: BluetoothManager by lazy {
        getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }
    // 퍼미션 응답 처리 코드 아래에 권한 추가하면 권한 요청됩니다.
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN
    )

    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var deviceList: ArrayList<String>
    private lateinit var devices: ArrayList<BluetoothDevice>
    private lateinit var listView: ListView
    private lateinit var scanButton: Button
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                Log.d("Bluetooth", "블루투스 활성화")
                startDiscovery()
            } else if (it.resultCode == RESULT_CANCELED) {
                Log.d("Bluetooth", "블루투스 활성화 취소")
            }
        }

    // 퍼미션 체크 및 권한 요청 함수
    private fun checkPermissions() {
        // 거절되었거나 아직 수락하지 않은 권한(퍼미션)을 저장할 문자열 배열 리스트
        var rejectedPermissionList = ArrayList<String>()
        Log.d("현빈",rejectedPermissionList.toString())

        // 필요한 퍼미션들을 하나씩 끄집어내서 현재 권한을 받았는지 체크
        for (permission in requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //만약 권한이 없다면 rejectedPermissionList에 추가
                rejectedPermissionList.add(permission)
            }
        }
        // 거절된 퍼미션이 있다면 -> 권한 요청
        if (rejectedPermissionList.isNotEmpty()) {
            // 권한 요청!
            val array = arrayOfNulls<String>(rejectedPermissionList.size)
            ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array), 2)
            Log.d("현빈", "권한추가됨$this")
        } else {
            Toast.makeText(this, "Bluetooth Permission Success", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_guide_bleconnect)

        checkPermissions()

        listView = findViewById(R.id.listView)
        scanButton = findViewById(R.id.scanButton)
        deviceList = ArrayList()
        devices = ArrayList()
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        listView.adapter = arrayAdapter

        scanButton.setOnClickListener {
            if (bluetoothAdapter?.isEnabled == true) {
                makeDiscoverable()
                startDiscovery()
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activityResultLauncher.launch(enableBtIntent)
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            connectToDevice(devices[position])
            Log.d("Bluetooth", "기기 선택됨: ${devices[position].name} - ${devices[position].address}")
        }
    }

    private fun makeDiscoverable() {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300) // 300초 동안 탐색 가능 모드로 설정
        }
        startActivity(discoverableIntent)
    }

    private fun startDiscovery() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(receiver, filter)
        Log.d("현빈","탐색시작")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 1)
                return
            } else {
                Log.d("현빈", "권한 잘 부여되어있음")
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                return
            } else {
                Log.d("현빈", "권한 잘 부여되어있음")
            }
        }
        bluetoothAdapter?.let {
            if (it.isDiscovering) {
                it.cancelDiscovery()
            }
            val success = it.startDiscovery()
            Log.d("현빈", "블루투스 기기 탐색 시작됨: $success")
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("현빈","블루투스 탐색 시작")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("현빈","블루투스 탐색 종료")
                }
                BluetoothDevice.ACTION_FOUND -> {
                    Log.d("현빈","기기발견")
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        val deviceName = it.name ?: "Unknown"
                        val deviceAddress = it.address
                        deviceList.add("$deviceName - $deviceAddress")
                        devices.add(it)
                        arrayAdapter.notifyDataSetChanged()
                        Log.d("Bluetooth", "블루투스 기기 발견: $deviceName - $deviceAddress")
                        Toast.makeText(this@AudioGuideBLEConnectActivity, "발견된 기기: $deviceName", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothAdapter?.cancelDiscovery()
        val socket: BluetoothSocket?

        try {
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()
            Toast.makeText(this, "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
            Log.d("Bluetooth", "Connected to ${device.name} - ${device.address}")
        } catch (e: IOException) {
            Log.e("Bluetooth", "Connection failed", e)
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show()
            Log.d("Bluetooth", "Connection failed to ${device.name} - ${device.address}")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            // 권한별로 상태 확인
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]

                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permissions", "$permission 권한이 허용되었습니다.")
                } else {
                    Log.e("Permissions", "$permission 권한이 거부되었습니다.")
                    Toast.makeText(this, "$permission 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            // 모든 권한이 허용되었는지 확인
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startDiscovery()
            } else {
                Log.e("Bluetooth", "필요한 권한 중 일부가 거부되었습니다.")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        Log.d("Bluetooth", "BroadcastReceiver 해제")
    }
}
