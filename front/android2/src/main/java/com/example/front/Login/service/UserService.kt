package com.example.front.login.service

import com.example.front.login.data.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserService {
    @POST("/users/register")
    fun registerUser(@Body user: User): Call<User>  //요청을 감싸고 응답을 받는 틀?
}