package com.example.front.transportation.error

import java.io.IOException

class NetworkConnectionException(message: String = "네트워크 연결에 문제가 있습니다.", cause: Throwable? = null) : IOException(message, cause)
