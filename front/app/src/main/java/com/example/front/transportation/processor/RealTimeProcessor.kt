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

    suspend fun fetchRealtimeStation(stId: Int, busRouteId: Int, ord: Int, resultType: String): List<Map<String, String>>? {
        return try {
            val response = withContext(Dispatchers.IO) {
                busService.getArrInfoByRoute(Public_APIKEY, stId, busRouteId, ord, resultType)
            }
            val rawJson = response.string()
            Log.d("RealTimeProcessor", "Raw response received: $rawJson")
            val parsedResponse = gson.fromJson(rawJson, RealtimeStation::class.java)

            if (parsedResponse == null || parsedResponse.msgBody == null || parsedResponse.msgBody.itemList == null) {
                Log.e("RealTimeProcessor", "Parsed response or msgBody is null")
                return emptyList() // Return an empty list if the response is not as expected
            }

            parsedResponse.msgBody.itemList.map { item ->
                mapOf(
                    "정류소 이름" to (item.stNm ?: "정보 없음"),
                    "노선 이름" to (item.rtNm ?: "정보 없음"),
                    "첫 번째 차량 번호판" to (item.plainNo1 ?: "정보 없음"),
                    "첫 번째 차량 남은 시간(초)" to (item.traTime1 ?: "정보 없음"),
                    "첫 번째 차량 도착 여부" to (item.isArrive1 ?: "정보 없음"),
                    "첫 번째 차량 도착 메시지" to (item.arrmsg1 ?: "정보 없음"),
                    "두 번째 차량 번호판" to (item.plainNo2 ?: "정보 없음"),
                    "두 번째 차량 남은 시간(초)" to (item.traTime2 ?: "정보 없음"),
                    "두 번째 차량 도착 여부" to (item.isArrive2 ?: "정보 없음"),
                    "두 번째 차량 도착 메시지" to (item.arrmsg2 ?: "정보 없음")
                )
            }.also { resultList ->
                return resultList
            }
        } catch (e: Exception) {
            Log.e("RealTimeProcessor", "Error while fetching realtime station data", e)
            null
        }
    }
}