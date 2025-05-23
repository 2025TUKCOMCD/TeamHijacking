package com.example.front.login.service

import com.example.front.login.data.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.GET

interface UserService {
    //사용자 등록
    @POST("/users/register")
    fun registerUser(@Body user: User): Call<User>  //요청을 감싸고 응답을 받는 틀?

    // loginId로 사용자 정보 조회
    @GET("/users/{loginId}")
    fun getUserByLoginId(@Path("loginId") loginId: String): Call<User>

    //loginId로 사용자 정보 수정
    @GET("/users/{loginId}")
    fun updateUser(
        @Path("loginId") loginId: String,
        @Body user: User): Call<User>
}