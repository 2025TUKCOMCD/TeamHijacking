package com.example.front.transportation

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityTransNewPathDetatilBinding
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
                    val realtimeResult = RouteProcessor.fetchRealtimeStation(routeStationsAndBuses)
                    // 도착정보 출력
                } else {
                    // 기본 정보 출력
                }
            }
            //

        } else {
            Log.e("TransNewPathDetatilActivity", "routeStationsAndBusesString is empty")
        }

    }
}