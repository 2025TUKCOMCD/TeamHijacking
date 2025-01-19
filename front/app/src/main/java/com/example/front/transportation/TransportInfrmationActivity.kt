package com.example.front.transportation

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityTransportInfrmationBinding
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.AlertDialog
import com.example.front.R
import com.example.front.databinding.TransSavedDialogBinding
import com.example.front.transportation.dialog.TransSavedDialog

//교통 안내용 Activity
class TransportInfrmationActivity : AppCompatActivity() {

    private lateinit var binding : ActivityTransportInfrmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportInfrmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imsiBtt2 : Button = binding.imsiBtt2

       // imsiBtt2.setOnClickListener(this)
        imsiBtt2.setOnClickListener{
            transSavedDialogShow2()
        }
    }

    private fun transSavedDialogShow(){
        val transSavedBuilder = AlertDialog.Builder(this)
            .setTitle("Title")
            .setMessage("Message")
            .setPositiveButton("Ok") {dialog, which->
                //handle OK button Click
            }
            .setNegativeButton("Cancle") {dialog, which->
                //handle cancle butotn click
            }
        val dialog = transSavedBuilder.create()
        dialog.show()
        //https://medium.com/@manuchekhrdev/android-show-simple-alert-dialog-in-kotlin-f7e232ec949e
        //커스텀 dialog가 아닐 시
    }

    private fun transSavedDialogShow2(){
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
        val dialog = AlertDialog.Builder(this)
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
            dialog.dismiss()
            //원하는 추가 작업
        }
    }
}