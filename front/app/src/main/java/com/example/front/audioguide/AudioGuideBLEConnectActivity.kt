package com.example.front.audioguide

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
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
            stopBLEScan()
            val selectedDevice = devices[position]
            val deviceName = selectedDevice.name ?: "Unknown"

            connectToDevice(selectedDevice, this)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBLEScan()
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

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if ((device.name != null && device.name.startsWith("AGH") && !devices.contains(device)) ||
                (device.name != null && device.name.startsWith("BGH") && !devices.contains(device))
            ) {
                devices.add(device)
                arrayAdapter.notifyDataSetChanged()
                Log.d("BLE", "Found BLE device: ${device.name} - ${device.address}")
                Toast.makeText(this@AudioGuideBLEConnectActivity, "발견된 기기: ${device.name}", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("bluetoothconnect", "Scan failed with error code: $errorCode")
        }
    }
}