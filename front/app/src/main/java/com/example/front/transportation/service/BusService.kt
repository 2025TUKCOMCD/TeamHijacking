package com.example.front.transportation.service

import com.example.front.transportation.data.realtimeStation.RealStationResult
import retrofit2.http.GET
import retrofit2.http.Query

interface BusService {
    @GET("realtimeStation")
    suspend fun getRealtimeStation(
        @Query("apiKey") apiKey: String,
        @Query("stationID") stationID: Int,
        @Query("routeIDs") routeIDs: String,
        @Query("stationBase") stationBase: Int = 1,
        @Query("lowBus") lowBus: Int = 0
    ): RealStationResult
}