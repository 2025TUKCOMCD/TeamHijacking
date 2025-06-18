package com.example.front.presentation

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.example.front.R // 워치 앱의 R 클래스 import

/**
 * 휴대폰 로그인을 안내하는 커스텀 다이얼로그 클래스입니다.
 * Wear OS 앱에서 데이터가 비어있을 때 사용자에게 메시지를 표시하는 데 사용됩니다.
 */
class PhoneLoginPromptDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 기본 다이얼로그의 타이틀 바를 제거합니다.
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // 커스텀 다이얼로그 레이아웃을 설정합니다.
        setContentView(R.layout.login_request_dialog) // 위에서 생성한 레이아웃 파일 참조
        val window = this.window
        window?.apply {
            setLayout(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER) // 다이얼로그를 화면 중앙에 위치시킵니다.
        }

        // 다이얼로그 메시지 TextView 연결
        val dialogMessage: TextView = findViewById(R.id.dialogMessage)
        dialogMessage.text = "휴대폰으로 로그인해주세요." // 표시할 메시지

        // 확인 버튼 연결 및 클릭 리스너 설정
        val okButton: Button = findViewById(R.id.dialogOkButton)
        okButton.setOnClickListener {
            dismiss() // 다이얼로그 닫기
        }
    }
}