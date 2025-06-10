package com.example.front.transportation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle

import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.front.R
import com.example.front.databinding.ActivityTransportNewPathSearchBinding
import com.example.front.transportation.data.searchPath.Route


import androidx.activity.viewModels


class TransportNewPathSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransportNewPathSearchBinding
    private val routeViewModel: RouteViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransportNewPathSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // onCreate 스코프에 있는 지역 변수
        val startLat = intent.getDoubleExtra("startLat", 37.340174)
        val startLng = intent.getDoubleExtra("startLng", 126.7335933)
        val endLat = intent.getDoubleExtra("endLat", 37.340174)
        val endLng = intent.getDoubleExtra("endLng", 127.0900351)
        val departureName = intent.getStringExtra("departureName") ?: "출발지"
        val destinationName = intent.getStringExtra("destinationName") ?: "도착지"

        val loadingSpinner = findViewById<ProgressBar>(R.id.loadingSpinner)
        val dataLayout = findViewById<LinearLayout>(R.id.newPathLinearLayout)

        // 데이터 로딩 시작
        loadingSpinner.visibility = View.VISIBLE
        dataLayout.visibility = View.GONE

        // ViewModel 의 LiveData 관찰
        routeViewModel.routeData.observe(this, Observer { routes ->
            // 데이터 로드 완료 후 처리
            loadingSpinner.visibility = View.GONE
            dataLayout.visibility = View.VISIBLE

            // 데이터 UI에 설정
            routes?.let {
                if (it.isNotEmpty()) {
                    // !!! 여기서 updateRouteViews 호출 시 start/end Lat/Lng를 함께 넘겨줍니다. !!!
                    updateRouteViews(it, startLat, startLng, endLat, endLng, departureName, destinationName)
                } else {
                    // 데이터 없는 경우 처리
                }
            }
        })

        // 경로 데이터 가져오기
        routeViewModel.fetchRoute(startLng, startLat, endLng, endLat)
    }

    // updateRouteViews 메서드 시그니처를 변경하여 start/end Lat/Lng를 받도록 합니다.
    private fun updateRouteViews(
        routes: List<Route>,
        startLat: Double, // 새로운 파라미터
        startLng: Double, // 새로운 파라미터
        endLat: Double,   // 새로운 파라미터
        endLng: Double,    // 새로운 파라미터
        departureName : String,
        destinationName : String
    ) {
        val route1Views = listOf(binding.transitCountView1, binding.totalTimeView1, binding.detailedPathView1, binding.mainTransitTypesView1)
        val route2Views = listOf(binding.transitCountView2, binding.totalTimeView2, binding.detailedPathView2, binding.mainTransitTypesView2)
        val route3Views = listOf(binding.transitCountView3, binding.totalTimeView3, binding.detailedPathView3, binding.mainTransitTypesView3)

        val routeLayouts = listOf(binding.someRootLayout1, binding.someRootLayout2, binding.someRootLayout3)
        val routeViews = listOf(route1Views, route2Views, route3Views)

        routes.forEachIndexed { index, route ->
            if (index < routeViews.size) {
                val (transitCountView, totalTimeView, detailedPathView, mainTransitTypesView) = routeViews[index]
                detailedPathView.text = route.transitTypeNo.joinToString(", ")
                transitCountView.text = getString(R.string.transitCount, route.transitCount)
                totalTimeView.text = "${route.totalTime} 분"
                mainTransitTypesView.text = route.mainTransitType

                // Set click listener for each route layout
                routeLayouts[index].setOnClickListener {
                    // Create the Intent and add data
                    val intent = Intent(this, TransportInformationActivity::class.java)
                    intent.putIntegerArrayListExtra("pathTransitType", ArrayList(route.pathTransitType)) // 경로 유형
                    intent.putStringArrayListExtra("transitTypeNo", ArrayList(route.transitTypeNo)) // 경로 한글 버전
                    intent.putExtra("routeIds", ArrayList(route.routeIds))

                    // !!! 파라미터로 받은 startLat, startLng, endLat, endLng 사용 !!!
                    intent.putExtra("startLat", startLat)
                    intent.putExtra("startLng", startLng)
                    intent.putExtra("endLat", endLat)
                    intent.putExtra("endLng", endLng)
                    intent.putExtra("departureName", departureName) // 출발지 주소
                    intent.putExtra("destinationName", destinationName) // 도착지 주소
                    startActivity(intent)
                }
            }
        }
    }
}