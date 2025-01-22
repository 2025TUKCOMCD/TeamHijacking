package com.example.front.transportation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityTransportInfrmationBinding
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.front.R
import com.example.front.databinding.TransSavedConfirmDialogBinding
import com.example.front.databinding.TransSavedDialogBinding

//교통 안내용 Activity
class TransportInfrmationActivity : AppCompatActivity() {

    private lateinit var binding : ActivityTransportInfrmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportInfrmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imsiBtt2 : Button = binding.imsiBtt2

       //다이얼로그를 띄우기 위한 임시 버튼, 추후 삭제 예정.
        imsiBtt2.setOnClickListener{
            transSavedDialogShow()
        }
    }

    private fun transSavedDialogShow(){
        // 뷰 바인딩 확인
        val transSavedDialogBinding = try {
            TransSavedDialogBinding.inflate(layoutInflater)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (transSavedDialogBinding == null) {
            println("TransSavedDialogBinding.inflate 실패")
            return
        }

        //Alertdialog.Builder를 통한 다이얼로그 생성
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(transSavedDialogBinding.root)
            .create()

        //dialog 동작 후 크기 조정
        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 화면 너비의 90%
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        //버튼 관련 변수 설정
        val yesSavedBtt: TextView = transSavedDialogBinding.YesSavedBtt
        val noSavedBtt: TextView = transSavedDialogBinding.NoSavedBtt

        //버튼 관련 설정 진행
        yesSavedBtt.setOnClickListener{
            //Yes 클릭 시 동작
            transSavedNicknameDialogShow()
            dialog.dismiss()
            //원하는 추가 작업
        }

        noSavedBtt.setOnClickListener{
            dialog.dismiss()
        }
    }

    //다이얼로그의 yes 버튼 클릭 시 동작되어야 할 코드.

    private fun transSavedNicknameDialogShow(){

        val transSavedConfirmDialogBinding = try {
            TransSavedConfirmDialogBinding.inflate(layoutInflater)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if(transSavedConfirmDialogBinding == null) {
            println("TransSavedConfigDialogBinding.inflate fail")
            return
        }

        //Alertdialog.Builder를 통한 다이얼로그 생성
        val dialog2 = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(transSavedConfirmDialogBinding.root)
            .create()

        //다이얼로그 동작 후 크기 조정
        dialog2.show()
        dialog2.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 화면 너비의 90%
            LinearLayout.LayoutParams.WRAP_CONTENT)

        //버튼 관련 변수 설정
        val transSavedConfirmBtt: TextView = transSavedConfirmDialogBinding.transSavedConfirmBtt

        transSavedConfirmBtt.setOnClickListener{
            dialog2.dismiss()
        }
    }
}