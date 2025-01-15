package com.example.front.transportation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.adapters.ViewBindingAdapter.setPadding
import com.example.front.R
import com.example.front.data.searchPath.PathRouteResult
import com.example.front.databinding.ActivityTransportationMainBinding
import com.example.front.processor.RouteProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransportationMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportationMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportationMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val startLat = 37.513841
        val startLng = 127.101823
        val endLat = 37.476813
        val endLng = 126.964156

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = RouteProcessor.fetchAndProcessRoutes(startLat, startLng, endLat, endLng)

// 결과 분리 및 로그 출력 + 버튼 생성
                result.forEach { route ->
                    // 각각의 값 분리
                    val routeStationsAndBuses = route.routeStationsAndBuses
                    val totalTime = route.totalTime
                    val transitCount = route.transitCount
                    val mainTransitTypes = route.mainTransitTypes
                    val detailedPath = route.detailedPath

                    // 로그 출력
                    Log.d("RouteProcessor", "routeStationsAndBuses = $routeStationsAndBuses")
                    Log.d("RouteProcessor", "Total Time: $totalTime 분")
                    Log.d("RouteProcessor", "Transit Count: $transitCount 회")
                    Log.d("RouteProcessor", "Main Transit Types: $mainTransitTypes")
                    Log.d("RouteProcessor", "Detailed Path: $detailedPath")

                    // 동적 버튼 생성
                    val button = Button(this@TransportationMainActivity).apply {
                        text = "총 소요 시간: ${totalTime}분 \n 환승 횟수: ${transitCount}\n환승 정보: ${mainTransitTypes}\n상세 경로: ${detailedPath}".trimIndent()
                        setTextColor(resources.getColor(android.R.color.white))
                        textSize = 16f
                        setPadding(32, 24, 32, 24)
                        setBackgroundColor(resources.getColor(android.R.color.black))
                        setOnClickListener {
                            CoroutineScope(Dispatchers.Main).launch {
                                val realtimeResult =
                                    RouteProcessor.fetchRealtimeStation(routeStationsAndBuses)
                                Log.d("RouteProcessor", "실시간 경로 데이터: $realtimeResult")
                            }
                        }
                    }

                    // 생성된 버튼을 컨테이너에 추가
                    binding.routeContainer.addView(button)
                }
            } catch (e: Exception) {
                Log.e("TransportationMainActivity", "Error fetching routes", e)
            }
        }
    }

}