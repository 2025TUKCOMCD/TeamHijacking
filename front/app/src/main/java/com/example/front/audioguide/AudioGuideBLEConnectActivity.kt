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

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_guide_bleconnect)

        listView = findViewById(R.id.listView)
        scanButton = findViewById(R.id.scanButton)
        deviceList = ArrayList()
        devices = ArrayList()
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        listView.adapter = arrayAdapter

        if (bluetoothAdapter == null) {
            Log.d("Bluetooth", "블루투스를 지원하지 않는 장비입니다.")
            finish()
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN), 1)
        }

        scanButton.setOnClickListener {
            if (bluetoothAdapter?.isEnabled == true) {
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

    private fun startDiscovery() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 1)
            return
        }
        bluetoothAdapter?.let {
            if (it.isDiscovering) {
                it.cancelDiscovery()
            }
            val success = it.startDiscovery()
            Log.d("현빈", "블루투스 기기 탐색 시작됨: $success")
            Log.d("Bluetooth", "블루투스 기기 탐색 시작됨")
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
//            when(action) {
//                BluetoothDevice.ACTION_FOUND -> {
//                    // Discovery has found a device. Get the BluetoothDevice
//                    // object and its info from the Intent.
//                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
//                    val deviceName = device.name
//                    Log.d("현빈",deviceName.toString())
//                    val deviceHardwareAddress = device.address // MAC address
//                    Log.d("현빈", deviceHardwareAddress.toString())
//                }
//            }
            if (BluetoothDevice.ACTION_FOUND == action) {
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
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                startDiscovery()
            } else {
                Log.e("Bluetooth", "블루투스 권한이 거부되었습니다.")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        Log.d("Bluetooth", "BroadcastReceiver 해제")
    }
}
