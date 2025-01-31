package com.example.front.transportation.processor

import android.util.Log
import com.example.front.BuildConfig
import com.example.front.transportation.data.realtimeStation.RealtimeStation
import com.example.front.transportation.service.BusService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

object RealTimeProcessor {
    private const val BASE_URL = "http://ws.bus.go.kr/api/rest/"
    private const val Public_APIKEY: String = BuildConfig.Public_APIKEY
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val busService: BusService = retrofit.create(BusService::class.java)
    private val requestQueue = ConcurrentLinkedQueue<suspend () -> List<Map<String, String>>?>()

    suspend fun fetchRealtimeStation(stId: Int, busRouteId: Int, ord: Int, resultType: String): List<Map<String, String>>? {
        val request = suspend {
            try {
                val response = withContext(Dispatchers.IO) {
                    busService.getArrInfoByRoute(Public_APIKEY, stId, busRouteId, ord, resultType)
                }
                val rawJson = response.string()
                Log.d("RealTimeProcessor", "Raw response received: $rawJson")
                val parsedResponse = gson.fromJson(rawJson, RealtimeStation::class.java)

                if (parsedResponse == null || parsedResponse.msgBody == null || parsedResponse.msgBody.itemList == null) {
                    Log.e("RealTimeProcessor", "Parsed response or msgBody is null")
                    emptyList<Map<String, String>>() // Return an empty list if the response is not as expected
                } else {
                    parsedResponse.msgBody.itemList.map { item ->
                        mapOf(
                            "stNm" to (item.stNm ?: "정보 없음"),
                            "rtNm" to (item.rtNm ?: "정보 없음"),
                            "traTime1" to (item.traTime1 ?: "정보 없음"),
                            "isArrive1" to (item.isArrive1 ?: "정보 없음"),
                            "arrmsg1" to (item.arrmsg1 ?: "정보 없음"),
                            "traTime2" to (item.traTime2 ?: "정보 없음"),
                            "isArrive2" to (item.isArrive2 ?: "정보 없음"),
                            "arrmsg2" to (item.arrmsg2 ?: "정보 없음")
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("RealTimeProcessor", "Error while fetching realtime station data", e)
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