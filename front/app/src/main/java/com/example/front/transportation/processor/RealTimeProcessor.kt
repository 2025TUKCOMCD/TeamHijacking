package com.example.front.transportation.processor

import android.util.Log
import com.example.front.BuildConfig
import com.example.front.transportation.data.realtimeStation.RealStationResult
import com.example.front.transportation.service.BusService
import com.example.front.transportation.service.RouteService
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

    suspend fun fetchRealtimeStation(stId: Int, busRouteId: Int, ord: Int, resultType: String): RealStationResult {
        return try {
            withContext(Dispatchers.IO) {
                busService.getArrInfoByRoute(Public_APIKEY, stId, busRouteId, ord, resultType)
            }
        } catch (e: Exception) {
            Log.e("RealTimeProcessor", "Error while fetching realtime station data", e)
            RealStationResult
        }

    }
}