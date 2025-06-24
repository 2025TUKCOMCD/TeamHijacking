package com.example.front.transportation.error

class InvalidResponseDataException(message: String = "서버 응답 데이터가 유효하지 않습니다.", cause: Throwable? = null) : Exception(message, cause)
