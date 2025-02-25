package com.example.front.audioguide

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityAudioGuideBleconnectBinding
import java.util.*

class AudioGuideBLEConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioGuideBleconnectBinding

    private val bluetoothManager: BluetoothManager by lazy { getSystemService(BluetoothManager::class.java) }
    private val bluetoothAdapter: BluetoothAdapter? by lazy { bluetoothManager.adapter }

    private lateinit var arrayAdapter: BleCustomAdapter
    private lateinit var devices: ArrayList<BluetoothDevice>
    private lateinit var listView: ListView
    private lateinit var scanButton: Button

    // 스캐너와 콜백 객체를 정의합니다.
    private val scanner by lazy { bluetoothAdapter?.bluetoothLeScanner }
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {

            val device = result.device
//
//            if (device.name != null && device.name.startsWith("G")&& !devices.contains(device)) {
//                devices.add(device)
//                arrayAdapter.notifyDataSetChanged()
//                Log.d("BLE", "Found BLE device: ${device.name} - ${device.address}")
//                Toast.makeText(this@AudioGuideBLEConnectActivity, "발견된 기기: ${device.name}", Toast.LENGTH_SHORT).show()
//            }
            if(!devices.contains(device)){
                    val deviceName = device.name ?: "Unknown"
                    val deviceAddress = device.address
                    devices.add(device)
                    arrayAdapter.notifyDataSetChanged()
                    Log.d("bluetoothconnect", "Found BLE device: $deviceName - ${deviceAddress}")
                    Toast.makeText(this@AudioGuideBLEConnectActivity, "발견된 기기: $deviceName", Toast.LENGTH_SHORT).show()
                }
        }
        override fun onScanFailed(errorCode: Int) {
            Log.e("bluetoothconnect", "Scan failed with error code: $errorCode")
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioGuideBleconnectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions(this)  // 권한을 검사 및 없다면 요청

        listView = binding.listView
        scanButton = binding.scanButton

        devices = ArrayList()
        arrayAdapter = BleCustomAdapter(this, devices)
        listView.adapter = arrayAdapter

        scanButton.setOnClickListener {
            if (bluetoothAdapter?.isEnabled == true) {
                startBLEScan()
            } else {
                checkPermissions(this)
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            connectToDevice(devices[position])
            Log.d("Bluetooth", "기기 선택됨: ${devices[position].name ?: "Unknown"} - ${devices[position].address}")
        }
    }

    private fun startBLEScan() {
        val filters = listOf(ScanFilter.Builder().build())
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner?.startScan(filters, settings, scanCallback)
    }

    private fun stopBLEScan() {
        scanner?.stopScan(scanCallback)
    }

    private fun connectToDevice(device: BluetoothDevice) {
        stopBLEScan()
        val deviceName = device.name ?: "Unknown"
        val deviceAddress = device.address
        Toast.makeText(this, "$deviceName 에 연결 시도 중입니다", Toast.LENGTH_SHORT).show()
        Log.d("Bluetooth", "$deviceName - ${deviceAddress}에 연결 시도 중입니다")
        navigateToAudioGuideBLEControl(device, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBLEScan()
    }
}