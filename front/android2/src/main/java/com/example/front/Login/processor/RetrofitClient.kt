package com.example.front.login.processor

import com.example.front.BuildConfig
import com.example.front.login.service.UserService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    /*Retrofit.Builder 이용 서버 기본 URL 설정
    * JSON 데이터 자동 으로, 객체로 변환 - Converter
    * 또, Retrofit.create() 이용해 Interface 구현체 생성
    * => Retrofit 과 Server 간의 통신 준비 세팅을 하는 공간*/

    private const val HOST_URL = BuildConfig.HOST_URL

    val instance: UserService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(HOST_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(UserService::class.java)
    }
}