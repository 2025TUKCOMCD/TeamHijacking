package com.example.front.transportation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityTransNewPathDetatilBinding
import com.example.front.transportation.processor.RealTimeProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        val routeStationsAndBusesString = intent.getStringExtra("routeStationsAndBuses") ?: ""
        Log.d("TransNewPathDetailActivity", "routeStationsAndBusesString: $routeStationsAndBusesString")

        if (routeStationsAndBusesString.isNotEmpty()) {
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

            CoroutineScope(Dispatchers.Main).launch {
                val results = mutableListOf<Map<String, String>>()
                for (busInfoMap in routeStationsAndBuses) {
                    Log.d("TransNewPathDetailActivity", "Bus Info Map: $busInfoMap")

                    val busID = busInfoMap["busID"]
                    val startLocalStationID = busInfoMap["startLocalStationID"]?.toInt()
                    val endLocalStationID = busInfoMap["endLocalStationID"]
                    val busLocalBlID = busInfoMap["busLocalBlID"]?.toInt()
                    val startStationInfo = busInfoMap["startStationInfo"]?.toInt()
                    val endStationInfo = busInfoMap["endStationInfo"]

                    Log.d("TransNewPathDetailActivity", "Bus ID: $busID")
                    Log.d("TransNewPathDetailActivity", "Start Local Station ID: $startLocalStationID")
                    Log.d("TransNewPathDetailActivity", "End Local Station ID: $endLocalStationID")
                    Log.d("TransNewPathDetailActivity", "Bus Local BLID: $busLocalBlID")
                    Log.d("TransNewPathDetailActivity", "Start Station Info: $startStationInfo")
                    Log.d("TransNewPathDetailActivity", "End Station Info: $endStationInfo")

                    try {
                        val result = withContext(Dispatchers.IO) {
                            RealTimeProcessor.fetchRealtimeStation(
                                stId = startLocalStationID ?: 0,
                                busRouteId = busLocalBlID ?: 0,
                                ord = startStationInfo ?: 0,
                                "json"
                            )
                        }
                        result?.let { results.addAll(it) }
                    } catch (e: Exception) {
                        Log.e("TransNewPathDetailActivity", "Error occurred while fetching real-time station data", e)
                    }
                }

                results.forEach { item ->
                    tvRouteInfo.append("${item["rtNm"]} - ${item["stNm"]}:\n" +
                            "첫 번째 차량: ${item["arrmsg1"]} (${item["traTime1"]}초)\n" +
                            "두 번째 차량: ${item["arrmsg2"]} (${item["traTime2"]}초)\n\n")
                }
            }
        } else {
            Log.e("TransNewPathDetailActivity", "routeStationsAndBusesString is empty")
            tvRouteInfo.text = "No route information provided."
        }
    }
}