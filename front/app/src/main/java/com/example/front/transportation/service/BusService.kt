package com.example.front.transportation.service

import okhttp3.ResponseBody
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
    ): ResponseBody

    @GET("buspos/getBusPosByRouteSt")
    suspend fun getBusPosByRouteSt(
        @Query("ServiceKey") ServiceKey: String,
        @Query("busRouteId") busRouteId: Int,
        @Query("startOrd") startOrd: Int,
        @Query("endOrd") endOrd: Int,
        @Query("resultType") resultType: String
    ): ResponseBody

    @GET("6410000/busarrivalservice/v2/getBusArrivalItemv2")
    suspend fun getBusArrivalItemv2(
        @Query("serviceKey") serviceKey: String,
        @Query("stationId") stationId: Int,
        @Query("routeId") routeId: Int,
        @Query("staOrder") staOrder: Int,
        @Query("format") format: String
    ): ResponseBody

}