package com.example.front.data

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface RouteService {
    @GET("searchPubTransPathT")
    suspend fun searchPubTransPathT(
        @Query("SY") startLat: Double,   // 출발 위도
        @Query("SX") startLng: Double,  // 출발 경도
        @Query("EY") endLat: Double,    // 도착 위도
        @Query("EX") endLng: Double,    // 도착 경도
        @Query("apiKey") apiKey: String // API 키
    ): ResponseBody

    @GET("realtimeRoute")
    suspend fun realtimeRoute(
        @Query("busID") busID: String,
        @Query("busBase") busBase: Int? = 0,
        @Query("lowBus") lowBus: Int? = 0,
        @Query("apiKey") apiKey: String
    ): ResponseBody
}