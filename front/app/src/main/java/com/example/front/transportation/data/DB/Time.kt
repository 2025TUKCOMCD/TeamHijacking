package com.example.front.transportation.data.DB

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class Time( val year: Int, val month: Int, val day: Int, val hour: Int, val minute: Int, val second: Int) {
    companion object {
        fun now(): Time {
            val calendar = Calendar.getInstance()
            return Time(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, // Calendar.MONTH는 0부터 시작하므로 +1
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)
            )
        }
    }

    // MySQL DATETIME 형식 (YYYY-MM-DD HH:MM:SS) 문자열로 변환하는 함수 추가
    fun toMySQLDateTimeString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        // MySQL DATETIME은 일반적으로 서버의 로컬 타임존을 따릅니다.
        // 클라이언트와 서버의 타임존이 다르면 문제가 될 수 있으니 주의.
        // 필요하다면 sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul") 또는 "UTC" 등으로 명시 가능.

        val calendar = Calendar.getInstance().apply {
            // Time 객체의 필드를 Calendar 객체에 설정
            set(year, month - 1, day, hour, minute, second)
        }
        return sdf.format(calendar.time)
    }

    // (선택 사항) ISO 8601 문자열 변환 함수 (사용할 경우)
    fun toIso8601String(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC") // ISO 8601은 보통 UTC 기준
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, day, hour, minute, second)
        }
        return sdf.format(calendar.time)
    }

}