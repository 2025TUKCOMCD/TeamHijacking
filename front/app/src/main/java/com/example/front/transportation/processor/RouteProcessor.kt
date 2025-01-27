package com.example.front.transportation.processor

import android.util.Log
import com.example.front.BuildConfig
import com.example.front.transportation.data.busLaneDetail.BusLaneDetail
import com.example.front.transportation.data.searchPath.Path
import com.example.front.transportation.data.searchPath.PathRouteResult
import com.example.front.transportation.data.searchPath.SearchPath
import com.example.front.transportation.data.searchPath.SubPath
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
            val parsedResponse = gson.fromJson(rawJson, SearchPath::class.java)
            val paths = parsedResponse.result?.path ?: return listOf()
            val sortedPaths = paths.map { path ->
                val score = calculateRouteScore(path)
                path to score
            }.sortedBy { it.second }

            // 로그 출력
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

            // 경로 결과 생성
            val validPaths = sortedPaths.mapIndexed { index, (path, _) ->
                val filteredSubPaths = path.subPath
                val info = path.info

                // 주요 교통수단 타입
                val mainTransitType = when (path.pathType) {
                    1 -> "지하철"
                    2 -> "버스"
                    3 -> "버스+지하철"
                    else -> "알 수 없음"
                }

                // 상세 경로
                val detailedPath = filteredSubPaths.joinToString(" -> ") { subPath ->
                    when (subPath.trafficType) {
                        1 -> "지하철(${subPath.lane?.firstOrNull()?.name})"
                        2 -> "버스(${subPath.lane?.firstOrNull()?.busNo})"
                        3 -> "도보(${subPath.sectionTime ?: 0}분)"
                        else -> "알 수 없는 교통수단"
                    }
                }

                // 경로별 busDetails 생성
                val routeBusDetails = mutableListOf<String>()
                filteredSubPaths.filter { it.trafficType == 2 }.forEach { subPath ->
                    val busID = subPath.lane?.firstOrNull()?.busID
                    val startLocalStationID = subPath.startLocalStationID
                    val endLocalStationID = subPath.endLocalStationID
                    if (busID != null && startLocalStationID != null && endLocalStationID != null) {
                        val busInfo = fetchBusRouteDetails(busID, startLocalStationID, endLocalStationID)
                        busInfo.forEach { info ->
                            Log.d("RouteProcessor", "$info")
                            routeBusDetails.add(info.map { "${it.key}:${it.value}" }
                                .joinToString(", "))
                        }
                    }
                }

                // 각 경로별 PathRouteResult 생성
                PathRouteResult(
                    totalTime = info.totalTime,
                    transitCount = info.busTransitCount + info.subwayTransitCount,
                    mainTransitTypes = mainTransitType,
                    detailedPath = detailedPath,
                    busDetails = routeBusDetails // 경로별로 분리된 busDetails 추가
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

    private suspend fun fetchBusRouteDetails(busID: Int, startLocalStationID: String, endLocalStationID: String): List<Map<String, String>> {
        return try {
            // 버스 경로 상세 정보 요청
            val response = withContext(Dispatchers.IO) {
                routeService.busLaneDetail(busID, ODsay_APIKEY)
            }

            val rawJson = response.string()

            // busLaneDetail 파싱
            val busLaneDetail = gson.fromJson(rawJson, BusLaneDetail::class.java)
            val busLocalBlID = busLaneDetail.result?.busLocalBlID

            // 반환할 결과 리스트
            val resultList = mutableListOf<Map<String, String>>()

            var startStationInfo: String? = null
            var endStationInfo: String? = null

            busLaneDetail.result?.station?.forEach { station ->
                if (station.localStationID == startLocalStationID) {
                    startStationInfo = "${station.idx + 1}"
                } else if (station.localStationID == endLocalStationID) {
                    endStationInfo = "${station.idx + 1}"
                }
            }

            if (busLocalBlID != null && startStationInfo != null && endStationInfo != null) {
                val busInfo = mapOf(
                "busID" to busID.toString(),
                "startLocalStationID" to (startLocalStationID ?: ""),
                "endLocalStationID" to (endLocalStationID ?: ""),
                "busLocalBlID" to (busLocalBlID ?: ""),
                "startStationInfo" to (startStationInfo ?: ""),
                "endStationInfo" to (endStationInfo ?: "")
            )
            resultList.add(busInfo)
            }

            // 결과 리스트 반환
            resultList
        } catch (e: Exception) {
            Log.e("RouteProcessor", "Error fetching bus route details for busID $busID", e)
            emptyList() // 오류 시 빈 리스트 반환
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