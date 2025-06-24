package com.example.front.transportation.processor

import RouteService
import android.util.Log
import com.example.front.BuildConfig
import com.example.front.transportation.data.DB.DBSaveDTO
import com.example.front.transportation.data.DB.GetRouteResponseDTO
import com.example.front.transportation.data.DB.SavedRouteResponseDTO
import com.example.front.transportation.data.searchPath.Route
import com.example.front.transportation.data.searchPath.RouteRequest
import com.example.front.transportation.error.InvalidResponseDataException
import com.example.front.transportation.error.NetworkConnectionException
import com.example.front.transportation.error.ServerErrorException
import com.example.front.transportation.error.UserNotFoundException
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

import java.io.IOException


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
        loginId : String,
        transportRouteKey: Int? = 0,
        isFavorite: Boolean = false
    ): SavedRouteResponseDTO? {
        return try {
            val dbSave = DBSaveDTO(
                startName,
                endName,
                userRouteCount = 1,
                isFavorite = isFavorite,
                startLat,
                startLng,
                endLat,
                endLng,
                savedRouteName,
                loginId,
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

    // DBGetRoute 함수는 이제 오류 발생 시 null 대신 특정 예외를 던집니다.
    suspend fun DBGetRoute(loginId: String): List<GetRouteResponseDTO> {
        Log.d("RouteProcessor", "저장된 DB 경로 요청 보냄: loginId=$loginId")

        return withContext(Dispatchers.IO) {
            try {
                // Retrofit의 suspend 함수는 2xx 응답이 아닐 경우 HttpException을 던지므로,
                // 별도로 isSuccessful 등을 확인할 필요 없이 바로 결과를 받습니다.
                val response: List<GetRouteResponseDTO> = routeService.getSavedRoute(loginId)

                Log.d("RouteProcessor", "저장된 DB 경로 응답 받음: $response")

                // Retrofit의 suspend 함수는 성공 시 null이 아닌 빈 리스트를 반환하는 것이 일반적이므로
                // 이 null 체크 조건은 대부분 도달하지 않습니다. 하지만 방어적인 코드로 유지할 수 있습니다.
                if (response == null) {
                    // 서버가 200 OK를 반환했지만, 응답 본문이 예상치 않게 null인 경우에 해당
                    throw InvalidResponseDataException("서버로부터 유효한 경로 데이터를 받지 못했습니다.")
                }

                response // 성공 시 List<GetRouteResponseDTO> 반환
            } catch (e: HttpException) {
                // HTTP 오류 (4xx, 5xx 등) 처리
                when (e.code()) {
                    404 -> {
                        Log.e("RouteProcessor", "사용자 없음 오류 (HTTP 404): $loginId", e)
                        // UserNotFoundException 생성자에 메시지와 cause(원본 예외)만 전달합니다.
                        throw UserNotFoundException("사용자 ID를 찾을 수 없습니다. (ID: $loginId)", e)
                    }
                    in 500..599 -> {
                        Log.e("RouteProcessor", "서버 내부 오류 (HTTP ${e.code()}): $loginId", e)
                        // ServerErrorException 생성자에 메시지, statusCode, cause(원본 예외)를 전달합니다.
                        // statusCode는 명시적으로 매개변수 이름을 지정하는 것이 좋습니다.
                        throw ServerErrorException(
                            message = "서버 오류가 발생했습니다. (코드: ${e.code()})",
                            statusCode = e.code(),
                            cause = e // 원본 HttpException을 cause로 전달
                        )
                    }
                    else -> {
                        Log.e("RouteProcessor", "예상치 못한 HTTP 오류 (코드: ${e.code()}): $loginId", e)
                        // 모든 예외는 cause를 함께 전달하는 것이 좋습니다.
                        throw Exception("알 수 없는 HTTP 오류: ${e.code()}", e)
                    }
                }
            } catch (e: IOException) {
                // 네트워크 연결 관련 오류 (예: 인터넷 연결 없음, 타임아웃, 호스트를 찾을 수 없음)
                Log.e("RouteProcessor", "네트워크 연결 오류: $loginId", e)
                throw NetworkConnectionException("네트워크 연결에 문제가 있습니다.", e)
            } catch (e: Exception) {
                // 그 외 모든 예상치 못한 오류
                Log.e("RouteProcessor", "DB 경로 가져오기 중 알 수 없는 오류 발생: $loginId", e)
                // ServerErrorException에 cause를 전달하여 원본 오류 추적을 돕습니다.
                throw ServerErrorException("경로를 가져오는 중 알 수 없는 오류가 발생했습니다.", null, e)
            }
        }
    }
}