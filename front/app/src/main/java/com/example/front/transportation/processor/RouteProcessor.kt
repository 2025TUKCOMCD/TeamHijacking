package com.example.front.transportation.processor

import android.util.Log
import com.example.front.BuildConfig
import com.example.front.transportation.data.realtimeStation.RealStationResult
import com.example.front.transportation.data.realtimeStation.RealtimeStation
import com.example.front.transportation.data.searchPath.Path
import com.example.front.transportation.data.searchPath.PathRouteResult
import com.example.front.transportation.data.searchPath.SearchPubTransPathTResponse
import com.example.front.transportation.service.RouteService
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.pow

object RouteProcessor {
    private const val BASE_URL = "https://api.odsay.com/v1/api/"
    private const val ODsay_APIKEY: String = BuildConfig.ODsay_APIKEY
    private val gson = Gson()
    private val client = OkHttpClient.Builder().build()

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
            Log.d("RouteProcessor", "API Raw Response: $rawJson")
            val parsedResponse = gson.fromJson(rawJson, SearchPubTransPathTResponse::class.java)
            val paths = parsedResponse.result?.path ?: return listOf()
            val sortedPaths = paths.map { path ->
                val score = calculateRouteScore(path)
                path to score
            }.sortedBy { it.second }
            sortedPaths.forEachIndexed { index, (path, score) ->
                Log.d("RouteProcessor", "Sorted Path $index: Score = $score")
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
            }

            val filteredPaths = sortedPaths.filter { (path, _) ->
                path.subPath.none { subPath ->
                    subPath.lane?.firstOrNull()?.type == 4
                }
            }

            val validPaths = filteredPaths.map { (path, score) ->
                val filteredSubPaths = path.subPath.filter { subPath ->
                    subPath.lane?.firstOrNull()?.type != 4
                }

                val startStationIDsArray = filteredSubPaths.filter { it.trafficType == 2 }
                    .mapNotNull { subPath -> subPath.startID }

                val busIDsArray = filteredSubPaths.filter { it.trafficType == 2 }
                    .mapNotNull { subPath -> subPath.lane?.firstOrNull()?.busID }

                val routeStationsAndBuses = startStationIDsArray.zip(busIDsArray)

                val info = path.info
                val mainTransitTypes = filteredSubPaths.mapNotNull { subPath ->
                    when (subPath.trafficType) {
                        1 -> "지하철(${subPath.lane?.firstOrNull()?.name})"
                        2 -> "버스(${subPath.lane?.firstOrNull()?.busNo})"
                        3 -> if (subPath.sectionTime != null && subPath.sectionTime >= 5) "도보 (${subPath.sectionTime}분)" else null
                        else -> null
                    }
                }.joinToString(" -> ")

                val detailedPath = filteredSubPaths.joinToString(", ") { subPath ->
                    when (subPath.trafficType) {
                        1 -> {
                            val lane = subPath.lane?.firstOrNull()
                            "지하철: ${lane?.name} (${subPath.startName} → ${subPath.endName}), 노선명: ${lane?.name}"
                        }

                        2 -> {
                            val lane = subPath.lane?.firstOrNull()
                            "버스: ${lane?.busNo} (${subPath.startName} → ${subPath.endName}), 버스 코드: ${lane?.busID}"
                        }

                        3 -> "도보: ${subPath.distance}m"
                        else -> "알 수 없는 교통수단"
                    }
                }
                Log.d("RouteProcessor", "Detailed Path: $detailedPath")

                PathRouteResult(
                    routeStationsAndBuses = routeStationsAndBuses,
                    totalTime = info.totalTime,
                    transitCount = info.busTransitCount + info.subwayTransitCount,
                    mainTransitTypes = mainTransitTypes,
                    detailedPath = detailedPath
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

    suspend fun fetchRealtimeStation(routeStationsAndBuses: List<Pair<Int, Int>>): List<RealStationResult?> {
        return try {
            val realtimeStations = routeStationsAndBuses.mapNotNull { (stationID, busID) ->
                withContext(Dispatchers.IO) {
                    try {
                        val response = routeService.realtimeStation(stationID, busID.toString(), apiKey = ODsay_APIKEY)
                        val rawJson = response.string()
                        Log.d("RouteProcessor", "Realtime Station Raw Response: $rawJson")
                        val realtimeStation = gson.fromJson(rawJson, RealtimeStation::class.java)
                        val real = realtimeStation.result.real.firstOrNull()
                        val arrival1 = real?.arrival1
                        val arrival2 = real?.arrival2
                        Log.d("RouteProcessor", "Arrival1: $arrival1")
                        Log.d("RouteProcessor", "Arrival2: $arrival2")
                        listOfNotNull(
                            arrival1?.let {
                                RealStationResult(
                                    leftStation = it.leftStation,
                                    arrivalSec = it.arrivalSec,
                                    busStatus = it.busStatus,
                                    endBusYn = it.endBusYn,
                                    lowBusYn = it.lowBusYn,
                                    fulCarAt = it.fulCarAt) },
                            arrival2?.let {
                                RealStationResult(
                                    leftStation = it.leftStation,
                                    arrivalSec = it.arrivalSec,
                                    busStatus = it.busStatus,
                                    endBusYn = it.endBusYn,
                                    lowBusYn = it.lowBusYn,
                                    fulCarAt = it.fulCarAt ) }
                        )
                    } catch (e: Exception) {
                        Log.e("RouteProcessor", "Error fetching real-time station data for stationID: $stationID, busID: $busID", e)
                        null
                    }
                }
            }.flatMap { it }
            realtimeStations
        } catch (e: Exception) {
            Log.e("RouteProcessor", "Error fetching real-time station data", e)
            listOf()
        }
    }

    suspend fun fetchRealtimeLocation() {
        // Implementation needed
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