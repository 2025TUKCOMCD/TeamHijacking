package com.example.front.Login.service

import com.example.front.login.data.SmartThingsRequest
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/smartthings")
    fun getSmartThingsToken(@Query("userId") userId: String): Call<SmartThingsRequest>

}