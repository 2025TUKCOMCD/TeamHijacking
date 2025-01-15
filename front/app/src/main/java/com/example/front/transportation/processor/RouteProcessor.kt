package com.example.front.transportation.processor

import android.util.Log
import com.example.front.BuildConfig
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
            }.sortedByDescending { it.second }



            sortedPaths.map { (path, _) ->
                val startStationIDsArray = path.subPath.filter { it.trafficType == 2 }
                    .mapNotNull { subPath -> subPath.startID }

                val busIDsArray = path.subPath.filter { it.trafficType == 2 }
                    .mapNotNull { subPath -> subPath.lane?.firstOrNull()?.busID }

                // `routeStationsAndBuses` 리스트 생성
                val routeStationsAndBuses = startStationIDsArray.zip(busIDsArray)

                val info = path.info
                val subPaths = path.subPath

                val mainTransitTypes = subPaths.mapNotNull { subPath ->
                    when (subPath.trafficType) {
                        1 -> "지하철(${subPath.lane?.firstOrNull()?.name})"
                        2 -> "버스(${subPath.lane?.firstOrNull()?.busNo})"
                        3 -> if (subPath.sectionTime != null && subPath.sectionTime >= 5) "도보 (${subPath.sectionTime}분)" else null
                        else -> null
                    }
                }.joinToString(" -> ")

                val detailedPath = subPaths.joinToString(", ") { subPath ->
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
        } catch (e: JsonSyntaxException) {
            Log.e("RouteProcessor", "JSON 문법 오류", e)
            listOf()
        } catch (e: Exception) {
            Log.e("RouteProcessor", "Error fetching routes", e)
            listOf()
        }
    }

    suspend fun fetchRealtimeStation(routeStationsAndBuses: List<Pair<Int, Int>>): List<RealtimeStation?> {
    return try {
        Log.d("RouteProcessor", "Fetching real-time data for routeStationsAndBuses: $routeStationsAndBuses")

        val realtimeStations = routeStationsAndBuses.map { (stationID, busID) ->
            withContext(Dispatchers.IO) {
                try {
                    val response = routeService.realtimeStation(stationID, busID.toString(), apiKey = ODsay_APIKEY)
                    val rawJson = response.string()
                    Log.d("RouteProcessor", "Realtime Station Raw Response: $rawJson")
                    gson.fromJson(rawJson, RealtimeStation::class.java)
                } catch (e: Exception) {
                    Log.e("RouteProcessor", "Error fetching real-time station data for stationID: $stationID, busID: $busID", e)
                    null
                }
            }
        }

        realtimeStations
    } catch (e: Exception) {
        Log.e("RouteProcessor", "Error fetching real-time station data", e)
        listOf()
    }
}


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