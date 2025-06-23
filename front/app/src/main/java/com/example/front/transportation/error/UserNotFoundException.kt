package com.example.front.transportation.error

class UserNotFoundException(message: String = "사용자를 찾을 수 없습니다.", cause: Throwable? = null) : Exception(message, cause)
