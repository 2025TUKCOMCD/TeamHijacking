package kr.io.seemore.transportation.service

import kr.io.seemore.transportation.data.searchPath.Route
import kr.io.seemore.transportation.data.searchPath.RouteRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface RealtimeService {
    @POST("/api/realtime")
    suspend fun getRoute(@Body request: RouteRequest): List<Route>
}