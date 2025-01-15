package com.example.front.transportation

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityTransportationSavedPathBinding

class TransportationSavedPathActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportationSavedPathBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransportationSavedPathBinding.inflate(layoutInflater)
        setContentView(binding.main)

        //텍스트뷰 바인딩
        val someRootThing: TextView = binding.someRootThing

        //textView에 text 삽입
        someRootThing.text="여기에서 텍스트 수정이 가능함"

        someRootThing.setOnClickListener{
            //일단 한 번 읽어주고
            //세부 정보로 넘어감
            // 새로운 경로 탐색 버튼 클릭 시 실행할 로직
            val intent = Intent(this, TransNewPathDetatilActivity::class.java)
            startActivity(intent)
        }

    }
}