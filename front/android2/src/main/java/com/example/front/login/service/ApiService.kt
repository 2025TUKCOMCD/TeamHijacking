package com.example.front.login.service

import com.example.front.iot.smartHome.DeviceResponse
import com.example.front.iot.smartHome.DeviceStatusResponse
import com.example.front.login.data.SmartThingsRequest
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("v1/devices")
    fun getDevices(
        @Header("Authorization") token: String
    ): Call<DeviceResponse>

    @GET("devices/{deviceId}/status")
    fun getDeviceStatus(
        @Path("deviceId") deviceId: String,
        @Header("Authorization") token: String
    ): Call<DeviceStatusResponse>
}