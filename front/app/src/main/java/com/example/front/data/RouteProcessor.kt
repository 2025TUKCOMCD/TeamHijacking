package com.example.front.data

import android.util.Log
import com.example.front.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RouteProcessor {
    private const val BASE_URL = "https://api.odsay.com/v1/api/"

    private const val ODsay_APIKEY : String = BuildConfig.ODsay_APIKEY
    private val gson = Gson()
    private val client = OkHttpClient.Builder().build()

    // Retrofit 서비스 초기화
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val routeService: RouteService = retrofit.create(RouteService::class.java)

    // 경로 데이터 가져오기 및 처리
    suspend fun fetchAndProcessRoutes(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): List<PathRouteResult> {
        return try {
            val response = withContext(Dispatchers.IO) {
                routeService.searchPubTransPathT(startLat, startLng, endLat, endLng, ODsay_APIKEY)
            }

            // 원시 JSON 응답 데이터 출력
            val rawJson = response.string()
            Log.d("RouteProcessor", "API Raw Response: $rawJson")

            // JSON 데이터 파싱
            val parsedResponse = gson.fromJson(rawJson, SearchPubTransPathTResponse::class.java)

            val paths = parsedResponse.result?.path

            if (paths == null || paths.isEmpty()) {
                Log.d("RouteProcessor", "No paths found in API response.")
                return listOf()
            }

            // 경로 데이터 처리 및 점수 계산 후 정렬
            val sortedPaths = paths.map { path ->
                val score = calculateRouteScore(path)
                path to score
            }.sortedByDescending { it.second }

            // 경로 출력
            sortedPaths.map { (path, _) ->
                val info = path.info
                val subPaths = path.subPath

                // 주요 교통수단 요약
                val mainTransitTypes = subPaths.mapNotNull { subPath ->
                    when (subPath.trafficType) {
                        1 -> "지하철(${subPath.sectionTime}분)"
                        2 -> "버스(${subPath.sectionTime}분)"
                        // 도보는 5분이상일 경우만 표시
                        3 -> if (subPath.sectionTime != null && subPath.sectionTime >= 5) "도보 (${subPath.sectionTime}분)" else null
                        else -> null
                    }
                }.joinToString(" -> ")

                // 세부 경로 처리
                val detailedPath = subPaths.joinToString(", ") { subPath ->
                    when (subPath.trafficType) {
                        1 -> { // 지하철
                            val lane = subPath.lane?.firstOrNull()
                            "지하철: ${lane?.name} (${subPath.startName} → ${subPath.endName})"
                        }
                        2 -> { // 버스
                            val lane = subPath.lane?.firstOrNull()
                            "버스: ${lane?.busNo} (${subPath.startName} → ${subPath.endName})"
                        }
                        3 -> { // 도보
                            "도보: ${subPath.distance}m"
                        }
                        else -> "알 수 없는 교통수단"
                    }
                }
                // 경로 결과 반환
                PathRouteResult(
                    totalTime = info.totalTime,
                    transitCount = info.busTransitCount + info.subwayTransitCount,
                    mainTransitTypes = mainTransitTypes,
                    detailedPath = detailedPath
                )
            }

        } catch (e: JsonSyntaxException) {
            Log.e("RouteProcessor", "JSON 문법 오류", e)
            listOf()
        } catch (e: Exception) {
            Log.e("RouteProcessor", "Error fetching routes", e)
            listOf()
        }
    }

    // 경로 점수 계산 함수
    private fun calculateRouteScore(path: Path): Double {
        val info = path.info
        val walkFactor = if (info.totalWalk > 500) 0.0 else 0.5

        return (0.30 * (info.busTransitCount + info.subwayTransitCount)) +
                (0.25 * if (info.subwayTransitCount > 0) 1 else 0) +
                (0.20 * (60.0 / info.totalTime)) +
                (0.15 * if (info.busTransitCount > 0) 1 else 0) +
                (0.10 * walkFactor)
    }
}