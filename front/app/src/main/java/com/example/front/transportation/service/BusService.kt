package com.example.front.transportation.service

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface BusService {
    @GET("getArrInfoByRoute")
    suspend fun getArrInfoByRoute(
        @Query("serviceKey") serviceKey: String,
        @Query("stId") stId: String,
        @Query("busRouteId") busRouteId: String,
        @Query("ord") ord: String = "1"
    ): ResponseBody
}