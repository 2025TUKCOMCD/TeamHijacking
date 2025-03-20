package com.example.front.transportation.processor

import android.util.Log
import com.example.front.BuildConfig
import com.example.front.BuildConfig.Public_Bus_APIKEY
import com.example.front.transportation.data.realtimeStation.bus.RealtimeGyeonGiStation
import com.example.front.transportation.data.realtimeStation.bus.RealtimeSeoulStation
import com.example.front.transportation.service.BusService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

object BusRealTimeProcessor {
    private const val BASE_URL_SEOUL = "http://ws.bus.go.kr/api/rest/"
    private const val BASE_URL_GYEONGGI = "http://apis.data.go.kr/"
    private const val Public_Bus_APIKEY: String = BuildConfig.Public_Bus_APIKEY
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofitSeoul: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_SEOUL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val retrofitGyeonggi: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_GYEONGGI)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val busServiceSeoul: BusService = retrofitSeoul.create(BusService::class.java)
    private val busServiceGyeonggi: BusService = retrofitGyeonggi.create(BusService::class.java)
    private val requestQueue = ConcurrentLinkedQueue<suspend () -> List<Map<String, String>>?>()

    suspend fun fetchRealtimeSeoulStation(stationName: String, stId: Int, busRouteId: Int, ord: Int, resultType: String): List<Map<String, String>>? {
        val request = suspend {
            try {
                Log.d("RealTimeProcessor", "Fetching Seoul station data: stId=$stId, busRouteId=$busRouteId, ord=$ord")
                val response = withContext(Dispatchers.IO) {
                    busServiceSeoul.getArrInfoByRoute(Public_Bus_APIKEY, stId, busRouteId, ord, resultType)
                }
                val rawJson = response.string()
                Log.d("RealTimeProcessor", "Raw response received: $rawJson")
                val parsedResponse = gson.fromJson(rawJson, RealtimeSeoulStation::class.java)

                if (parsedResponse == null || parsedResponse.msgBody == null || parsedResponse.msgBody.itemList == null) {
                    Log.e("RealTimeProcessor", "Parsed response or msgBody is null")
                    emptyList<Map<String, String>>() // Return an empty list if the response is not as expected
                } else {
                    parsedResponse.msgBody.itemList.map { item ->
                        mapOf(
                            "stationName" to (stationName ?: "정보 없음"),
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

    suspend fun fetchRealtimeGyeonGiStation(stationName: String, stationId: Int, routeId: Int, staOrder: Int, format: String): List<Map<String, String>>? {
        val request = suspend {
            try {
                Log.d("RealTimeProcessor", "Fetching Gyeonggi station data: stationId=$stationId, routeId=$routeId, staOrder=$staOrder")
                val response = withContext(Dispatchers.IO) {
                    busServiceGyeonggi.getBusArrivalItemv2(Public_Bus_APIKEY, stationId, routeId, staOrder, format)
                }
                val rawJson = response.string()
                Log.d("RealTimeProcessor", "Raw response received: $rawJson")
                val parsedResponse = gson.fromJson(rawJson, RealtimeGyeonGiStation::class.java)

                if (parsedResponse == null || parsedResponse.response == null || parsedResponse.response.msgBody == null || parsedResponse.response.msgBody.busArrivalItem == null) {
                    Log.e("RealTimeProcessor", "Parsed response or msgBody is null")
                    emptyList<Map<String, String>>() // Return an empty list if the response is not as expected
                } else {
                    val busArrivalItem = parsedResponse.response.msgBody.busArrivalItem

                    listOf(
                        mapOf(
                            "stationName" to (stationName ?: "정보 없음"),
                            "routeName" to (busArrivalItem.routeName ?: "정보 없음"),
                            "predictTime1" to (busArrivalItem.predictTime1.toString() ?: "정보 없음"),
                            "predictTime2" to (busArrivalItem.predictTime2.toString() ?: "정보 없음")
                        )
                    )
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