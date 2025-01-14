package com.example.front.processor

import android.util.Log
import com.example.front.BuildConfig
import com.example.front.data.realtimeBus.DetailInfo
import com.example.front.data.realtimeBus.RealtimeRouteResponse
import com.example.front.data.realtimeStation.RealtimeStation
import com.example.front.data.searchPath.Path
import com.example.front.data.searchPath.PathRouteResult
import com.example.front.data.searchPath.SearchPubTransPathTResponse
import com.example.front.service.RouteService
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

            val startStationIDsArray = paths.map { path ->
                path.subPath.filter { it.trafficType == 2 }
                    .mapNotNull { subPath ->
                        subPath.startID
                    }.toTypedArray()
            }

            val formattedStartStationIDs = startStationIDsArray.mapIndexed { i, startStationIDs ->
                "Path $i: ${startStationIDs.joinToString(", ")}"
            }.joinToString("\n")

            Log.d("RouteProcessor", formattedStartStationIDs)

            val busIDsArray = paths.map { path ->
                path.subPath.filter { it.trafficType == 2 }
                    .mapNotNull { subPath ->
                        subPath.lane?.firstOrNull()?.busID
                    }.toTypedArray()
            }

            sortedPaths.map { (path, _) ->
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
                    totalTime = info.totalTime,
                    transitCount = info.busTransitCount + info.subwayTransitCount,
                    mainTransitTypes = mainTransitTypes,
                    detailedPath = detailedPath,
                    startStationIDs = startStationIDsArray.firstOrNull() ?: arrayOf(),
                    busIDs = busIDsArray.firstOrNull() ?: arrayOf()
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

//    suspend fun fetchRealtimeStation(stationID: Int, busID: Int): RealtimeStation? {
//        return try {
//            val response = withContext(Dispatchers.IO) {
//                routeService.realtimeStation(stationID, busID.toString(), apiKey = ODsay_APIKEY)
//            }
//
//            val rawJson = response.string()
//            Log.d("RouteProcessor", "Realtime Station Raw Response: $rawJson")
//
//            gson.fromJson(rawJson, RealtimeStation::class.java)
//        } catch (e: Exception) {
//            Log.e("RouteProcessor", "Error fetching real-time station data", e)
//            null
//        }
//    }
//
//    fun fetchRealtimeStationData(pathRouteResult: PathRouteResult, pathIndex: Int) {
//        Log.d("RouteProcessor", "Fetching real-time data for path index: $pathIndex")
//        CoroutineScope(Dispatchers.IO).launch {
//            pathRouteResult.startStationIDs.zip(pathRouteResult.busIDs).forEach { (startStationID, busID) ->
//                try {
//                    val response = fetchRealtimeStation(startStationID, busID)
//                    Log.d("TransportationMainActivity", "Realtime Station Data: $response")
//                } catch (e: Exception) {
//                    Log.e("TransportationMainActivity", "Error fetching real-time station data", e)
//                }
//            }
//        }
//    }

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