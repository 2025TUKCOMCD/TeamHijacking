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
            val routeStationsAndBuses = routeStationsAndBusesString.split(",").chunked(7).mapNotNull { parts ->
                if (parts.size == 7) {
                    mapOf(
                        "stationName" to parts[0].split(":").getOrNull(1)?.trim().orEmpty(),
                        "busID" to parts[1].split(":").getOrNull(1)?.trim().orEmpty(),
                        "startLocalStationID" to parts[2].split(":").getOrNull(1)?.trim().orEmpty(),
                        "endLocalStationID" to parts[3].split(":").getOrNull(1)?.trim().orEmpty(),
                        "busLocalBlID" to parts[4].split(":").getOrNull(1)?.trim().orEmpty(),
                        "startStationInfo" to parts[5].split(":").getOrNull(1)?.trim().orEmpty(),
                        "endStationInfo" to parts[6].split(":").getOrNull(1)?.trim().orEmpty()
                    )
                } else {
                    null
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                val results = mutableListOf<Map<String, String>>()
                for (busInfoMap in routeStationsAndBuses) {
                    Log.d("TransNewPathDetailActivity", "Bus Info Map: $busInfoMap")
                    val stationName = busInfoMap["stationName"]
                    val busNo = busInfoMap["busID"]
                    val startLocalStationID = busInfoMap["startLocalStationID"]?.toIntOrNull()
                    val endLocalStationID = busInfoMap["endLocalStationID"]
                    val busLocalBlID = busInfoMap["busLocalBlID"]?.toIntOrNull()
                    val startStationInfo = busInfoMap["startStationInfo"]?.toIntOrNull()
                    val endStationInfo = busInfoMap["endStationInfo"]

                    Log.d("TransNewPathDetailActivity", "Station Name: $stationName")
                    Log.d("TransNewPathDetailActivity", "Bus No: $busNo")
                    Log.d("TransNewPathDetailActivity", "Start Local Station ID: $startLocalStationID")
                    Log.d("TransNewPathDetailActivity", "End Local Station ID: $endLocalStationID")
                    Log.d("TransNewPathDetailActivity", "Bus Local BLID: $busLocalBlID")
                    Log.d("TransNewPathDetailActivity", "Start Station Info: $startStationInfo")
                    Log.d("TransNewPathDetailActivity", "End Station Info: $endStationInfo")

                    try {
                        val result = withContext(Dispatchers.IO) {
                            if (startLocalStationID in 100100001..124900014) {
                                RealTimeProcessor.fetchRealtimeSeoulStation(
                                    stationName = stationName ?: "",
                                    stId = startLocalStationID ?: 0,
                                    busRouteId = busLocalBlID ?: 0,
                                    ord = startStationInfo ?: 0,
                                    "json"
                                )
                            } else {
                                RealTimeProcessor.fetchRealtimeGyeonGiStation(
                                    stationName = stationName ?: "",
                                    stationId = startLocalStationID ?: 0,
                                    routeId = busLocalBlID ?: 0,
                                    staOrder = startStationInfo ?: 0,
                                    "json"
                                )
                            }
                        }

                        if (result.isNullOrEmpty()) {
                            tvRouteInfo.append("서비스 지역이 \n 아닙니다.\n")
                        } else {
                            results.addAll(result)
                        }
                    } catch (e: Exception) {
                        Log.e("TransNewPathDetailActivity", "Error occurred while fetching real-time station data", e)
                    }
                }

                results.forEach { item ->
                    val arrivalInfo1 = if (item["arrmsg1"] == "운행종료" || item["predictTime1"].isNullOrEmpty()) {
                        "도착 정보 없음"
                    } else {
                        "${item["arrmsg1"] ?: item["predictTime1"]}초"
                    }

                    val arrivalInfo2 = if (item["arrmsg2"] == "운행종료" || item["predictTime2"].isNullOrEmpty()) {
                        "도착 정보 없음"
                    } else {
                        "${item["arrmsg2"] ?: item["predictTime2"]}초"
                    }

                    if (item.containsKey("rtNm")) {
                        // Seoul data
                        tvRouteInfo.append("${item["rtNm"]} - ${item["stationName"]}:\n" +
                                "첫 번째 차량: $arrivalInfo1\n" +
                                "두 번째 차량: $arrivalInfo2\n\n")
                    } else {
                        // Gyeonggi data
                        tvRouteInfo.append("${item["routeName"]} - ${item["stationName"]}:\n" +
                                "첫 번째 차량: $arrivalInfo1\n" +
                                "두 번째 차량: $arrivalInfo2\n\n")
                    }
                }
            }
        } else {
            Log.e("TransNewPathDetailActivity", "routeStationsAndBusesString is empty")
            tvRouteInfo.text = "No route information provided."
        }
    }
}