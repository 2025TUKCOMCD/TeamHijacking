package com.example.front.transportation.processor

import android.util.Log
import com.example.front.BuildConfig
import com.example.front.BuildConfig.Public_Subway_APIKEY
import com.example.front.transportation.data.realtimeStation.subway.RealtimeSubwayStation
import com.example.front.transportation.service.SubwayService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

object SubwayRealTimeProcessor {
    private const val BASE_URL_SUBWAY = "http://swopenAPI.seoul.go.kr/api/subway/"
    private const val Public_Subway_APIKEY: String = BuildConfig.Public_Subway_APIKEY
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofitSubway: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_SUBWAY)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val subwayService: SubwayService = retrofitSubway.create(SubwayService::class.java)
    private val requestQueue = ConcurrentLinkedQueue<suspend () -> List<Map<String, String>>?>()

    suspend fun fetchRealtimeSubwayStation(stationName: String): List<Map<String, String>>? {
        val request = suspend {
            try {
                Log.d("SubwayRealTimeProcessor", "Fetching subway station data: stationName=$stationName")
                val response = withContext(Dispatchers.IO) {
                    subwayService.getRealtimeStationArrival(Public_Subway_APIKEY, stationName)
                }
                val rawJson = response.string()
                Log.d("SubwayRealTimeProcessor", "Raw response received: $rawJson")
                val parsedResponse = gson.fromJson(rawJson, RealtimeSubwayStation::class.java)

                if (parsedResponse == null || parsedResponse.row == null) {
                    Log.e("SubwayRealTimeProcessor", "Parsed response or row is null")
                    emptyList<Map<String, String>>() // Return an empty list if the response is not as expected
                } else {
                    parsedResponse.row.map { item ->
                        mapOf(
                            "stationName" to (item.statnNm ?: "정보 없음"),
                            "trainLineNm" to (item.trainLineNm ?: "정보 없음"),
                            "arvlMsg2" to (item.arvlMsg2 ?: "정보 없음"),
                            "arvlMsg3" to (item.arvlMsg3 ?: "정보 없음")
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SubwayRealTimeProcessor", "Error while fetching realtime station data", e)
                null
            }
        }

        requestQueue.add(request)
        return processQueue()
    }

    private suspend fun processQueue(): List<Map<String, String>>? {
        return withContext(Dispatchers.IO) {
            while (requestQueue.isNotEmpty()) {
                val request = requestQueue.poll()
                if (request != null) {
                    return@withContext request()
                }
            }
            null
        }
    }
}