package com.example.front.transportation.processor

import RouteService
import android.util.Log
import com.example.front.BuildConfig
import com.example.front.transportation.data.searchPath.Route
import com.example.front.transportation.data.searchPath.RouteRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RouteProcessor {
    private const val Host_URL = BuildConfig.Host_URL
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Host_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val routeService: RouteService = retrofit.create(RouteService::class.java)

    suspend fun fetchRoute(startLat: Double, startLng: Double, endLat: Double, endLng: Double): List<Route>? {
        return try {
            val routeRequest = RouteRequest(startLat, startLng, endLat, endLng)
            Log.d("RouteProcessor", "요청 보냄: $routeRequest")

            val response = withContext(Dispatchers.IO) {
                routeService.getRoute(routeRequest)
            }
            Log.d("RouteProcessor", "응답 받음: $response")

            response
        } catch (e: Exception) {
            Log.e("RouteProcessor", "Error fetching route", e)
            null
        }
    }
}
