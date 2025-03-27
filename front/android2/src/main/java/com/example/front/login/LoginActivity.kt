package com.example.front.login


import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityLoginBinding
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


    /* 카카오톡 앱을 통해 로그인할 수 있는지 확인하고, 카카오톡이 설치되지 않은 경우에는 카카오 계정으로 로그인한다. */
    private fun kakaoLogin() {

        try {
            // 카카오톡 설치 여부 확인 후 로그인 실행, 안 되어있을 시 카카오 계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                Log.d("login", "카카오톡이 설치되어 있음")
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    Log.d("login", "callback 실행됨 - loginWithKaKaoTalk")

                    /*if(error != null) {
                        Log.e("login", "카카오톡 로그인 실패", error)
                    }*/
                    //사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우
                    //의도적인 로그인 취소로 보고 카카오 계정으로 로그인 시도 없이 로그인 취소로 처리(예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    handleLoginResult(token, error) //콜백을 함수로 분리
                }
            } else {
                Log.d("login", "카카오톡이 설치되어 있지 않음, 계정 로그인 시도")
                UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                    Log.d("login", "callback 실행됨 - loginWithKakaoAccount")
                    handleLoginResult(token, error)
                }
            }
        } catch (e: Exception) {
            Log.e("login", "로그인 실행 중 예외 발생: ${e.message}")
        }
    }


    //이전 코드에서 callBack 함수가 제대로 작동되지 않음을 확인, 이를 개선하기 위해 callback을 분리함. callback->handleLoginResult()
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


    /* UserAPiClient.instance.me() 메소드를 호출하면 현재 로그인한 사용자의 정보 받아올 수 있음.
       사용자 정보를 활용한 추가 로직 구현 가능 */
    private fun fetchKakaoUserInfo() {
        Log.d("login", "fetchKakaoUserInfo() 실행됨")

        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Toast.makeText(this, "사용자 정보 요청 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("login","사용자 정보 요청 실패: ${error.message}")
            } else if (user == null) {
                Log.e("login", "사용자 정보가 null")
            } else {
                Toast.makeText(this, "사용자 정보 요청 성공: ${user.kakaoAccount?.profile?.nickname}", Toast.LENGTH_SHORT).show()
                Log.i("login", "사용자 정보 요청 성공: " +
                      "\n 회원정보: ${user.id}" +
                      "\n 이메일: ${user.kakaoAccount?.email}" +
                      "\n 닉네임: ${user.kakaoAccount?.profile?.nickname}")
                //사용자 정보를 활용하여 추가 로직 구현 가능
                //TODO:: 추가 로직 구현

            }
        }
    }


    //로그아웃을 위한 function. 앱에 저장된 로그인 정보 지우고 돌려보냄. 참고:: https://quessr.tistory.com/84
    private fun kakaoLogout() {
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Toast.makeText(this, "로그아웃 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("login","로그아웃 실패, SDK에서 토큰 삭제됨", error)
            } else {
                Toast.makeText(this, "로그아웃 성공", Toast.LENGTH_SHORT).show()
                Log.i("logtin", "로그아웃 성공, SDK에서 토큰 삭제됨")
            }
        }
    }


    //TODO:: 로그인 여부를 자동 확인하는 func 구현 필요
    private fun isUserLogin(){

    }

    private fun updateProfile() {
        // 사용자 정보 저장,
        /*사용자 프로퍼티인 properties 필드 하위 정보의 값을 저장한다.
        * 키 값은 내 애플리케이션>카카오 로그인>사용자 프로퍼티에 정의한 값?을 사용해야 한다.
        * https://developers.kakao.com/docs/latest/ko/kakaologin/prerequisite#user-properties*/

//        val properties = mapOf("${CUSTOM_PROPERTY_KEY}" to "${CUSTOM_PROPERTY_VALUE}")
//
//        UserApiClient.instance.updateProfile(properties) { error ->
//            if (error != null) {
//                Log.e("login", "사용자 정보 저장 실패", error)
//            }
//            else {
//                Log.i("login", "사용자 정보 저장 성공")
//            }
    }


//    private fun selectShippingAddress() {
//        UserApiClient.instance.selectShippingAddress(context) { addressId, error ->
//            if (error != null) {
//                Log.i("login", "배송지 선택 실패 $error")
//                return@selectShippingAddress
//            }
//
//            UserApiClient.instance.shippingAddresses(addressId!!) { userShippingAddresses, err ->
//                if (err != null) {
//                    Log.i("login", "배송지 조회 실패 $err")
//                } else if (userShippingAddresses != null) {
//                    Log.i(
//                        "login", "배송지 조회 성공" +
//                                "\n회원번호: ${userShippingAddresses.userId}" +
//                                "\n배송지: \n${
//                                    userShippingAddresses.shippingAddresses?.joinToString(
//                                        "\n"
//                                    )
//                                }"
//                    )
//                }
//            }
//        }
//    }

}