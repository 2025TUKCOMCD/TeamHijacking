package com.example.front.transportation.processor

import RouteService
import android.util.Log
import com.example.front.BuildConfig
import com.example.front.transportation.data.DB.DBSave
import com.example.front.transportation.data.searchPath.Route
import com.example.front.transportation.data.searchPath.RouteRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.front.transportation.data.DB.Time

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
//    suspend fun DBSaveRoute(startName : String, endName : String, startLat: Double, startLng: Double,
//                            endLat: Double, endLng: Double, savedRouteName: String): Unit? {
//        return try {
//
//            val DBSave = DBSave(startName, endName, startLat, startLng, endLat, endLng, savedRouteName)
//            Log.d("RouteProcessor", "요청 보냄: $DBSave")
//
//            val response = withContext(Dispatchers.IO) {
//                routeService.saveDBRoute(DBSave)
//            }
//            Log.d("RouteProcessor", "응답 받음: $response")
//
//            response
//        } catch (e: Exception) {
//            Log.e("RouteProcessor", "Error fetching route", e)
//            null
//        }
//    }
    suspend fun DBSaveRoute(startName: String,endName: String, startLat: Double, startLng: Double, endLat: Double, endLng: Double, savedRouteName: String): Boolean { // 저장 성공 여부만 반환하도록 Boolean으로 가정
        return try {
            // 현재 시간을 밀리초 타임스탬프로 가져옴

            val currentTime = Time.now() // Time 객체 생성
            val mysqlDateTimeString = currentTime.toMySQLDateTimeString() // <-- MySQL DATETIME 형식 문자열로 변환

            val dbSave = DBSave(
                startName,
                endName,
                whenFirstGo = mysqlDateTimeString,   // 처음 저장하는 것이므로 현재 시간
                whenLastGo = mysqlDateTimeString,    // 처음 저장하는 것이므로 현재 시간
                userouteCount = 1,                 // 처음 저장하는 것이므로 1
                isFavorite = false,                 // 처음 저장하는 것이므로 false
                startLat,
                startLng,
                endLat,
                endLng,
                savedRouteName    // 저장된 경로 이름
            )
            Log.d("RouteProcessor", "DB 저장 요청 보냄: $dbSave")

            val response = withContext(Dispatchers.IO) {
                routeService.saveDBRoute(dbSave)
            }
            Log.d("RouteProcessor", "DB 저장 응답 받음: $response")

            // 백엔드가 성공 시 true를 반환한다고 가정
            response // 백엔드 API의 실제 반환 타입에 따라 이 부분 수정 필요
        } catch (e: Exception) {
            Log.e("RouteProcessor", "Error saving route to DB", e) // 로그 메시지 변경
            false // 실패 시 false 반환
        }
    }
}
