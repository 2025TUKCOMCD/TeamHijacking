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
//                RouteProcessor.fetchRealtimeLocation()
            }
        }

        // Retrieve the routeStationsAndBuses from the intent
        val routeStationsAndBusesString = intent.getStringExtra("routeStationsAndBuses") ?: ""
        Log.d("TransNewPathDetatilActivity", "routeStationsAndBusesString: $routeStationsAndBusesString")

        if (routeStationsAndBusesString.isNotEmpty()) {
            val routeStationsAndBuses = routeStationsAndBusesString.split(",").map {
                val parts = it.split(" ")
                val stationID = parts[0].toIntOrNull() ?: 0 // Convert stationID to Int
                val busID = parts[1].toIntOrNull() ?: 0 // Handle empty or invalid integer
                stationID to busID
            }
            // 도착 정보 추출
            CoroutineScope(Dispatchers.Main).launch {
                if (routeStationsAndBuses.isNotEmpty()) {
                    // 실시간 도착정보 api 실행
                    val realtimeResult = RealTimeProcessor.fetchRealtimeStation(routeStationsAndBuses)
                    Log.d("TransNewPathDetatilActivity", "realtimeResult: $realtimeResult")
                    if (realtimeResult.isNotEmpty()) {
                        val busInfo = realtimeResult.joinToString("\n") { result ->
                            val endBusText = if (result?.endBusYn == "Y") "막차" else ""
                            "Left Station: ${result?.leftStation}, Arrival Sec: ${result?.arrivalSec}, Bus Status: ${result?.busStatus}, End Bus: ${result?.endBusYn} $endBusText, Low Bus: ${result?.lowBusYn}, Full Car: ${result?.fulCarAt}"
                        }
                        tvRouteInfo.text = busInfo
                    } else {
                        tvRouteInfo.text = "No bus information available."
                    }
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