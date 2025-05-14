package com.example.front.login

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityLoginBinding
import com.example.front.login.data.User
import com.example.front.login.processor.RetrofitClient
import com.example.front.login.processor.UserProcessor
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.common.util.Utility

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //로그인 버튼 바인딩
        val loginButton: ImageButton = binding.loginButton

        loginButton.setOnClickListener {
            Log.d("login", "로그인 버튼 눌림")
            kakaoLogin()
        }

        val keyHash = Utility.getKeyHash(this)
        Log.e("해시키", keyHash)
    }


    /* kakaoTalk 앱을 통해 로그인 가능 여부 확인, 설치 되지 않은 경우 계정 으로 로그인 */
    private fun kakaoLogin() {

        try {
            // kakaoTalk 설치 여부 확인 후 로그인 실행, else 카카오 계정 으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                Log.d("login", "kakaoTalk 설치 되어 있음")
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    Log.d("login", "callback 실행됨 - loginWithKaKaoTalk")

                    //kakaoTalk 설치 후 device 권한 요청 화면 에서 로그인 취소한 경우 의도적 로그인 취소로 보고 카카오 계정 로그인 시도 없이 로그인 취소로 처리(예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    handleLoginResult(token, error) //콜백을 함수로 분리
                }
            } else {
                Log.d("login", "kakaoTalk 설치 되어 있지 않음, 계정 로그인 시도")
                UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                    Log.d("login", "callback 실행됨 - login With kakaoAccount")
                    handleLoginResult(token, error)
                }
            }
        } catch (e: Exception) {
            Log.e("login", "로그인 실행 중 예외 발생: ${e.message}")
        }
    }


    //callback 분리함. callback->handleLoginResult()
    private fun handleLoginResult(token:OAuthToken?, error: Throwable?) {
        Log.d("login", "callback 호출됨")  // 추가
        if (error != null) {
            Toast.makeText(this, "로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            Log.e("login", "로그인 실패 ${error.message}")
        } else if (token != null) {
            Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
            Log.d("login", "로그인 성공")
            fetchKakaoUserInfo()
        } else {
            Log.e("login", "알 수 없는 로그인 오류 발생")
        }
    }


    /* UserAPiClient.instance.me() 메소드 호출 시 현재 로그인 사용자 정보 받아올 수 있음.
       사용자 정보를 활용한 추가 로직 구현 가능 */
    private fun fetchKakaoUserInfo() {
        Log.d("login", "fetch kakao UserInfo() 실행됨")

        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Toast.makeText(this, "사용자 정보 요청 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("login","사용자 정보 요청 실패: ${error.message}")
            } else if (user == null) {
                Log.e("login", "사용자 정보가 null")
            } else {
                Toast.makeText(this, "사용자 정보 요청 성공: ${user.kakaoAccount?.profile?.nickname}", Toast.LENGTH_SHORT).show()
                Log.i("login", "사용자 정보 요청 성공: " +
                        "\n 회원 정보: ${user.id}" +
                        "\n 이메일: ${user.kakaoAccount?.email}" +
                        "\n 닉네임: ${user.kakaoAccount?.profile?.nickname}")

                //사용자 정보 객체 생성
                val user = User(
                    name = "${user.kakaoAccount?.profile?.nickname}",
                    loginId = "${user.id}")

                UserProcessor.registerUser(user) {  registeredUser ->    //registeredUser 는 Retrofit 통신의 응답 결과를 받아 저장 하는 콜백 함수의 매개 변수
                    if(registeredUser != null) {
                        Log.d("Login", "등록된 사용자: $registeredUser")
                    } else {
                        Log.e("login", "등록 실패")
                    }
                }
            }
        }
        //사용자 정보를 활용해 추가 로직 구현 가능
    }


    //logout 위한 function. 앱에 저장된 로그인 정보 지움. 참고:: https://quessr.tistory.com/84
    private fun kakaoLogout() {
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Toast.makeText(this, "logout 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("login","logout 실패, SDK 에서 토큰 삭제됨", error)
            } else {
                Toast.makeText(this, "logout 성공", Toast.LENGTH_SHORT).show()
                Log.i("login", "logout 성공, SDK 에서 토큰 삭제됨")
            }
        }
    }


    //TODO:: 로그인 여부를 자동 확인 func 구현 필요
    private fun isUserLogin(){

    }

    private fun updateProfile() {

    }
}