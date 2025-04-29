package com.example.front.login.processor

import android.util.Log
import com.example.front.login.data.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/* 실제 API 호출을 처리 하고, 응답 처리 로직을 담당 */
object UserProcessor {
    private val userService = RetrofitClient.instance

    // 사용자 등록 (회원가입)
    fun registerUser(user: User, onResult: (User?) -> Unit) {
        val call = userService.registerUser(user)

        call.enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    Log.d("UserProcessor", "등록 성공: ${response.body()}")
                    onResult(response.body())
                } else {
                    Log.e("UserProcessor", "서버 오류: ${response.code()}")
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("UserProcessor", "통신 실패: ${t.message}")
                onResult(null)
            }
        })
    }

    //추후 사용자 조회, 정보 수정 추가 가능
    fun getUserByLogin(loginId: String, callback: (User?) -> Unit) {
        userService.getUserByLoginId(loginId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    Log.d("UserProcessor", "조회 성공: ${response.body()}")
                    callback(response.body())
                } else {
                    Log.e("UserProcessor", "조회 실패 - 서버 오류: ${response.code()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("UserProcessor", "조회 실패 - 통신 오류: ${t.message}")
                callback(null)
            }
        })
    }

    //사용자 정보 수정
    fun updateUser(user: User, callback: (User?) -> Unit) {
        userService.updateUser(user.loginId, user).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    Log.d("UserProcessor", "수정 성공: ${response.body()}")
                    callback(response.body())
                } else {
                    Log.e("UserProcessor", "수정 실패 - 서버 오류: ${response.code()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("UserProcessor", "수정 실패 - 통신 오류: ${t.message}")
                callback(null)
            }
        })
    }
}