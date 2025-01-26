package com.example.front.transportation.processor

import android.util.Log
import com.example.front.BuildConfig
import com.example.front.transportation.data.searchPath.Path
import com.example.front.transportation.data.searchPath.PathRouteResult
import com.example.front.transportation.data.searchPath.SearchPubTransPathResponse
import com.example.front.transportation.service.RouteService
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.math.pow

object RouteProcessor {
    private const val BASE_URL = "https://api.odsay.com/v1/api/"
    private const val ODsay_APIKEY: String = BuildConfig.ODsay_APIKEY
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val routeService: RouteService = retrofit.create(RouteService::class.java)

    suspend fun fetchAndProcessRoutes(startLat: Double, startLng: Double, endLat: Double, endLng: Double): List<PathRouteResult> {
        return try {
            val response = withContext(Dispatchers.IO) {
                routeService.searchPubTransPathT(startLat, startLng, endLat, endLng, ODsay_APIKEY)
            }

            val rawJson = response.string()
            val parsedResponse = gson.fromJson(rawJson, SearchPubTransPathResponse::class.java)
            val paths = parsedResponse.result?.path ?: return listOf()

            val processedPaths = paths.map { path ->
                val busDetails = mutableListOf<String>()

                if (path.pathType == 2 || path.pathType == 3) {
                    path.subPath.filter { it.trafficType == 2 }.forEach { subPath ->
                        val busID = subPath.lane?.firstOrNull()?.busID
                        if (busID != null) {
                            val busInfo = fetchBusRouteDetails(busID)
                            busDetails.add("버스(${subPath.lane?.firstOrNull()?.busNo}): $busInfo")
                        }
                    }
                }

                val mainTransitType = when (path.pathType) {
                    1 -> "지하철"
                    2 -> "버스"
                    3 -> "버스+지하철"
                    else -> "알 수 없음"
                }

                val detailedPath = path.subPath.joinToString(" -> ") { subPath ->
                    when (subPath.trafficType) {
                        1 -> "지하철(${subPath.lane?.firstOrNull()?.name})"
                        2 -> "버스(${subPath.lane?.firstOrNull()?.busNo})"
                        3 -> "도보(${subPath.sectionTime ?: 0}분)"
                        else -> "알 수 없는 교통수단"
                    }
                }

                PathRouteResult(
                    totalTime = path.info.totalTime,
                    transitCount = path.info.busTransitCount + path.info.subwayTransitCount,
                    mainTransitTypes = mainTransitType,
                    detailedPath = detailedPath,
                    busDetails = busDetails
                )
            }
            processedPaths
        } catch (e: JsonSyntaxException) {
            Log.e("RouteProcessor", "JSON 문법 오류", e)
            listOf()
        } catch (e: Exception) {
            Log.e("RouteProcessor", "Error fetching routes", e)
            listOf()
        }
    }

    private suspend fun fetchBusRouteDetails(busID: Int): String {
        return try {
            val response = withContext(Dispatchers.IO) {
                routeService.busLaneDetail(busID, ODsay_APIKEY)
            }
            val rawJson = response.string()
            Log.d("RouteProcessor", "Bus Route Details for busID $busID: $rawJson")
            rawJson
        } catch (e: Exception) {
            Log.e("RouteProcessor", "Error fetching bus route details for busID $busID", e)
            "상세 정보 불러오기 실패"
        }
    }

    private fun calculateRouteScore(path: Path): Double {
        val info = path.info

        val totalTransfers = info.busTransitCount + info.subwayTransitCount
        val transferScore = if (totalTransfers > 0) {
            1.5.pow(totalTransfers)
        } else {
            1.0
        }

        val timeScore = (info.totalTime / 60) * 1.0
        val busWeight = if (totalTransfers > 0) {
            info.busTransitCount.toDouble() / totalTransfers
        } else 0.0

        val walkFactor = (info.totalWalk / 1000.0) * 1.0

        return (0.4 * transferScore) +
                (0.3 * timeScore) +
                (0.2 * busWeight) +
                (0.1 * walkFactor)
    }
}
