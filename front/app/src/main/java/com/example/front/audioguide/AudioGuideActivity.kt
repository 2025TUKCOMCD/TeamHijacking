package com.example.front.audioguide

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityAudioGuideBinding

class AudioGuideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioGuideBinding.inflate(layoutInflater)

        //XML에서 정의된 버튼들을 연결
        val blinkerBtn1: ImageButton = binding.blinkerBtn1
        val blinkerBtn2: ImageButton = binding.blinkerBtn2

        setContentView(binding.main)

        //음성 유도기, 횡단보도 유도 기능
        blinkerBtn1.setOnClickListener{
            Log.d("Blinker", "시각장애인용 음향 유도기 안내 버튼 클릭")

        }

        //음성 신호기 기능
        blinkerBtn2.setOnClickListener{
            Log.d("Blinker", "시각장애인용 음향 신호기 작동 버튼 클릭")
        }

    }
}