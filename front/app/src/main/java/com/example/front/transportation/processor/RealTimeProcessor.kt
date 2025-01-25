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
import java.util.concurrent.TimeUnit

object RealTimeProcessor {


    suspend fun fetchRealtimeStation(routeStationsAndBuses: List<Pair<Int, Int>>) {

    }
}