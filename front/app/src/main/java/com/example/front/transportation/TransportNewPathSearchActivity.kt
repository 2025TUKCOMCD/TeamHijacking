package com.example.front.transportation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
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

        val startLat = intent.getDoubleExtra("startLat", 37.340174)
        val startLng = intent.getDoubleExtra("startLng", 126.7335933)
        val endLat = intent.getDoubleExtra("endLat", 37.340174)
        val endLng = intent.getDoubleExtra("endLng", 127.0900351)

        val loadingSpinner = findViewById<ProgressBar>(R.id.loadingSpinner)
        val dataLayout = findViewById<LinearLayout>(R.id.newPathLinearLayout)

        // 데이터 로딩 시작
        loadingSpinner.visibility = View.VISIBLE
        dataLayout.visibility = View.GONE

        // ViewModel의 LiveData 관찰
        routeViewModel.routeData.observe(this, Observer { routes ->
            // 데이터 로드 완료 후 처리
            loadingSpinner.visibility = View.GONE
            dataLayout.visibility = View.VISIBLE

            // 데이터를 UI에 설정
            routes?.let {
                if (it.isNotEmpty()) {
                    updateRouteViews(it)
                } else {
                    // 데이터가 없는 경우 처리
                }
            }
        })

        // 경로 데이터 가져오기
        routeViewModel.fetchRoute(startLng, startLat, endLng, endLat)
    }

    private fun updateRouteViews(routes: List<Route>) {
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
                    // Handle click event
                    val intent = Intent(this, TransportInfrmationActivity::class.java)
                    intent.putIntegerArrayListExtra("pathTransitType", ArrayList(route.pathTransitType))
                    intent.putStringArrayListExtra("transitTypeNo", ArrayList(route.transitTypeNo))
                    intent.putParcelableArrayListExtra("routeIds", ArrayList(route.routeIds.map { it as Parcelable }))
                    startActivity(intent)
                }
            }
        }
    }
}