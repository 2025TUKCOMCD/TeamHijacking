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
import com.example.front.transportation.TransportationMainActivity

class AudioGuideBLEConnectActivity : AppCompatActivity() {

    //BluetoothManager를 받아옴
    private val bluetoothManager: BluetoothManager by lazy {
        getSystemService(BluetoothManager::class.java)
    }
    //BlutoothAdapter를 받아옴
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }

    //사용할 변수들 1. 어댑터 배열 2. 장치리스트 3.장치 4. 리스트뷰(장치들이 하나씩 들어갈 예정) 5. 스캔 시작버튼
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var deviceList: ArrayList<String>
    private lateinit var devices: ArrayList<BluetoothDevice>
    private lateinit var listView: ListView
    private lateinit var scanButton: Button

    //필요한 권한들을 쭉 적어놓음 나중에 사용 예정
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN
    )
    // 블루투스를 활성화 하는지 물어보는 창으로 Intent를 사용하여 사용
    private val activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                Log.d("Bluetooth", "블루투스 활성화")
                startDiscovery()
            } else if (it.resultCode == RESULT_CANCELED) {
                Log.d("Bluetooth", "블루투스 활성화 취소")
            }
        }
    // 권한을 확인하는 함수로 위에 넣어둔 requiredPermissions들을 토대로 권한 검사
    private fun checkPermissions() {
        val rejectedPermissionList = ArrayList<String>()
        //권한들을 하나씩 검사
        for (permission in requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED   //만약 권한이 부여되어있지 않다면
            ) {
                rejectedPermissionList.add(permission) //rejectedPermissionList에 추가해 둠
            }
        }
        if (rejectedPermissionList.isNotEmpty()) {  //만약 거부된 권한 이 있다면
            val array = arrayOfNulls<String>(rejectedPermissionList.size)
            ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array), 2)  //권한을 요청함
        } else {
            Toast.makeText(this, "Bluetooth Permission Success", Toast.LENGTH_SHORT).show()  // 거부된 권한이 없다면 블루투스 권한성공을 출력
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_guide_bleconnect)

        checkPermissions()  //권한을 검사

        //사용할 객체들을 받아옴
        listView = findViewById(R.id.listView)
        scanButton = findViewById(R.id.scanButton)
        deviceList = ArrayList()
        devices = ArrayList()
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        listView.adapter = arrayAdapter
        //만약 스캔버튼을 누르면
        scanButton.setOnClickListener {
            if (bluetoothAdapter?.isEnabled == true) {  //만약 blutoothAdapter가 사용가능하다면
                makeDiscoverable() //블루투스 권한을 요청하고
                startDiscovery() //블루투스 탐색을 시작
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activityResultLauncher.launch(enableBtIntent)  //아니라면 권한을 요청하는 페이지로 이동
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            connectToDevice(devices[position]) //devices를 보내줌 기기 연결시에 해야할 함수 실행
            Log.d("Bluetooth", "기기 선택됨: ${devices[position].name} - ${devices[position].address}")  //로그 출력
        }
    }

    private fun makeDiscoverable() {  //서로 탐색할 수 있는 블루투스 권한을 요청
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        startActivity(discoverableIntent)
    }
    /*
    블루투스를 탐색하는 함수

     */
    private fun startDiscovery() {
        val filter = IntentFilter().apply {  //IntentFilter를 생성하고 이벤트 3가지를 추가 여기선 BluetoothDevice를 찾을때, BluetoothAdapter를 시작하고 끝낼때를 넣음
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(receiver, filter)  //receiver를 등록한다 쉽게 말해서 broadcast(블루투스 탐지 백그라운드를) 시작하겠다는뜻
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  //쉽게 말하자면 권한 검사 코드 늘리는 주범 SCAN과 LOCATION 권한을 검사한다(또 할필요 있는지 검토 필요)
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
        bluetoothAdapter?.let {  //만약 Adapter가 이미 탐색중이라면 중단시키고 재탐색 한다.
            if (it.isDiscovering) {
                it.cancelDiscovery()
            }
            it.startDiscovery()
        }
    }

    private val receiver = object : BroadcastReceiver() {  //여기서 블루투스를 탐색함
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {  //시작할때
                    Log.d("Bluetooth", "블루투스 탐색 시작")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> { //끝날때
                    Log.d("Bluetooth", "블루투스 탐색 종료")
                }
                BluetoothDevice.ACTION_FOUND -> {  //블루투스를 찾았을때
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)  //device를 받아옴
                    device?.let {
                        val deviceName = it.name ?: "Unknown"  //만약 이름이 없다면 Unknown으로 지정
                        val deviceAddress = it.address  //기기의 맥주소를 받아옴
                        deviceList.add("$deviceName - $deviceAddress")  //deviceList에 Name과 Address 순서대로 저장
                        devices.add(it) // devices에 기기 추가
                        arrayAdapter.notifyDataSetChanged() //UI를 다시그리게 arrayAdapter에 알려줌
                        Log.d("Bluetooth", "블루투스 기기 발견: $deviceName - $deviceAddress")  //로그및 토스
                        Toast.makeText(this@AudioGuideBLEConnectActivity, "발견된 기기: $deviceName", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("Bluetooth", "GATT 연결 성공: ${gatt.device.name} - ${gatt.device.address}") //Gatt 연결 성공
                gatt.discoverServices()  //서비스를 찾는 코드
                navigateToNextActivity(gatt.device)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("Bluetooth", "GATT 연결 해제: ${gatt.device.name} - ${gatt.device.address}")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {  //만약 서비스를 찾는다면
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Bluetooth", "서비스 검색 성공: ${gatt.device.name}")
                for (service in gatt.services) {
                    Log.d("Bluetooth", "서비스: ${service.uuid}")  //서비스 및 특성 출력
                    for (characteristic in service.characteristics) {
                        Log.d("Bluetooth", "  특성: ${characteristic.uuid}")
                    }
                }
            } else {
                Log.d("Bluetooth", "서비스 검색 실패: ${gatt.device.name}")
            }
        }
    }



    private fun navigateToNextActivity(device: BluetoothDevice) {
        val intent = Intent(this, AudioGuideBLEControl::class.java)
        intent.putExtra("EXTRA_BLUETOOTH_DEVICE", device)
        startActivity(intent)
    }




    /*
    연결함수로 device.connectGatt부분이 메인임
    Gatt함수 자동 재연결을 false로 하였고 bludtoothGattCallback을 넣어서 GATT과의 연결 상태 및 서비스 등을 발견하게 해줌
 */
    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothAdapter?.cancelDiscovery() //어뎁터의 간섭을 막기 위해 탐색을 중지 시킴
        device.connectGatt(this, false, bluetoothGattCallback) //
        Toast.makeText(this, "${device.name}에 연결 시도 중입니다", Toast.LENGTH_SHORT).show()
        Log.d("Bluetooth", "${device.name} - ${device.address}에 연결 시도 중입니다")
    }
    /*
    권한을 요청해 주는 함수로 권한을 요청하는데 사용
     */
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
