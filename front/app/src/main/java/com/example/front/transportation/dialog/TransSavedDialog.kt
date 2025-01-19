package com.example.front.transportation.dialog

import androidx.appcompat.app.AppCompatActivity
import android.app.Dialog
import android.view.Window
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.widget.Button
import android.widget.TextView
import com.example.front.R
import com.example.front.databinding.TransSavedDialogBinding

//커스텀 다이얼로그
class TransSavedDialog(){

   /* private lateinit var binding : TransSavedDialogBinding
    private val dlg = Dialog(context) //부모 액티비티의 context가 들어간다

    fun show(content : String) {
        binding = TransSavedDialogBinding.inflate(context.layoutInflater)

        val doYouWantSaveTextView : TextView = binding.doYouWantSaveTextView
        val YesSavedBtt : TextView = binding.YesSavedBtt
        val NoSavedBtt : TextView = binding.NoSavedBtt

        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE) //타이틀바 제거
        dlg.setContentView(binding.root)   //다이얼로그에 사용할 xml 파일을 불러옴
        dlg.setCancelable(false)   //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함

        // binding.content.text = content //부모 액티비티에서 받은 텍스트 세팅?
        //내 코드와는 연관점이 없음

        //Yes 버튼 동작
        YesSavedBtt.setOnClickListener{
            //Yes 동작시 출력될 어쩌고
            dlg.dismiss()
        }

        //No 버튼 동작
        NoSavedBtt.setOnClickListener{
            dlg.dismiss()
        }

        dlg.show()
    }*/

   /* fun  TransSavedDialogYESClickListener(listener: (String) -> Unit) {

    }

    //TransSavedDialogYESClickListener 인터페이스
    interface TransSavedDialogYESClickListener {
        fun YesSavedClicked(content : String)
    }*/
}