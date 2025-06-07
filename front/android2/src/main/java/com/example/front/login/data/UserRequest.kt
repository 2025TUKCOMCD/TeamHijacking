package com.example.front.login.data

data class UserRequest(
//    val id: Int? = null,   //서버서 주는 필드?
    val name: String = "" ,
    val loginId: String = "",
    val email: String = "",
//    val createAt: String? = null,   // 서버서 자동 생성
//    val updateAt: String? = null    // 서버서 자동 생성 / update
)