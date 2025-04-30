package com.example.front.iot.SmartHome

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface SmartThingsApiService {
    @GET("v1/devices")
    fun getDevices(
        @Header("Authorization") token: String
    ): Call<DeviceResponse>

    @POST("v1/devices/{deviceId}/commands")
    fun sendCommand(
        @Path("deviceId") deviceId: String,
        @Body command: CommandBody,
        @Header("Authorization") token: String
    ): Call<Unit>

    @GET("devices/{deviceId}/status")
    fun getDeviceStatus(
        @Path("deviceId") deviceId: String,
        @Header("Authorization") token: String
    ): Call<DeviceStatusResponse>

    // ✅ 무드등 밝기(광도) 조절 API
    @POST("v1/devices/{deviceId}/commands")
    fun setBrightness(
        @Path("deviceId") deviceId: String,
        @Body command: CommandBody,
        @Header("Authorization") token: String
    ): Call<Unit>

    // ✅ 무드등 색상(채도) 조절 API
    @POST("v1/devices/{deviceId}/commands")
    fun setColor(
        @Path("deviceId") deviceId: String,
        @Body command: CommandBody,
        @Header("Authorization") token: String
    ): Call<Unit>
}