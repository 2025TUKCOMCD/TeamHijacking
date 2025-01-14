package com.example.front.transportation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.front.processor.RouteProcessor
import com.example.front.databinding.ActivityTransportationMainBinding
import kotlinx.coroutines.launch
import android.app.AlertDialog
import com.example.front.data.searchPath.PathRouteResult

class TransportationMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportationMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportationMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchAndDisplayRoutes(
            startLat = 37.513841, // 잠실역
            startLng = 127.101823,
            endLat = 37.476813, // 낙성대역
            endLng = 126.964156
        )
    }

    //
    private fun fetchAndDisplayRoutes(startLat: Double, startLng: Double, endLat: Double, endLng: Double) {
        lifecycleScope.launch {
            try {
                val result = RouteProcessor.fetchAndProcessRoutes(
                    startLat = startLat,
                    startLng = startLng,
                    endLat = endLat,
                    endLng = endLng
                )

                result.forEachIndexed { index, pathRouteResult ->
                    val button = Button(this@TransportationMainActivity).apply {
                        text = "경로 ${index + 1}\n총 소요 시간 ${pathRouteResult.totalTime}분\n환승 횟수 ${pathRouteResult.transitCount}\n환승 정보\n${pathRouteResult.mainTransitTypes}"
                        setTextColor(resources.getColor(android.R.color.white))
                        textSize = 16f
                        setPadding(32, 24, 32, 24)
                        setBackgroundColor(resources.getColor(android.R.color.black))
                        setOnClickListener {
                            //showDetailDialog(pathRouteResult)
                        }
                    }
                    binding.routeContainer.addView(button)
                }

            } catch (e: Exception) {
                Log.e("TransportationMainActivity", "Error fetching routes", e)
            }
        }
    }

//    private fun showDetailDialog(pathRouteResult: PathRouteResult) {
//        val detailInfo = pathRouteResult.detailInfo
//        if (detailInfo != null) {
//            val message = "현재 위치: ${detailInfo.positionX}, ${detailInfo.positionY}\n" +
//                          "출발 정류장 ID: ${detailInfo.fromStationId}\n" +
//                          "도착 정류장 ID: ${detailInfo.toStationId}"
//            AlertDialog.Builder(this)
//                .setTitle("상세 경로 정보")
//                .setMessage(message)
//                .setPositiveButton("확인", null)
//                .show()
//        } else {
//            AlertDialog.Builder(this)
//                .setTitle("상세 경로 정보")
//                .setMessage("버스 경로 정보가 없습니다.")
//                .setPositiveButton("확인", null)
//                .show()
//        }
//    }
}