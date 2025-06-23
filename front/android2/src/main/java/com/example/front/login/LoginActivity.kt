package com.example.front.login

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.example.front.BuildConfig
import com.example.front.MainActivity
import com.example.front.databinding.ActivityLoginBinding
import com.example.front.login.data.UserRequest
import com.example.front.login.processor.UserProcessor
import com.example.front.login.processor.UserProcessor.getSmartThingsToken
import com.example.front.login.service.UserService
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.common.util.Utility
import java.security.SecureRandom
import java.util.Base64


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    // SmartThings OAuth 관련 상수 (BuildConfig에서 가져옴)
    private val SMARTTHINGS_AUTHORIZE_URL = BuildConfig.SMARTTHINGS_AUTHORIZE_URL
    private val SMARTTHINGS_CLIENT_ID = BuildConfig.SMARTTHINGS_CLIENT_ID
    private val SMARTTHINGS_REDIRECT_URI = BuildConfig.SMARTTHINGS_REDIRECT_URI
    private val SMARTTHINGS_SCOPE = "r:devices:* w:devices:* x:devices:*" // SmartThings API 접근 권한
    private var SMARTTHINGS_TOKEN : Unit? = null // SmartThings 토큰을 저장할 변수;

    // CSRF 토큰 생성을 위한 SecureRandom 인스턴스
    private val secureRandom = SecureRandom()



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
                                // 여기에 코드 추가
                                initiateSmartThingsOAuth(registeredUser.loginId)
                                getSmartThingsToken(registeredUser.loginId){
                                    token ->
                                    if (token != null) {
                                        saveSmartThingsToken(token)
                                        Log.d("SmartThings", "토큰 저장 완료: $token")
                                    } else {
                                        Log.e("SmartThings", "토큰 저장 실패")
                                    }
                                }
                                //moveToMain(registeredUser.name)
                            }
                        }
                        409 -> {
                            Toast.makeText(this, "이미 등록된 사용자 입니다.", Toast.LENGTH_SHORT).show()
                            //이미 등록된 사용자 처리 로직
                            saveLoginInfo(user)
                            // 여기에 코드 추가
                            getSmartThingsToken(user.loginId) {
                                    token ->
                                if (token != null) {

                                    saveSmartThingsToken(token)
                                    Log.d("SmartThings", "토큰 저장 완료: $token")
                                } else {
                                    Log.e("SmartThings", "토큰 저장 실패")
                                }
                            }
                            // initiateSmartThingsOAuth(user.loginId)
                            //moveToMain(user.name)
                        }
                        else -> {
                            Toast.makeText(this, "등록 실패 (오류 코드: ${response.code()})", Toast.LENGTH_SHORT).show()
                        }
                    }

                    Log.d("login", "등록 처리도 잘 됨 안 되었을수도")
                }
            }
        }
        //사용자 정보를 활용해 추가 로직 구현 가능
    }
/*TODO::mhhghghghgh*/
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

    private fun saveSmartThingsToken(token: String) {
        val sharedPref = getSharedPreferences("smartThingsPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("smartThingsToken", token)
            apply()
        }
        Log.d("SmartThings", "토큰 저장 완료: $token")
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
    /**
     * SmartThings OAuth 인증 흐름을 시작합니다.
     * 사용자를 SmartThings 인증 페이지로 리디렉션합니다.
     * @param userId SmartThings 연동에 사용할 사용자 ID (백엔드에서 토큰과 매핑하기 위해 state에 포함)
     */
    private fun initiateSmartThingsOAuth(userId: String) {
        // CSRF 보호를 위한 state 값 생성 (userId와 랜덤 문자열 조합)
//        val csrfBytes = ByteArray(16)
//        secureRandom.nextBytes(csrfBytes)
//        val csrfToken = Base64.getUrlEncoder().withoutPadding().encodeToString(csrfBytes)
        val state = "${userId}" // 백엔드가 userId를 추출할 수 있도록 형식 지정

        // SmartThings 인증 URL 구성
        val authUrl = Uri.parse(SMARTTHINGS_AUTHORIZE_URL).buildUpon()
            .appendQueryParameter("response_type", "code") // OAuth 2.0 Authorization Code Flow
            .appendQueryParameter("client_id", SMARTTHINGS_CLIENT_ID)
            .appendQueryParameter("scope", SMARTTHINGS_SCOPE)
            .appendQueryParameter("redirect_uri", SMARTTHINGS_REDIRECT_URI)
            .appendQueryParameter("state", state) // CSRF 토큰과 사용자 ID 포함
            .build()
            .toString()

        Log.d("SmartThingsOAuth", "SmartThings OAuth URL: $authUrl")

        // Chrome Custom Tabs를 사용하여 웹 페이지 열기 시도
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true) // 툴바에 웹 페이지 제목 표시
                .setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM) // 시스템 테마에 맞춤
                .setToolbarColor(ContextCompat.getColor(this, com.example.front.R.color.iotColor)) // 툴바 색상 설정 (예시 색상, 프로젝트에 맞게 변경)
                .build()

            customTabsIntent.launchUrl(this, Uri.parse(authUrl))
            Log.d("SmartThingsOAuth", "SmartThings 인증 페이지 Custom Tabs로 열기 성공")
            Toast.makeText(this, "SmartThings 인증을 진행해주세요.", Toast.LENGTH_LONG).show()
        } catch (e: ActivityNotFoundException) {
            // Custom Tabs (Chrome)이 설치되어 있지 않을 경우 일반 웹 브라우저로 폴백
            Log.e("SmartThingsOAuth", "Custom Tabs를 사용할 수 없습니다. 일반 웹 브라우저로 시도: ${e.message}", e)
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
                if (fallbackIntent.resolveActivity(packageManager) != null) {
                    startActivity(fallbackIntent)
                    Log.d("SmartThingsOAuth", "SmartThings 인증 페이지 일반 브라우저로 열기 성공")
                    Toast.makeText(this, "SmartThings 인증을 진행해주세요.", Toast.LENGTH_LONG).show()
                } else {
                    Log.e("SmartThingsOAuth", "SmartThings 인증 URL을 처리할 앱(웹 브라우저 등)을 찾을 수 없습니다: $authUrl")
                    Toast.makeText(this, "웹 브라우저를 찾을 수 없습니다. SmartThings 인증을 진행할 수 없습니다.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("SmartThingsOAuth", "SmartThings 인증 페이지 열기 중 폴백 오류: ${e.message}", e)
                Toast.makeText(this, "SmartThings 인증을 시작할 수 없습니다. ${e.message}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("SmartThingsOAuth", "SmartThings 인증 페이지 열기 중 예상치 못한 오류: ${e.message}", e)
            Toast.makeText(this, "SmartThings 인증을 시작할 수 없습니다. ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}