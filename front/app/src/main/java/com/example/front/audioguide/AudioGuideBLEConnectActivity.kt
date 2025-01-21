package com.example.front.audioguide

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
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

class AudioGuideBLEConnectActivity : AppCompatActivity() {
    private val bluetoothManager: BluetoothManager by lazy {
        getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var deviceList: ArrayList<String>
    private lateinit var devices: ArrayList<BluetoothDevice>
    private lateinit var listView: ListView
    private lateinit var scanButton: Button
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN
    )

    private val activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                Log.d("Bluetooth", "블루투스 활성화")
                startDiscovery()
            } else if (it.resultCode == RESULT_CANCELED) {
                Log.d("Bluetooth", "블루투스 활성화 취소")
            }
        }

    private fun checkPermissions() {
        val rejectedPermissionList = ArrayList<String>()
        for (permission in requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                rejectedPermissionList.add(permission)
            }
        }
        if (rejectedPermissionList.isNotEmpty()) {
            val array = arrayOfNulls<String>(rejectedPermissionList.size)
            ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array), 2)
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
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 1)
                return
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                return
            }
        }
        bluetoothAdapter?.let {
            if (it.isDiscovering) {
                it.cancelDiscovery()
            }
            it.startDiscovery()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("Bluetooth", "블루투스 탐색 시작")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("Bluetooth", "블루투스 탐색 종료")
                }
                BluetoothDevice.ACTION_FOUND -> {
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

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("Bluetooth", "GATT 연결 성공: ${gatt.device.name} - ${gatt.device.address}")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("Bluetooth", "GATT 연결 해제: ${gatt.device.name} - ${gatt.device.address}")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Bluetooth", "서비스 검색 성공: ${gatt.device.name}")
                for (service in gatt.services) {
                    Log.d("Bluetooth", "서비스: ${service.uuid}")
                    for (characteristic in service.characteristics) {
                        Log.d("Bluetooth", "  특성: ${characteristic.uuid}")
                    }
                }
            } else {
                Log.d("Bluetooth", "서비스 검색 실패: ${gatt.device.name}")
            }
        }
    }


    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothAdapter?.cancelDiscovery()
        device.connectGatt(this, false, bluetoothGattCallback)
        Toast.makeText(this, "${device.name}에 연결 시도 중입니다", Toast.LENGTH_SHORT).show()
        Log.d("Bluetooth", "${device.name} - ${device.address}에 연결 시도 중입니다")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
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
