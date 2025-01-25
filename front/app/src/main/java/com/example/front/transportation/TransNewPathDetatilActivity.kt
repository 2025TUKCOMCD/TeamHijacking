package com.example.front.transportation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityTransNewPathDetatilBinding
import com.example.front.transportation.processor.RealTimeProcessor
import com.example.front.transportation.processor.RouteProcessor
import com.example.front.transportation.service.RouteService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransNewPathDetatilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransNewPathDetatilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransNewPathDetatilBinding.inflate(layoutInflater)
        setContentView(binding.main)

        val tvRouteInfo: TextView = binding.tvRouteInfo
        tvRouteInfo.setTextColor(resources.getColor(android.R.color.white))

        val btnSelectRoute: Button = binding.btnSelectRoute
        btnSelectRoute.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                // Route selection functionality can be implemented here
            }
        }

        // Retrieve the routeStationsAndBuses from the intent
        val routeStationsAndBusesString = intent.getStringExtra("routeStationsAndBuses") ?: ""
        Log.d("TransNewPathDetatilActivity", "routeStationsAndBusesString: $routeStationsAndBusesString")

        if (routeStationsAndBusesString.isNotEmpty()) {
            val routeStationsAndBuses = routeStationsAndBusesString.split(",").mapNotNull {
                val parts = it.split(" ")
                val stationID = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
                val busID = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                stationID to busID
            }
            // 도착 정보 추출
            CoroutineScope(Dispatchers.Main).launch {
                if (routeStationsAndBuses.isNotEmpty()) {
                    // 실시간 도착정보 api 실행
                    val realtimeResult = RouteProcessor.fetchRealtimeStation(routeStationsAndBuses)
                    Log.d("TransNewPathDetatilActivity", "realtimeResult: $realtimeResult")
                } else {
                    // 기본 정보 출력
                    tvRouteInfo.text = "No bus information available."
                }
            }
        } else {
            Log.e("TransNewPathDetatilActivity", "routeStationsAndBusesString is empty")
            tvRouteInfo.text = "No route information provided."
        }
    }
}
