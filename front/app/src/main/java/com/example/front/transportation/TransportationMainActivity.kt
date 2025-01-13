package com.example.front.transportation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.front.processor.RouteProcessor
import com.example.front.databinding.ActivityTransportationMainBinding
import kotlinx.coroutines.launch

data class PathRouteResult(
    val totalTime: Int,
    val transitCount: Int,
    val mainTransitTypes: String,
    val detailedPath: String
)

class TransportationMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportationMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportationMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //위도 경도 입력 부분
        fetchAndDisplayRoutes(
            startLat = 37.513841, // 잠실역
            startLng = 127.101823,
            endLat = 37.476813, // 낙성대역
            endLng = 126.964156
        )
    }
    // 경로 데이터 가져오기
    private fun fetchAndDisplayRoutes(startLat: Double, startLng: Double, endLat: Double, endLng: Double) {
        lifecycleScope.launch {
            try {
                val result = RouteProcessor.fetchAndProcessRoutes(
                    startLat = startLat,
                    startLng = startLng,
                    endLat = endLat,
                    endLng = endLng
                )

                result.forEach { PathRouteResult : PathRouteResult ->
                    val button = Button(this@TransportationMainActivity).apply {
                        text = "총 소요 시간 ${PathRouteResult.totalTime}분\n환승 횟수 ${PathRouteResult.transitCount}\n환승 정보\n ${PathRouteResult.mainTransitTypes}"
                        setTextColor(resources.getColor(android.R.color.white))
                        textSize = 16f
                        setPadding(32, 24, 32, 24) // 버튼 내부 여백
                        setBackgroundColor(resources.getColor(android.R.color.black)) // 버튼 배경색을 검정색으로 설정
                        setOnClickListener {
                            // 상세 경로 표시 부분
                        }
                    }
                    binding.routeContainer.addView(button)
                }

            } catch (e: Exception) {
                Log.e("TransportationMainActivity", "Error fetching routes", e)
            }
        }
    }
    // 상세 경로 다이얼로그
}