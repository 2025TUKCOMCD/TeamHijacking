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
                // searchPubTransPathT get 호출
                routeService.searchPubTransPathT(startLat, startLng, endLat, endLng, ODsay_APIKEY)
            }

            val rawJson = response.string()

            // gson으로 Data Class 객체화
            val parsedResponse = gson.fromJson(rawJson, SearchPubTransPathResponse::class.java)
            val paths = parsedResponse.result?.path ?: return listOf()

            // 정렬 경로 지정
            val sortedPaths = paths.map { path ->
                val score = calculateRouteScore(path)
                path to score
            }.sortedBy { it.second }

            // 로그 호출
            sortedPaths.forEachIndexed { index, (path, score) ->
                Log.d("RouteProcessor", "--- 경로 $index 시작 ---")
                Log.d("RouteProcessor", "Score: $score")
                Log.d("RouteProcessor", "Transit Count: ${path.info.busTransitCount + path.info.subwayTransitCount} 회")
                Log.d("RouteProcessor", "Total Time: ${path.info.totalTime} 분")
                Log.d("RouteProcessor", "Main Transit Types: ${
                    path.subPath.joinToString(" -> ") { subPath ->
                        when (subPath.trafficType) {
                            1 -> "지하철(${subPath.lane?.firstOrNull()?.name})"
                            2 -> "버스(${subPath.lane?.firstOrNull()?.busNo})"
                            3 -> "도보 (${subPath.sectionTime ?: 0}분)"
                            else -> "알 수 없는 교통수단"
                        }
                    }
                }")
                Log.d("RouteProcessor", "--- 경로 $index 끝 ---")
            }

            //
            val validPaths = sortedPaths.map { (path, _) ->
                val filteredSubPaths = path.subPath
                val info = path.info

                val mainTransitType = when (path.pathType) {
                    1 -> "지하철"
                    2 -> "버스"
                    3 -> "버스+지하철"
                    else -> "알 수 없음"
                }

                val detailedPath = filteredSubPaths.joinToString(", ") { subPath ->
                    when (subPath.trafficType) {
                        1 -> "지하철"
                        2 -> "버스"
                        3 -> "도보"
                        else -> "알 수 없는 교통수단"
                    }
                }
//                if (path.pathType == 2 || path.pathType == 3) {
//                    filteredSubPaths.filter { it.trafficType == 2 }.forEach { subPath ->
//                        val busID = subPath.lane?.firstOrNull()?.busID
//                        if (busID != null) {
//                            fetchBusRouteDetails(busID)
//                        }
//                    }
//                }

                PathRouteResult(
                    totalTime = info.totalTime,
                    transitCount = info.busTransitCount + info.subwayTransitCount,
                    mainTransitTypes = mainTransitType,
                    detailedPath = detailedPath ,
                )
            }
            validPaths
        } catch (e: JsonSyntaxException) {
            Log.e("RouteProcessor", "JSON 문법 오류", e)
            listOf()
        } catch (e: Exception) {
            Log.e("RouteProcessor", "Error fetching routes", e)
            listOf()
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
