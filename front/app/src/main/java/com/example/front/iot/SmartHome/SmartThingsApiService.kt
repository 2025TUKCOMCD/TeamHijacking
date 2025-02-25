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

}