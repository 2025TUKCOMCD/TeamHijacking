package com.example.front.iot.processor

import com.example.front.iot.service.SmartThingsApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.smartthings.com/"

    val instance: SmartThingsApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(SmartThingsApiService::class.java)
    }
}
