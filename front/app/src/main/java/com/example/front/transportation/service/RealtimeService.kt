package com.example.front.transportation.service

import com.example.front.transportation.data.searchPath.Route
import com.example.front.transportation.data.searchPath.RouteRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface RealtimeService {
    @POST("/api/realtime")
    suspend fun getRoute(@Body request: RouteRequest): List<Route>
}