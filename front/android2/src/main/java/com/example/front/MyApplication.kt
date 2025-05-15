package com.example.front

import android.app.Application
//import android.content.Context
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
//import com.umc.anddeul.R

/*https://uroa.tistory.com/43
   전역 변수? 를 공유 하기 위한 Application Class.
   자세한 것은 카카 오톡 로그인 개발 이슈 참조
 */

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        val keyHash = Utility.getKeyHash(this)
        Log.d("키 확인", keyHash)
        //카카오 SDK 초기화, 네이티브 앱 키를 사용해 SDK를 초기화
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_API_KEY)
    }
}