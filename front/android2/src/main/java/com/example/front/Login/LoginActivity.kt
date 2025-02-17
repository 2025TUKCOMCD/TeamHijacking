package com.example.front.Login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityLoginBinding
import com.example.front.MainActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import java.io.File
import java.io.FileInputStream
import java.util.Properties

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //로그인 버튼 바인딩
        val loginButton: ImageButton = binding.loginButton

        loginButton.setOnClickListener {

            //TODO:: 로그인이 끝난 후 실행될 수 있도록 조정할 것
            kakaoLogin()
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }
    }


    /* 카카오톡 앱을 통해 로그인할 수 있는지 확인하고, 카카오톡이 설치되지 않은 경우에는 카카오 계정으로 로그인한다. */
    private fun kakaoLogin(){

        //로그인 성공 여부를 확인한 후, 로그인에 성공하면 fetchKaKaoUserInfo를 호출해 사용자 정보를 요청한다.
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Toast.makeText(this, "로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                fetchKakaoUserInfo()
            }
        }

        // 카카오톡 설치 여부 확인 후 로그인 실행
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }

    }

    /* UserAPiClient.instance.me() 메소드를 호출하면 현재 로그인한 사용자의 정보 받아올 수 있음.
    * 사용자 정보를 활용한 추가 로직 구현 가능 */
    private fun fetchKakaoUserInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Toast.makeText(this, "사용자 정보 요청 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            } else if (user != null) {
                Toast.makeText(this, "사용자 정보 요청 성공: ${user.kakaoAccount?.profile?.nickname}", Toast.LENGTH_SHORT).show()
                // 사용자 정보를 활용하여 추가 로직 구현 가능
            }
        }
    }

    //로그아웃을 위한 function. 앱에 저장된 로그인 정보 지우고 돌려보냄. 참고:: https://quessr.tistory.com/84
    private fun kakaoLogout() {
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Toast.makeText(this, "로그아웃 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "로그아웃 성공", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //TODO:: 로그인 여부를 자동 확인하는 func 구현 필요
    fun isUserLogin(){

    }

}