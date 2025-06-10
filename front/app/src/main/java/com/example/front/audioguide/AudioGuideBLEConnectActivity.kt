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
import android.widget.Toast // Toast를 추가했습니다.
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityAudioGuideBleconnectBinding
import java.util.*

class AudioGuideBLEConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioGuideBleconnectBinding

    private val bluetoothManager: BluetoothManager by lazy { getSystemService(BluetoothManager::class.java) }
    private val bluetoothAdapter: BluetoothAdapter? by lazy { bluetoothManager.adapter }

    // AGH 기기들을 위한 리스트와 어댑터
    private lateinit var aghArrayAdapter: BleCustomAdapter
    private lateinit var aghDevices: ArrayList<BluetoothDevice>
    private lateinit var listViewAgh: ListView

    // BGH 기기들을 위한 리스트와 어댑터
    private lateinit var bghArrayAdapter: BleCustomAdapter
    private lateinit var bghDevices: ArrayList<BluetoothDevice>
    private lateinit var listViewBgh: ListView

    private lateinit var scanButton: Button

    // 스캐너 객체 (이전에 정의된 그대로)
    private val scanner by lazy { bluetoothAdapter?.bluetoothLeScanner }

    @SuppressLint("MissingInflatedId") // ListView ID를 올바르게 사용하면 이 경고는 사라집니다.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioGuideBleconnectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 권한 확인 및 요청 (checkPermissions 함수는 별도의 파일에 정의되어 있다고 가정합니다)
        checkPermissions(this)

        // AGH ListView 초기화
        listViewAgh = binding.listViewAgh
        aghDevices = ArrayList()
        aghArrayAdapter = BleCustomAdapter(this, aghDevices)
        listViewAgh.adapter = aghArrayAdapter

        // BGH ListView 초기화
        listViewBgh = binding.listViewBgh
        bghDevices = ArrayList()
        bghArrayAdapter = BleCustomAdapter(this, bghDevices)
        listViewBgh.adapter = bghArrayAdapter

        scanButton = binding.scanButton

        scanButton.setOnClickListener {
            if (bluetoothAdapter?.isEnabled == true) {
                // 스캔 시작 전에 기존 목록을 비웁니다.
                aghDevices.clear()
                bghDevices.clear()
                aghArrayAdapter.notifyDataSetChanged()
                bghArrayAdapter.notifyDataSetChanged()
                startBLEScan()
            } else {
                Toast.makeText(this, "블루투스를 켜주세요.", Toast.LENGTH_SHORT).show()
                // 필요하다면 블루투스 활성화 요청 인텐트를 띄울 수 있습니다.
                // val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                // startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }

        // AGH ListView 아이템 클릭 리스너
        listViewAgh.setOnItemClickListener { _, _, position, _ ->
            stopBLEScan()
            val selectedDevice = aghDevices[position]
            Log.d("bluetoothconnect", "AGH 기기 클릭됨: ${selectedDevice.name ?: "Unknown"}")
            connectToDevice(selectedDevice, this) // connectToDevice 함수는 별도 파일에 정의되어 있다고 가정
        }

        // BGH ListView 아이템 클릭 리스너
        listViewBgh.setOnItemClickListener { _, _, position, _ ->
            stopBLEScan()
            val selectedDevice = bghDevices[position]
            Log.d("bluetoothconnect", "BGH 기기 클릭됨: ${selectedDevice.name ?: "Unknown"}")
            connectToDevice(selectedDevice, this) // connectToDevice 함수는 별도 파일에 정의되어 있다고 가정
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBLEScan()
    }

    private fun startBLEScan() {
        val filters = listOf(ScanFilter.Builder().build()) // 모든 기기를 스캔하고 필터링은 onScanResult에서 진행
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner?.startScan(filters, settings, scanCallback)
        Toast.makeText(this, "블루투스 스캔 시작...", Toast.LENGTH_SHORT).show()
        Log.d("bluetoothconnect", "BLE 스캔 시작")
    }

    private fun stopBLEScan() {
        scanner?.stopScan(scanCallback)
        Toast.makeText(this, "블루투스 스캔 중지.", Toast.LENGTH_SHORT).show()
        Log.d("bluetoothconnect", "BLE 스캔 중지")
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            // device.name이 null일 경우 "Unknown Device"로 설정
            val deviceName = device.name ?: "Unknown Device"


            // AGH 기기 필터링 및 추가
            if (!aghDevices.contains(device) && deviceName.startsWith("G")) {
                aghDevices.add(device)
                aghArrayAdapter.notifyDataSetChanged()
                Log.d("bluetoothconnect", "Found AGH device: $deviceName - ${device.address}")
            }
            // BGH 기기 필터링 및 추가
            else
                if (!bghDevices.contains(device)) {
                    bghDevices.add(device)
                    bghArrayAdapter.notifyDataSetChanged()
                    Log.d("bluetoothconnect", "Found BGH device: $deviceName - ${device.address}")
                }

        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("bluetoothconnect", "Scan failed with error code: $errorCode")
            Toast.makeText(this@AudioGuideBLEConnectActivity, "스캔 실패: $errorCode", Toast.LENGTH_LONG).show()
        }

        // onBatchScanResults는 기본 구현을 그대로 두어도 무방합니다.
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            // 배치 스캔 결과를 처리할 필요가 있다면 여기에 구현합니다.
        }
    }
}