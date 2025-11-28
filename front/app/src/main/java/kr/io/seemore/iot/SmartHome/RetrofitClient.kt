package kr.io.seemore.iot.SmartHome

import kr.io.seemore.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.smartthings.com/"
    private const val HOST_URL = BuildConfig.Host_URL

    val instance: SmartThingsApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(SmartThingsApiService::class.java)
    }

    val tokenapiService : TokenApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(HOST_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(TokenApiService::class.java)
    }
}
