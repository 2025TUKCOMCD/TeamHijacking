package com.example.front.audioguide

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

        device?.let {
            if(checkPermissions(this)){
                connectToBluetoothGatt(device, this)
            }
            else{
                Log.d("BluetoothControl", " 권한 없음")
            }
        }
        //UART 방식의 형태로 되어있어서 데이터를 넣는 부분과 받아오는부분 개발 필요
        if (bluetoothGattState) {
            binding.button1.setOnClickListener {
                sendDataToCharacteristic(position_derrivation, bluetoothGatt!!)
                Log.d("현빈", "위치 유도")
            }
            binding.button2.setOnClickListener {
                sendDataToCharacteristic(signal_guide, bluetoothGatt!!)
                Log.d("현빈", "신호 안내")
            }
            binding.button3.setOnClickListener {
                sendDataToCharacteristic(audio_guide, bluetoothGatt!!)
                Log.d("현빈", "음성 안내")
            }
        }
        else{
            Log.d("BluetoothControl", "gatt 연결 안됨")
        }
    }

}
