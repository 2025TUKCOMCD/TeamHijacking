package com.example.front.app // 워치 앱의 실제 패키지 이름으로 변경

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity // Gravity import 추가
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.example.front.R // 워치 앱의 R 클래스 import

/**
 * 휴대폰 로그인을 안내하는 커스텀 다이얼로그 클래스입니다.
 * Wear OS 앱에서 데이터가 비어있을 때 사용자에게 메시지를 표시하는 데 사용됩니다.
 */
class PhoneLoginPromptDialog(context: Context) : Dialog(context) {

    private var currentUserId: String? = null // 사용자 ID 상태를 내부적으로 관리
    private lateinit var okButton: Button // 확인 버튼의 참조를 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 기본 다이얼로그의 타이틀 바를 제거합니다.
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // 커스텀 다이얼로그 레이아웃을 설정합니다.
        setContentView(R.layout.login_request_dialog) // 위에서 생성한 레이아웃 파일 참조

        // 다이얼로그 창의 레이아웃 파라미터를 가져와 중앙으로 정렬합니다.
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


        // 다이얼로그가 외부 터치나 뒤로가기 버튼으로 닫히지 않도록 설정 (추가된 부분)
        setCancelable(false) // 뒤로가기 버튼으로 다이얼로그가 닫히는 것을 방지
        setCanceledOnTouchOutside(false) // 다이얼로그 바깥을 터치하여 닫히는 것을 방지

        // 확인 버튼 연결
        okButton = findViewById(R.id.dialogOkButton)

        // 초기 상태: 사용자 ID가 없으므로 확인 버튼 비활성화
        updateOkButtonState()

        // 확인 버튼 클릭 리스너 설정
        okButton.setOnClickListener {
            // 버튼이 활성화된 경우에만 다이얼로그를 닫습니다.
            if (okButton.isEnabled) {
                dismiss() // 다이얼로그 닫기
            } else {
                // 버튼이 비활성화 상태일 때 클릭 시 사용자에게 알림 (선택 사항)
                // Toast.makeText(context, "로그인 정보가 필요합니다. 휴대폰에서 로그인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 외부에서 사용자 ID를 업데이트하고 확인 버튼의 활성화 상태를 갱신하는 메서드입니다.
     * 예: MainActivity에서 데이터 수신 후 이 메서드를 호출하여 다이얼로그 상태를 변경할 수 있습니다.
     * @param userId 업데이트할 사용자 ID. null이면 비활성화, non-null이면 활성화
     */
    fun updateLoginStatus(userId: String?) {
        this.currentUserId = userId
        updateOkButtonState() // 사용자 ID 변경 후 버튼 상태 갱신

        // 사용자 ID가 생겼다면 (로그인 성공 시) 다이얼로그를 자동으로 닫을 수도 있습니다. (선택 사항)
        // if (!currentUserId.isNullOrEmpty()) {
        //     dismiss()
        // }
    }

    /**
     * 확인 버튼의 활성화 상태를 업데이트하는 내부 함수입니다.
     * currentUserId 값에 따라 버튼의 enabled 속성과 텍스트/배경 색상을 변경합니다.
     */
    private fun updateOkButtonState() {
        // currentUserId가 null이 아니거나 비어있지 않으면 버튼 활성화
        val isEnabled = !currentUserId.isNullOrEmpty()
        okButton.isEnabled = isEnabled

        // 버튼 텍스트 색상 변경 (활성화/비활성화 시 시각적 구분)
        okButton.setTextColor(
            context.resources.getColor(
                if (isEnabled) android.R.color.black else android.R.color.darker_gray,
                null
            )
        )
        // 버튼 배경 틴트 색상 변경
        okButton.backgroundTintList = context.resources.getColorStateList(
            if (isEnabled) android.R.color.white else android.R.color.background_dark, // 또는 다른 적절한 비활성화 색상
            null
        )
    }
}
