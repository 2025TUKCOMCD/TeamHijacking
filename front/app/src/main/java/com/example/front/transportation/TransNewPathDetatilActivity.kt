package com.example.front.transportation

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.front.R
import com.example.front.data.RouteProcessor
import com.example.front.databinding.ActivityTransNewPathDetatilBinding
//import com.example.front.databinding.ActivityTransportationMainBinding
//import com.example.front.databinding.ActivityTransportationMainBinding
import kotlinx.coroutines.launch

/*경로 상세 정보 페이지, 경로 선택 시 상세 경로를 나타낼
* 주요 기능은 다음에 올 대중교통이 몇 분 뒤에 오는지 등이다.*/
class TransNewPathDetatilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransNewPathDetatilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransNewPathDetatilBinding.inflate(layoutInflater)
        setContentView(binding.main)

        val tvRouteInfo: TextView = binding.tvRouteInfo


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
        }
    }
}