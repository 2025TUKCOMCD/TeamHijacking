package com.example.front.login.processor

import android.util.Log
import com.example.front.login.data.UserRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/* 실제 API 호출을 처리 하고, 응답 처리 로직을 담당 */
object UserProcessor {


    private val userService = RetrofitClient.userService

    //오류 여기서 가능성
    fun registerUser(user: UserRequest, callback: (Response<UserRequest>) -> Unit) {
        Log.d("login", "registerUser 실행됨.")
        val call = userService.registerUser(user)

        Log.d("login", "userService.registerUser(user) 실행됨")
        call.enqueue(object : Callback<UserRequest> {
            override fun onResponse(call: Call<UserRequest>, response: Response<UserRequest>) {
                if (response.isSuccessful) {
                    Log.d("response","${response}")
                    Log.d("UserProcessor", "등록 성공: ${response.body()}")
                } else {
                    Log.w("UserProcessor", "등록 실패 - 상태 코드: ${response.code()}")
                }
                callback(response)  // 성공 여부 관계 없이 Response 객체 전달
            }

            override fun onFailure(call: Call<UserRequest>, t: Throwable) {
                Log.e("UserProcessor", "통신 실패: ${t.message}")
                callback(Response.error(500, okhttp3.ResponseBody.create(null, "통신 실패")))
            }
        })
    }



    //추후 사용자 조회, 정보 수정 추가 가능
    fun getUserByLogin(loginId: String, callback: (UserRequest?) -> Unit) {
        userService.getUserByLoginId(loginId).enqueue(object : Callback<UserRequest> {
            override fun onResponse(call: Call<UserRequest>, response: Response<UserRequest>) {
                if (response.isSuccessful) {
                    Log.d("UserProcessor", "조회 성공: ${response.body()}")
                    callback(response.body())
                } else {
                    Log.e("UserProcessor", "조회 실패 - 서버 오류: ${response.code()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<UserRequest>, t: Throwable) {
                Log.e("UserProcessor", "조회 실패 - 통신 오류: ${t.message}")
                callback(null)
            }
        })
    }

    //사용자 정보 수정
    fun updateUser(user: UserRequest, callback: (UserRequest?) -> Unit) {
        userService.updateUser(user.loginId, user).enqueue(object : Callback<UserRequest> {
            override fun onResponse(call: Call<UserRequest>, response: Response<UserRequest>) {
                if (response.isSuccessful) {
                    Log.d("UserProcessor", "수정 성공: ${response.body()}")
                    callback(response.body())
                } else {
                    Log.e("UserProcessor", "수정 실패 - 서버 오류: ${response.code()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<UserRequest>, t: Throwable) {
                Log.e("UserProcessor", "수정 실패 - 통신 오류: ${t.message}")
                callback(null)
            }
        })
    }
}