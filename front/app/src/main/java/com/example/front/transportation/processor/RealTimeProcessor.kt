package com.example.front.transportation.processor

import android.util.Log
import com.example.front.BuildConfig
import com.example.front.transportation.data.realtimeStation.RealStationResult
import com.example.front.transportation.service.BusService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLEncoder

object RealTimeProcessor {
    private const val BUS_Location = "http://ws.bus.go.kr/api/rest/arrive/"
    private const val Public_APIKEY: String = BuildConfig.Public_APIKEY
    private val gson = Gson()
    private val client = OkHttpClient.Builder().build()

    suspend fun fetchRealtimeStation(routeStationsAndBuses: List<Pair<Int, Int>>): List<RealStationResult?> {
        return try {
            val realtimeStations = routeStationsAndBuses.mapNotNull { (stationID, busID) ->
                withContext(Dispatchers.IO) {
                    try {
                        val urlBuilder = StringBuilder("$BUS_Location/getArrInfoByRoute")
                        urlBuilder.append("?serviceKey=$Public_APIKEY")
                        urlBuilder.append("&stId=${stationID}")
                        urlBuilder.append("&busRouteId=${busID}")
                        urlBuilder.append("&ord=1")

                        val url = urlBuilder.toString()
                        val request = okhttp3.Request.Builder()
                            .url(url)
                            .get()
                            .build()

                        val response = client.newCall(request).execute()
                        val rawResponse = response.body?.string()
                        Log.d("RealTimeProcessor", "Response: $rawResponse")

                        if (rawResponse?.trim()?.startsWith("<") == true) {
                            // XML 응답
                            Log.e("RealTimeProcessor", "Received XML response, unable to parse with Gson.")
                            null
                        } else {
                            // JSON 응답
                            gson.fromJson(rawResponse, RealStationResult::class.java)
                        }
                    } catch (e: Exception) {
                        Log.e("RealTimeProcessor", "Error fetching real-time station data for stationID: $stationID, busID: $busID", e)
                        null
                    }
                }
            }
            realtimeStations
        } catch (e: Exception) {
            Log.e("RealTimeProcessor", "Error fetching real-time station data", e)
            listOf()
        }
    }
}



//    suspend fun fetchRealtimeLocation(busIDs: List<Int>) {
//        busIDs.forEach { busID ->
//            try {
//                val response = withContext(Dispatchers.IO) {
//                    RouteProcessor.routeService.realtimeStation(0, busID.toString(), apiKey = RouteProcessor.Pub)
//                }
//                val rawJson = response.string()
//               Log.d("RouteProcessor", "Realtime Location Raw Response: $rawJson")
//                // Process the response as needed
//            } catch (e: Exception) {
//                Log.e("RouteProcessor", "Error fetching real-time location data for busID: $busID", e)
//            }
//        }
//    }
//}