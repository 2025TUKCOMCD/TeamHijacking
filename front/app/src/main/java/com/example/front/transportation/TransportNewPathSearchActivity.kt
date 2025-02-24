package com.example.front.transportation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityTransportNewPathSearchBinding
import com.example.front.transportation.data.searchPath.Route
import com.example.front.transportation.processor.RouteProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransportNewPathSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransportNewPathSearchBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransportNewPathSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val startLat = intent.getDoubleExtra("startLat", 37.340174)
        val startLng = intent.getDoubleExtra("startLng", 126.7335933)
        val endLat = intent.getDoubleExtra("endLat", 37.340174)
        val endLng = intent.getDoubleExtra("endLng", 127.0900351)

        Log.d("현빈", startLat.toString())
        Log.d("현빈", startLng.toString())
        Log.d("현빈", endLat.toString())
        Log.d("현빈", endLng.toString())

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = RouteProcessor.fetchRoute(startLng, startLat, endLng, endLat)

                // result가 null이 아니고 빈 리스트가 아닌지 확인
                if (result != null && result.isNotEmpty()) {
                    // 특정 경로에 접근 예시
                    if (result.size > 0) {
                        val firstRoute = result[0]
                        if (firstRoute.predictTimes1.contains("서비스 지역 아님") || firstRoute.predictTimes2.contains("서비스 지역 아님")) {
                            binding.mainTransitTypesView1.text = firstRoute.mainTransitType
                            binding.transitCountView1.text = firstRoute.transitCount.toString() + "회"
                            binding.totalTimeView1.text = firstRoute.totalTime.toString() + "분"
                            binding.detailedPathView1.text = "서비스 지역 아님"
                        } else {
                            Log.d(
                                "RouteProcessor",
                                "First Route Total Time: ${firstRoute.mainTransitType}"
                            )
                            binding.mainTransitTypesView1.text = firstRoute.mainTransitType
                            binding.transitCountView1.text = firstRoute.transitCount.toString() + "회"
                            binding.totalTimeView1.text = firstRoute.totalTime.toString() + "분"
                            binding.detailedPathView1.text = firstRoute.detailedPath
                        }

                        // 클릭 리스너 설정
                        addClickListener(binding.someRootLayout1, firstRoute)
                    }

                    if (result.size > 1) {
                        val secondRoute = result[1]
                        if (secondRoute.predictTimes1.contains("서비스 지역 아님") || secondRoute.predictTimes2.contains("서비스 지역 아님")) {
                            binding.mainTransitTypesView2.text = secondRoute.mainTransitType
                            binding.transitCountView2.text = secondRoute.transitCount.toString() + "회"
                            binding.totalTimeView2.text = secondRoute.totalTime.toString() + "분"
                            binding.detailedPathView2.text = "서비스 지역 아님"
                        } else {
                            Log.d("RouteProcessor", "Second Route Main Transit Type: ${secondRoute.mainTransitType}")
                            binding.mainTransitTypesView2.text = secondRoute.mainTransitType
                            binding.transitCountView2.text = secondRoute.transitCount.toString() + "회"
                            binding.totalTimeView2.text = secondRoute.totalTime.toString() + "분"
                            binding.detailedPathView2.text = secondRoute.detailedPath
                        }

                        // 클릭 리스너 설정
                        addClickListener(binding.someRootLayout2, secondRoute)
                    }

                    if (result.size > 2) {
                        val thirdRoute = result[2]
                        if (thirdRoute.predictTimes1.contains("서비스 지역 아님") || thirdRoute.predictTimes2.contains("서비스 지역 아님")) {
                            binding.mainTransitTypesView3.text = thirdRoute.mainTransitType
                            binding.transitCountView3.text = thirdRoute.transitCount.toString() + "회"
                            binding.totalTimeView3.text = thirdRoute.totalTime.toString() + "분"
                            binding.detailedPathView3.text = "서비스 지역 아님"
                        } else {
                            Log.d("RouteProcessor", "Third Route Transit Count: ${thirdRoute.mainTransitType}")
                            binding.mainTransitTypesView3.text = thirdRoute.mainTransitType
                            binding.transitCountView3.text = thirdRoute.transitCount.toString() + "회"
                            binding.totalTimeView3.text = thirdRoute.totalTime.toString() + "분"
                            binding.detailedPathView3.text = thirdRoute.detailedPath
                        }

                        // 클릭 리스너 설정
                        addClickListener(binding.someRootLayout3, thirdRoute)
                    }
                } else {
                    Log.d("RouteProcessor", "Result is null or empty")
                }
            } catch (e: Exception) {
                Log.e("RouteProcessor", "경로 탐색 중 오류 발생", e)
            }
        }
    }

    private fun addClickListener(layout: LinearLayout, route: Route) {
        layout.setOnClickListener {
            Log.d("ViewClick", "Clicked: ${route.mainTransitType}")
            val routeIds = route.routeIds.joinToString {","}
            val detail = route.detailTrans
            val parsedDetails = parseDetailTrans(detail)
            val predict1 = route.predictTimes1.joinToString(",")
            val predict2 = route.predictTimes2.joinToString(",")

            val intent = Intent(this, TransNewPathDetailActivity::class.java).apply {
                putExtra("routeIds", routeIds)
                putExtra("bus", parsedDetails["bus"]?.joinToString(","))
                putExtra("predict1", predict1)
                putExtra("predict2", predict2)
            }
            startActivity(intent)
        }
    }

    private fun parseDetailTrans(detailTrans: String): Map<String, List<String>> {
        val segments = detailTrans.split(" | ")

        val walkingSegments = mutableListOf<String>()
        val busSegments = mutableListOf<String>()
        val subwaySegments = mutableListOf<String>()

        for (segment in segments) {
            when {
                segment.contains("도보") -> walkingSegments.add(segment)
                segment.contains("버스") -> busSegments.add(segment)
                segment.contains("지하철") -> subwaySegments.add(segment)
            }
        }

        return mapOf(
            "walking" to walkingSegments,
            "bus" to busSegments,
            "subway" to subwaySegments
        )
    }
}
