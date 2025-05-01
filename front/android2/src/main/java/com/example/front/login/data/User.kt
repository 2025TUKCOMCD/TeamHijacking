package com.example.front.login.data

data class User(
    val id: Int? = null,   //서버에서 내려주는? 필드?
    val name: String,
    val loginId: String,
    val createAt: String? = null,   // 서버에서 자동 생성
    val updateAt: String? = null    // 서버에서 자동 생성 / 업데이트
)