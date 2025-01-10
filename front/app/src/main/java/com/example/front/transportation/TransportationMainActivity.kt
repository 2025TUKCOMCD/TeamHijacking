package com.example.front.transportation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.front.R
import com.example.front.data.RouteProcessor
import com.example.front.databinding.ActivityTransportationMainBinding
import kotlinx.coroutines.launch

class TransportationMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportationMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransportationMainBinding.inflate(layoutInflater)
        setContentView(binding.main)

        // XML에서 정의된 버튼들을 연결
        val btnSavedPath: Button = binding.btnSavedPath
        val btnNewPath: Button = binding.btnNewPath
        val btnTransportBackspace: Button = binding.btnTransportBackspace

        // 각 버튼의 클릭 이벤트 처리
        btnSavedPath.setOnClickListener {
            // 저장된 경로 탐색 버튼 클릭 시 실행할 로직
            val intent = Intent(this, TransportationSavedPathActivity::class.java)
            startActivity(intent)
        }

        // 각 버튼의 클릭 이벤트 처리
        btnNewPath.setOnClickListener {
            // 새로운 경로 탐색 버튼 클릭 시 실행할 로직
            val intent = Intent(this, TransportationNewPathActivity::class.java)
            startActivity(intent)
        }

        // 각 버튼의 클릭 이벤트 처리
        btnTransportBackspace.setOnClickListener {
            // 뒤로가기 버튼 클릭 시 실행할 로직

            // 뒤로가기를 onBackPressedDispatcher를 통해 등록
          }



       /*
        lifecycleScope.launch {
            try {
                val result = RouteProcessor.fetchAndProcessRoutes(
                    startLat = 37.513841, // 잠실역
                    startLng = 127.101823,
                    endLat = 37.476813, // 낙성대역
                    endLng = 126.964156
                )
                tvRouteInfo.text = result
            } catch (e: Exception) {
                Log.e("TransportationMainActivity", "Error fetching routes", e)
            }
        }*/
    }
}