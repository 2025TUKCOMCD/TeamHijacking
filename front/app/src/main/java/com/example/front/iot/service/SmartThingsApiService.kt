package com.example.front.iot.service

import com.example.front.iot.data.DeviceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface SmartThingsApiService {
    @GET("v1/devices")
    fun getDevices(
        @Header("Authorization") token: String
    ): Call<DeviceResponse>
}