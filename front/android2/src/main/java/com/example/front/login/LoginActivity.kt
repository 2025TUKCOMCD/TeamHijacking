package com.example.front.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.front.MainActivity
import com.example.front.databinding.ActivityLoginBinding
import com.example.front.login.data.UserRequest
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
                    // kakaoTalk 설치 여부 확인 후 로그인 실행
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                                Log.d("login", "kakaoTalk 설치 되어 있음")
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                                Log.d("login", "callback 실행됨 - loginWithKaKaoTalk")

                    //의도적 로그인 취소 체크
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    handleLoginResult(token, error) //콜백을 함수로 분리
                }
            } else {
                                Log.d("login", "kakaoTalk 설치 되어 있지 않음, 계정 로그인 시도")
                UserApiClient.instance.loginWithKakaoAccount(
                    context = this,
                    prompts = listOf(com.kakao.sdk.auth.model.Prompt.LOGIN)
                ) { token, error ->
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
            Log.d("login", user?.kakaoAccount?.email.toString())
            if (error != null) {
                Toast.makeText(this, "사용자 정보 요청 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("login","사용자 정보 요청 실패: ${error.message}")
                return@me
            }else if (user == null) {
                Log.e("login", "사용자 정보가 null")
            } else if(user.kakaoAccount?.email == null) {
                // 이메일 동의를 강제로 다시 요청
                UserApiClient.instance.loginWithKakaoAccount(
                    context = this,
                    prompts = listOf(com.kakao.sdk.auth.model.Prompt.LOGIN)
                ) { token, error ->
                    if (error != null) {
                        Toast.makeText(this, "이메일 동의 요청 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@loginWithKakaoAccount
                    }

                    // 재로그인 성공 시 다시 사용자 정보 요청
                    fetchKakaoUserInfo()
                }
            } else {
                Toast.makeText(this, "사용자 정보 요청 성공: ${user.kakaoAccount?.profile?.nickname}", Toast.LENGTH_SHORT).show()
                Log.i("login", "사용자 정보 요청 성공: " +
                        "\n 회원 정보: ${user.id}" +
                        "\n 이메일: ${user.kakaoAccount?.email}" +
                        "\n 닉네임: ${user.kakaoAccount?.profile?.nickname}")

                //사용자 정보 객체 생성
                val user = UserRequest(
                    name = "${user.kakaoAccount?.profile?.nickname}",
                    loginId = "${user.id}",
                    email = "${user.kakaoAccount?.email}"
                )

                Log.d("login", "객체 생성 제대로 됨")

                //response 확인, 객체 response 받기 전 오류면
                //registerUser에서 오류가 난 것 같다
                UserProcessor.registerUser(user) { response ->
                    Log.d("login", response.code().toString())
                    when (response.code()) {
                        201 -> {
                            val registeredUser = response.body()
                            if (registeredUser != null ) {
                                //로그인 정보 저장 + 메인 화면 이동
                                saveLoginInfo(registeredUser)
                                moveToMain(registeredUser.name)
                            }
                        }
                        409 -> {
                            Toast.makeText(this, "이미 등록된 사용자 입니다.", Toast.LENGTH_SHORT).show()
                            //이미 등록된 사용자 처리 로직
                            moveToMain(user.name)
                        }
                        else -> {
                            Toast.makeText(this, "등록 실패 (오류 코드: ${response.code()})", Toast.LENGTH_SHORT).show()
                        }
                    }

                    Log.d("login", "등록 처리도 잘 됨 안 되었을수도")
                }
                moveToMain("일단아무거나")

            }
        }
        //사용자 정보를 활용해 추가 로직 구현 가능
    }

    // sharedPreferences 에 로그인 정보 저장
    private fun saveLoginInfo(user: UserRequest) {
        val sharedPref = getSharedPreferences("userPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("loginId", user.loginId)
            putString("name", user.name)
//            putString("email", user.email)
            apply()
        }
    }

    //MainActivity 로 이동 위한 코드
    private fun moveToMain(userName: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("userName", userName)
        }
        startActivity(intent)
        finish()  // 로그인 Activity 종료
    }

    private fun updateProfile() {

    }
}