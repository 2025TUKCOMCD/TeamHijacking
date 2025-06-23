package com.example.front.transportation.error

class ServerErrorException(message: String = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", statusCode: Int? = null, cause: Throwable? = null) : Exception(message, cause)
