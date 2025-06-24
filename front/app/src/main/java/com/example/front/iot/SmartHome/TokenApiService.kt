package com.example.front.iot.SmartHome

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TokenApiService {
    @GET("api/smartthings")
    public fun getSmartThingsToken(@Query("userId") userId: String): Call<SmartThingsRequest>

}