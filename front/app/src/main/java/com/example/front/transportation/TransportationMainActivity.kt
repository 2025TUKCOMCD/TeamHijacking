package com.example.front.transportation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.front.R
import com.example.front.data.RouteInfo
import com.example.front.data.RouteProcessor
import com.example.front.databinding.ActivityTransportationMainBinding
import kotlinx.coroutines.launch

class TransportationMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportationMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportationMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example call to fetch and display routes
        fetchAndDisplayRoutes(
            startLat = 37.513841, // 잠실역
            startLng = 127.101823,
            endLat = 37.476813, // 낙성대역
            endLng = 126.964156
        )
    }

    private fun fetchAndDisplayRoutes(startLat: Double, startLng: Double, endLat: Double, endLng: Double) {
        lifecycleScope.launch {
            try {
                val result = RouteProcessor.fetchAndProcessRoutes(
                    startLat = startLat,
                    startLng = startLng,
                    endLat = endLat,
                    endLng = endLng
                )

                result.forEach { routeInfo ->
                    val button = Button(this@TransportationMainActivity).apply {
                        text = "총 소요 시간 ${routeInfo.totalTime}분\n환승 횟수 ${routeInfo.transitCount}\n환승 정보 ${routeInfo.mainTransitTypes}"
                        setTextColor(resources.getColor(android.R.color.white))
                        textSize = 16f
                        setPadding(32, 24, 32, 24) // 버튼 내부 여백
                        setBackgroundColor(resources.getColor(android.R.color.black)) // 버튼 배경색을 검정색으로 설정
                        setOnClickListener {
                            showRouteDetails(routeInfo)
                        }
                    }
                    binding.routeContainer.addView(button)
                }

            } catch (e: Exception) {
                Log.e("TransportationMainActivity", "Error fetching routes", e)
            }
        }
    }

    private fun showRouteDetails(routeInfo: RouteInfo) {
        AlertDialog.Builder(this)
            .setTitle("경로 상세 정보")
            .setMessage("총 소요 시간: ${routeInfo.totalTime}분\n환승 횟수: ${routeInfo.transitCount}\n주요 교통수단: ${routeInfo.mainTransitTypes}\n세부 경로: ${routeInfo.detailedPath}")
            .setPositiveButton("확인", null)
            .show()
    }
}