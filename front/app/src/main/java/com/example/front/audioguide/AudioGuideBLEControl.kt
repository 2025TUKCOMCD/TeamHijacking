package com.example.front.audioguide

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.front.databinding.ActivityAudioGuideBlecontrolBinding

class AudioGuideBLEControl : AppCompatActivity() {
    //전송할 데이터를 기능에 따라 변수 이름 지정
    private var position_derrivation = byteArrayOf(0x31, 0x00, 0x01)
    private var signal_guide = byteArrayOf(0x31, 0x00, 0x02)
    private var audio_guide = byteArrayOf(0x31, 0x00, 0x03)

    private lateinit var binding: ActivityAudioGuideBlecontrolBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAudioGuideBlecontrolBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("BluetoothControl", "화면 시작")

        val device: BluetoothDevice? = intent.getParcelableExtra("EXTRA_BLUETOOTH_DEVICE")
        Log.d("BluetoothControl", "객체 받아오기")
        Log.d("BluetoothControl", device.toString())

        bluetoothGattDisconnected = false
        connectToBluetoothGatt(device!!, this)

        //UART 방식의 형태로 되어있어서 데이터를 넣는 부분과 받아오는부분 개발 필요
        binding.button1.setOnClickListener {
            if (bluetoothGattState) {
                sendDataToCharacteristic(position_derrivation, bluetoothGatt!!)  //음성유도기 유도
                sendDataToCharacteristic(signal_guide, bluetoothGatt!!)          //음향신호기 유도
                Log.d("현빈", "위치 유도")
            }
            else{
                Log.d("BluetoothControl", "Gatt 연결 안됨")
            }
        }
        binding.button3.setOnClickListener {
            if (bluetoothGattState) {
                sendDataToCharacteristic(audio_guide, bluetoothGatt!!)  //음향신호기 신호
                Log.d("현빈", "음성 안내")
            }
            else{
                Log.d("BluetoothControl", "Gatt 연결 안됨")
            }
        }
        //임시 기능 이전 화면으로 돌아가게 만듦 나중엔 수정 예정
        binding.nextButton.setOnClickListener{
            finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothGattDisconnected = true
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
        bluetoothGatt?.disconnect()
    }

}
