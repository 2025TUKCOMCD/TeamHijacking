package com.example.front.transportation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityTransportationMainBinding

class TransportationMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportationMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportationMainBinding.inflate(layoutInflater)
        setContentView(binding.main)

        // XML 에서 정의된 버튼 들을 연결
        val btnSavedPath: ImageButton = binding.btnSavedPath
        val btnNewPath: ImageButton = binding.btnNewPath
            //임시 버튼 생성, transportInformationActivity = 교통 안내 Activity 로 이어진다.
            val imsiBtn: Button = binding.imsiBtt

        // 각 버튼의 클릭 이벤트 처리
        btnSavedPath.setOnClickListener {
            // 저장된 경로 탐색 버튼 클릭 시 실행할 로직
            Log.d("Transportation", "저장된 경로 탐색 버튼 클릭")
            val intent = Intent(this, TransportationSavedPathActivity::class.java)
            startActivity(intent)
        }

        // 각 버튼의 클릭 이벤트 처리
        btnNewPath.setOnClickListener {
            // 새로운 경로 탐색 버튼 클릭 시 실행할 로직
            Log.d("Transportation", "새로운 경로 탐색 버튼 클릭")
            val intent = Intent(this, TransportationNewPathActivity::class.java)
            startActivity(intent)
        }

        imsiBtn.setOnClickListener {
            //교통 안내 Activity 로 이동됨, Dialog 창을 확인 하기 위함.
            val intent = Intent(this, TransportInformationActivity::class.java)
            startActivity(intent)
        }

    }

}