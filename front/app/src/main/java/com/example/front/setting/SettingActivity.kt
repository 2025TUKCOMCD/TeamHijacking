package com.example.front.setting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.front.R
import com.example.front.databinding.ActivitySettingBinding
import com.example.front.databinding.SettingTextviewDialogBinding
import androidx.appcompat.app.AlertDialog
import com.example.front.transportation.TransportDetailRouteControlActivity

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //view 바인딩
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //각각의 객체 바인딩
        val settingProfile: TextView = binding.settingProfile
        val settingNewestVersion: TextView = binding.settingNewestVersion
        val settingFAQ: TextView = binding.settingFAQ
        val askFormBtt: TextView = binding.askFormBtt
        val testBtn: Button = binding.testBtn

        //클릭 시 이동하도록...
        settingProfile.setOnClickListener{
              //TODO:: 스마츠폰 앱 구현 이후 수정
        }
        settingNewestVersion.setOnClickListener{
            editSettingDialog("0.0.0 Beta Version")
        }
        settingFAQ.setOnClickListener {
            editSettingDialog(resources.getString(R.string.FAQText))
            //FAQ 내용 수정은 string.xml에서
        }
        askFormBtt.setOnClickListener {
            editSettingDialog("Contact:: hanisky1@naver.com")
        }

        testBtn.setOnClickListener{
            intent = Intent(this, TransportDetailRouteControlActivity::class.java)
            startActivity(intent)
        }


    }

    fun editSettingDialog(someText:String = "기본 텍스트") {
        /*  다이얼로그 내 내용 정리하는 곳... String을 정의하면 그걸로 바뀜. */

        //뷰 바인딩 확인
        val settingTextviewDialogBinding = try {
            SettingTextviewDialogBinding.inflate(layoutInflater)
        } catch ( e: Exception ) {
            e.printStackTrace()
            null
        }

        if( settingTextviewDialogBinding == null ) {
            Log.e("bug", "settingTextViewDialogBinding.inflate 실패 ")
            return
        }

        //Alertdialog.Builder를 통한 다이얼로그 생성
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(settingTextviewDialogBinding.root)
            .create()

        //다이얼로그 동작 후 크기 조정
        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 화면 너비의 90%
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        //변수 바인딩
        val NoBtn : TextView = settingTextviewDialogBinding.NoBtn
        val settingDialogDefaultText: TextView = settingTextviewDialogBinding.settingDialogDefaultText

        //요소에 대한 함수 설정
        NoBtn.setOnClickListener {
            dialog.dismiss()
        }
        settingDialogDefaultText.text = someText
    }
}