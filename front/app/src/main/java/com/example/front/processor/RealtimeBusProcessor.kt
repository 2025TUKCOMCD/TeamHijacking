package com.example.front.processor

import android.util.Log
import com.example.front.BuildConfig
import com.example.front.data.realtimeBus.DetailInfo
import com.example.front.data.realtimeBus.RealtimeRouteResponse
import com.example.front.service.RouteService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RealtimeBusProcessor {
    private const val ODsay_APIKEY: String = BuildConfig.ODsay_APIKEY
    private val gson = Gson()

    suspend fun fetchDetailInfo(busID: String, routeService: RouteService): DetailInfo? {
        return try {
            // 실시간 경로 정보 가져오기
            val response = withContext(Dispatchers.IO) {
                routeService.realtimeRoute(busID, apiKey = ODsay_APIKEY)
            }

            // JSON 응답 파싱
            val rawJson = response.string()
            Log.d("DetailInfoProcessor", "Realtime Route Raw Response: $rawJson")

            val realtimeRoute = gson.fromJson(rawJson, RealtimeRouteResponse::class.java)
            val realtimeInfo = realtimeRoute.result?.real?.firstOrNull()

            // DetailInfo 객체 생성
            realtimeInfo?.let {
                DetailInfo(
                    fromStationId = it.fromStationId?.toInt() ?: 0,
                    toStationId = it.toStationId?.toInt() ?: 0,
                    positionX = it.positionX?.toInt() ?: 0,
                    positionY = it.positionY?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            Log.e("DetailInfoProcessor", "Error fetching detail info", e)
            null
        }
    }
}