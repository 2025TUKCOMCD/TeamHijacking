package com.example.front.iot.smartthingsOauth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.front.iot.smartthingsOauth.YourBackendApiService
import com.example.front.iot.smartthingsOauth.BackendAuthResponse // 수정: BackendAuthResponse 대신 더 일반적인 BackendResponse 사용
import com.example.front.presentation.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// TODO: 이 상수는 BuildConfig 또는 AppConstants 파일에서 가져오는 것이 좋습니다.
// SmartThings CLI에 등록한 Redirect URI와 정확히 일치해야 합니다.
private const val REDIRECT_URI = "https://seemore.io.kr/callback"

// TODO: Spring 백엔드의 기본 URL (BuildConfig.BASE_URL 등에서 가져오세요)
// 이 URL은 SmartThings로부터 받은 'code'를 보낼 당신의 백엔드 API 엔드포인트입니다.
private const val YOUR_SPRING_BACKEND_BASE_URL = "https://seemore.io.kr/" // 예시, 실제 백엔드 주소로 변경 (HTTPS 권장)

class AuthCallbackActivity : AppCompatActivity() {

    private lateinit var backendApiService: YourBackendApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 이 액티비티는 UI를 가질 필요가 없습니다. 인증 처리 후 바로 다른 화면으로 전환됩니다.

        // Retrofit 서비스 초기화 ( onCreate에서 초기화해도 무방하나, 실제 앱에서는 DI를 사용하세요)
        val retrofit = Retrofit.Builder()
            .baseUrl(YOUR_SPRING_BACKEND_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        backendApiService = retrofit.create(YourBackendApiService::class.java)

        handleIncomingUri(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingUri(intent)
    }

    private fun handleIncomingUri(intent: Intent?) {
        val uri: Uri? = intent?.data

        // 1. 리다이렉트 URI 유효성 검사
        if (uri == null || !uri.toString().startsWith(REDIRECT_URI)) {
            Log.e("AuthCallbackActivity", "Invalid URI or does not match REDIRECT_URI: $uri")
            showToastAndFinish("잘못된 인증 요청입니다.")
            return
        }

        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        val error = uri.getQueryParameter("error") // SmartThings에서 에러 발생 시

        if (error != null) {
            // 2. SmartThings 인증 중 에러가 발생한 경우
            Log.e("AuthCallbackActivity", "SmartThings Auth Error: $error")
            showToastAndFinish("SmartThings 인증 중 오류가 발생했습니다: $error")
            return
        }

        if (code != null) {
            Log.d("AuthCallbackActivity", "Received code: $code")
            Log.d("AuthCallbackActivity", "Received state: $state")

            // TODO: (선택 사항이지만 강력 권장) 'state' 값 검증
            // 인증 요청 시 보냈던 'state' 값과 지금 받은 'state' 값이 일치하는지 확인합니다.
            // 이는 CSRF(Cross-Site Request Forgery) 공격을 방지합니다.
            // 실제 앱에서는 'state'를 앱의 안전한 저장소(예: SharedPreferences)에 저장하고 비교합니다.
            val storedState = getStoredState() // TODO: 실제 앱에서 저장된 state를 가져오는 함수 구현
            if (storedState == null || storedState != state) {
                Log.e("AuthCallbackActivity", "State mismatch! Possible CSRF attack. Expected: $storedState, Received: $state")
                showToastAndFinish("보안 오류: 인증 상태가 일치하지 않습니다.")
                return
            }
            clearStoredState() // TODO: 사용 후 저장된 state 제거

            // 3. 'code'와 '사용자 ID'를 Spring 백엔드로 전송
            // !!! 중요: userId는 앱에서 현재 로그인한 사용자 정보로부터 가져와야 합니다. !!!
            // 예시: val currentUserId = YourAppSessionManager.getUserId() // 앱의 사용자 세션 관리 클래스 사용
            // 지금은 임시로 "testuser123"과 같은 값을 사용하거나, 로그인 기능을 먼저 구현해야 합니다.
            val currentUserId = "testuser_unique_id_from_your_app" // <-- TODO: 실제 사용자 ID를 여기에 넣어야 합니다!

            if (currentUserId.isEmpty()) {
                showToastAndFinish("사용자 ID를 찾을 수 없습니다. 로그인 상태를 확인해주세요.")
                return
            }

            sendCodeToBackend(code, state, currentUserId)

        } else {
            Log.e("AuthCallbackActivity", "No authorization code found in URI: $uri")
            showToastAndFinish("인증 코드를 받을 수 없습니다. 다시 시도해주세요.")
        }
    }

    private fun sendCodeToBackend(code: String, state: String?, userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 요청 바디 구성 (code, state, userId)
                val requestBody = mapOf(
                    "code" to code,
                    "state" to (state ?: ""), // state는 필수가 아닐 수 있으므로 빈 문자열 처리
                    "userId" to userId // 백엔드에서 어떤 사용자의 토큰인지 식별하는 데 사용됩니다.
                )
                val response = backendApiService.exchangeSmartThingsCode(requestBody)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val backendResponse = response.body()
                        Log.d("AuthCallbackActivity", "Code sent to backend successfully. Response: $backendResponse")
                        // 백엔드로부터 성공 메시지 또는 발행된 토큰을 받아 처리 (지금은 성공 메시지)
                        showToastAndFinish("SmartThings 연동 성공: ${backendResponse?.message ?: "데이터 처리 완료"}")
                        // TODO: 메인 화면 또는 데이터 표시 화면으로 이동
                        val intent = Intent(this@AuthCallbackActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("AuthCallbackActivity", "Failed to send code to backend. Error: ${response.code()} - $errorBody")
                        showToastAndFinish("SmartThings 연동 실패: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthCallbackActivity", "Error sending code to backend", e)
                withContext(Dispatchers.Main) {
                    showToastAndFinish("네트워크 오류 또는 백엔드 통신 실패: ${e.localizedMessage}")
                }
            } finally {
                // 작업 완료 후 액티비티 종료
                finish()
            }
        }
    }

    private fun showToastAndFinish(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // TODO: CSRF 방지용 state 저장/가져오기/삭제 함수 구현 (예시)
    private fun getStoredState(): String? {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        return prefs.getString("oauth_state", null)
    }

    private fun clearStoredState() {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        prefs.edit().remove("oauth_state").apply()
    }
    // MainActivity (인증 요청을 시작하는 곳)에서 SmartThings OAuth URL을 열기 전에 state를 저장해야 합니다.
    // 예시: val generatedState = UUID.randomUUID().toString()
    //       saveState(generatedState)
    //       val authUrl = "https://api.smartthings.com/oauth/authorize?response_type=code&client_id=YOUR_CLIENT_ID&scope=r:devices:*&redirect_uri=YOUR_REDIRECT_URI&state=$generatedState"
    //       startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)))
    // fun saveState(state: String) {
    //     val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
    //     prefs.edit().putString("oauth_state", state).apply()
    // }
}