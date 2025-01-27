package com.example.front.transportation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.front.databinding.ActivityTransNewPathDetatilBinding
import com.example.front.transportation.processor.RealTimeProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransNewPathDetailActivity : AppCompatActivity() {

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
        Log.d("TransNewPathDetailActivity", "routeStationsAndBusesString: $routeStationsAndBusesString")

        if (routeStationsAndBusesString.isNotEmpty()) {
            // 쉼표(,)로 나누고 각 버스 정보를 담은 map 생성
            val routeStationsAndBuses = routeStationsAndBusesString.split(",").chunked(6).map { parts ->
                mapOf(
                    "busID" to parts[0].split(":")[1].trim(),
                    "startLocalStationID" to parts[1].split(":")[1].trim(),
                    "endLocalStationID" to parts[2].split(":")[1].trim(),
                    "busLocalBlID" to parts[3].split(":")[1].trim(),
                    "startStationInfo" to parts[4].split(":")[1].trim(),
                    "endStationInfo" to parts[5].split(":")[1].trim()
                )
            }

            // 도착 정보 추출 및 출력
            routeStationsAndBuses.forEach { busInfoMap ->
                Log.d("TransNewPathDetailActivity", "Bus Info Map: $busInfoMap")

                val busID = busInfoMap["busID"]
                val startLocalStationID = busInfoMap["startLocalStationID"]
                val endLocalStationID = busInfoMap["endLocalStationID"]
                val busLocalBlID = busInfoMap["busLocalBlID"]
                val startStationInfo = busInfoMap["startStationInfo"]
                val endStationInfo = busInfoMap["endStationInfo"]

                Log.d("TransNewPathDetailActivity", "Bus ID: $busID")
                Log.d("TransNewPathDetailActivity", "Start Local Station ID: $startLocalStationID")
                Log.d("TransNewPathDetailActivity", "End Local Station ID: $endLocalStationID")
                Log.d("TransNewPathDetailActivity", "Bus Local BLID: $busLocalBlID")
                Log.d("TransNewPathDetailActivity", "Start Station Info: $startStationInfo")
                Log.d("TransNewPathDetailActivity", "End Station Info: $endStationInfo")

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val result = RealTimeProcessor.fetchRealtimeStation(
                            stId = startLocalStationID?.toInt() ?: 0,
                            busRouteId = busLocalBlID?.toInt() ?: 0,
                            ord = startStationInfo?.toInt() ?: 0,
                            "json"
                        )
                        // Initialize RecyclerView
                    } catch (e: Exception) {
                        Log.e("TransNewPathDetailActivity", "Error occurred while fetching real-time station data", e)
                    }
                }
            }
        } else {
            Log.e("TransNewPathDetailActivity", "routeStationsAndBusesString is empty")
            tvRouteInfo.text = "No route information provided."
        }
    }
}