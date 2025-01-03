package com.example.front.data

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RouteProcessor {
    private const val BASE_URL = "https://api.odsay.com/v1/api/"
    private const val API_KEY = "{API_KEY}" // 실제 API 키로 교체하세요.
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
    ): String {
        return try {
            val response = withContext(Dispatchers.IO) {
                routeService.searchPubTransPathT(startLat, startLng, endLat, endLng, API_KEY)
            }

            // 원시 JSON 응답 데이터 출력
            val rawJson = response.string()
            Log.d("RouteProcessor", "API Raw Response: $rawJson")

            // JSON 데이터 파싱
            val parsedResponse = gson.fromJson(rawJson, AccessibleResponse::class.java)
            val paths = parsedResponse.result?.path

            if (paths == null || paths.isEmpty()) {
                Log.d("RouteProcessor", "No paths found in API response.")
                return "No routes found"
            }

            // 경로 데이터 처리
            paths.joinToString("\n") { path ->
                val info = path.info
                val subPaths = path.subPath

                // 세부 경로 처리
                val subPathDetails = subPaths.joinToString(", ") { subPath ->
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

                // 요약 정보와 세부 경로를 반환
                "총 소요 시간: ${info.totalTime}분, 도보 거리: ${info.totalWalk}m, 세부 경로: $subPathDetails"
            }.also { result ->
                Log.d("RouteProcessor", "Processed Data: $result")
            }
        } catch (e: JsonSyntaxException) {
            Log.e("RouteProcessor", "JSON 문법 오류", e)
            "JSON syntax error"
        } catch (e: Exception) {
            Log.e("RouteProcessor", "Error fetching routes", e)
            "Error fetching routes"
        }
    }
}