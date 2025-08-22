package kr.io.seemore.login.service

import kr.io.seemore.iot.smartHome.DeviceResponse
import kr.io.seemore.iot.smartHome.DeviceStatusResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

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