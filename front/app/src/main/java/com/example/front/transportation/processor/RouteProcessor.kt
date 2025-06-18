package com.example.front.transportation.processor

import RouteService
import android.util.Log
import com.example.front.BuildConfig
import com.example.front.transportation.data.DB.DBSaveDTO
import com.example.front.transportation.data.DB.GetRouteResponseDTO
import com.example.front.transportation.data.DB.SavedRouteResponseDTO
import com.example.front.transportation.data.searchPath.Route
import com.example.front.transportation.data.searchPath.RouteRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// 필수 import 추가
import retrofit2.Response // retrofit2.Response 클래스를 import 해야 isSuccessful, body, errorBody 사용 가능
import retrofit2.awaitResponse // Call<T>.awaitResponse() 확장 함수 사용을 위해 필요

object RouteProcessor {
    private const val Host_URL = BuildConfig.Host_URL
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Host_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val routeService: RouteService = retrofit.create(RouteService::class.java)

    suspend fun fetchRoute(startLat: Double, startLng: Double, endLat: Double, endLng: Double): List<Route>? {
        return try {
            val routeRequest = RouteRequest(startLat, startLng, endLat, endLng)
            Log.d("RouteProcessor", "요청 보냄: $routeRequest")

            val response = withContext(Dispatchers.IO) {
                routeService.getRoute(routeRequest)
            }
            Log.d("RouteProcessor", "응답 받음: $response")

            response
        } catch (e: Exception) {
            Log.e("RouteProcessor", "Error fetching route", e)
            null
        }
    }

    suspend fun DBSaveRoute(
        startName: String,
        endName: String,
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        savedRouteName: String,
        transportRouteKey: Int? = 0, // Int? 타입으로 변경
        isFavorite: Boolean = false,     // Boolean 타입으로 변경
    ): SavedRouteResponseDTO? {
        return try {
            val dbSave = DBSaveDTO(
                startName,
                endName,
                userRouteCount = 1, // 필요에 따라 이 값을 동적으로 설정할 수 있습니다.
                isFavorite = isFavorite, // 전달받은 isFavorite 값 사용
                startLat,
                startLng,
                endLat,
                endLng,
                savedRouteName,
                loginId = "3970421203", // 사용자 ID, 실제 앱에서는 로그인한 사용자 ID로 변경 필요
                // DTO에 transportRouteKey와 isSelected 필드가 필요합니다.
                // DBSaveDTO에도 이 필드를 추가해야 합니다.
                transportrouteKey = transportRouteKey,
            )
            Log.d("RouteProcessor", "DB 저장/업데이트 요청 보냄: $dbSave")

            val response: SavedRouteResponseDTO = withContext(Dispatchers.IO) {
                routeService.saveDBRoute(dbSave)
            }
            Log.d("RouteProcessor", "DB 저장/업데이트 응답 받음: $response")

            response
        } catch (e: Exception) {
            Log.e("RouteProcessor", "Error saving/updating route to DB", e)
            null
        }
    }
    suspend fun DBGetRoute(loginId: String): List<GetRouteResponseDTO>? { // loginId를 파라미터로 받음
        return try {
            Log.d("RouteProcessor", "저장된 DB 경로 요청 보냄: loginId=$loginId")

            val response: List<GetRouteResponseDTO> = withContext(Dispatchers.IO) {
                // RouteService 인터페이스에 정의된 getSavedRoutes 함수 호출
                routeService.getSavedRoute(loginId)
            }
            Log.d("RouteProcessor", "저장된 DB 경로 응답 받음: $response")
            response
        } catch (e: Exception) {
            Log.e("RouteProcessor", "DB 경로 가져오기 오류 발생", e)
            null // 오류 발생 시 null 반환
        }
    }
    // suspend fun updateFavoriteStatus()
}