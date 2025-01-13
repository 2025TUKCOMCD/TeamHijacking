package com.example.front.audioguide

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.front.R
import com.example.front.databinding.ActivityAudioGuideBinding

class AudioGuideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioGuideBinding

    //XML에서 정의된 버튼들을 연결
    val blinkerBtn1: ImageButton = binding.blinkerBtn1
    val blinkerBtn2: ImageButton = binding.blinkerBtn2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioGuideBinding.inflate(layoutInflater)

        setContentView(binding.main)

        //음성 유도기, 횡단보도 유도 기능
        blinkerBtn1.setOnClickListener{

        }

        //음성 신호기 기능
        blinkerBtn2.setOnClickListener{

        }

    }
}