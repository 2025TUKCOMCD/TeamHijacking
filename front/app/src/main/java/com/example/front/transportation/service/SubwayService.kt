package com.example.front.transportation.service

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface SubwayService {
    @GET("{apiKey}/json/realtimeStationArrival/0/5/{stationName}")
    suspend fun getRealtimeStationArrival(
        @Path("apiKey") apiKey: String,
        @Path("stationName") stationName: String
    ): ResponseBody
}