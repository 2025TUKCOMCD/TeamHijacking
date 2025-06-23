package com.example.front.login.service

import com.example.front.login.data.SmartThingsRequest
import com.example.front.login.data.UserRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface UserService {
    //사용자 등록
    @POST("users/register")
    fun registerUser(@Body user: UserRequest): Call<UserRequest>  //요청을 감싸고 응답을 받는 틀?

    // loginId로 사용자 정보 조회
    @GET("users/{loginId}")
    fun getUserByLoginId(@Path("loginId") loginId: String): Call<UserRequest>

    //loginId로 사용자 정보 수정
    @PUT("users/{loginId}")
    fun updateUser(
        @Path("loginId") loginId: String,
        @Body user: UserRequest): Call<UserRequest>

    @GET("smartthings")
    fun getSmartThingsToken(@Query("userId") userId: String): Call<SmartThingsRequest>

}