package com.example.front.transportation.service

import com.example.front.transportation.data.realtimeStation.RealStationResult
import retrofit2.http.GET
import retrofit2.http.Query

interface BusService {
    @GET("arrive/getArrInfoByRoute")
    suspend fun getArrInfoByRoute(
        @Query("ServiceKey") ServiceKey: String,
        @Query("stId") stId: Int,
        @Query("busRouteId") busRouteId: Int,
        @Query("ord") ord: Int,
        @Query("resultType") resultType: String
    ): RealStationResult

    @GET("buspos/getBusPosByRouteSt")
    suspend fun getBusPosByRouteSt(
        @Query("ServiceKey") ServiceKey: String,
        @Query("busRouteId") busRouteId: Int,
        @Query("startOrd") startOrd: Int,
        @Query("endOrd") endOrd: Int,
        @Query("resultType") resultType: String
    ): RealStationResult
}