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

            val topPaths = sortedPaths.take(3)

            // 경로 결과 생성
            val validPaths = topPaths.mapIndexed { index, (path, _) ->
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
                val detailedPath = filteredSubPaths.map { subPath ->
                    when (subPath.trafficType) {
                        1 -> "${subPath.lane?.firstOrNull()?.name} (${subPath.startName}), (${subPath.endName})"
                        2 -> "${subPath.lane?.firstOrNull()?.busNo}번 (${subPath.startName}), (${subPath.endName})"
                        3 -> if (subPath.sectionTime ?: 0 > 0) "도보(${subPath.sectionTime}분)" else ""
                        else -> "알 수 없는 교통수단"
                    }
                }.filter { it.isNotEmpty() }.joinToString(" -> ")

                // 경로별 subwayDetails 생성

                // 경로별 busDetails 생성
                val routeBusDetails = mutableListOf<String>()
                filteredSubPaths.filter { it.trafficType == 2 }.forEach { subPath ->
                    val busID = subPath.lane?.firstOrNull()?.busID
                    val startLocalStatiionID = subPath.startLocalStationID
                    val endLocalStationID = subPath.endLocalStationID
                    val startStationID = subPath.startID
                    val endStationID = subPath.endID

                    if (busID != null && startStationID != null && endStationID != null && startLocalStatiionID != null && endLocalStationID != null) {
                        val busInfo = fetchBusRouteDetails(busID, startStationID, endStationID, startLocalStatiionID, endLocalStationID)
                        if (busInfo.isNotEmpty()) {
                            busInfo.forEach { info ->
                                Log.d("RouteProcessor", "$info")
                                routeBusDetails.add(info.map { "${it.key}:${it.value}" }.joinToString(", "))
                            }
                        } else {
                            routeBusDetails.add("버스 상세 정보를 불러올 수 없습니다")
                        }
                    } else {
                        routeBusDetails.add("버스 정보 불완전")
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

    private suspend fun fetchBusRouteDetails(busID: Int, startStationID: Int, endStationID:Int, startLocalStationID: String,endLocalStationID: String): List<Map<String, String>> {
        Log.d("RouteProcessor", "Fetching bus route details for busID $busID")
        return try {
            // 버스 경로 상세 정보 요청
            val response = withContext(Dispatchers.IO) {
                routeService.busLaneDetail(busID, ODsay_APIKEY)
            }

            val rawJson = response.string()
            Log.d("RouteProcessor", "Raw response received: $rawJson")
            // busLaneDetail 파싱
            val busLaneDetail = gson.fromJson(rawJson, BusLaneDetail::class.java)
            val busLocalBlID = busLaneDetail.result?.busLocalBlID
            var busNo = busLaneDetail.result?.busNo
            // 반환할 결과 리스트
            val resultList = mutableListOf<Map<String, String>>()

            var startStationInfo: String? = null
            var endStationInfo: String? = null
            var startStationName: String? = null

            busLaneDetail.result?.station?.forEach { station ->
                if (station.stationID == startStationID) {
                    if (startStationInfo == null || station.idx < startStationInfo!!.toInt() - 1) {
                        startStationInfo = "${station.idx + 1}"
                        startStationName = station.stationName
                    }
                } else if (station.stationID == endStationID) {
                    endStationInfo = "${station.idx + 1}"
                }
            }

            if (startStationInfo != null && endStationInfo != null) {
                val startIndex = startStationInfo!!.toInt()
                val endIndex = endStationInfo!!.toInt()
                if (startIndex >= endIndex) {
                    Log.e("RouteProcessor", "Invalid station order: startStation index is not less than endStation index")
                    // Handle the invalid order case here, e.g., by throwing an exception or returning an error
                }
            }

            if (busLocalBlID != null && startStationInfo != null && endStationInfo != null) {
                val busInfo = mapOf(
                    "stationName" to (startStationName ?: ""),
                    "busNo" to busNo.toString(),
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