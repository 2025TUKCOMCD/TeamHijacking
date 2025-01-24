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
            val intent = Intent(this, TransportNewPathSearchActivity::class.java)
            startActivity(intent)
        }

    }

    fun openView() {
        /*추후 TransSavedPathActivity가 열릴 시 작동할 function,
        * onCreate시 작동하여, database로부터 경로 목록 받아와 그 갯수만큼 버튼 생성.
        * 버튼 생성 function은 하단의 createPathBtt 이용, 순회하며 받아옴 */
    }

    fun createPathBtt() {
        //하... 버튼을 생성하는 기능,
        /* todo::
        *   1. path 지정 btt UIUX 디자인
        *   2. UIUX 개발
        *   3. 해당 버튼을 이 activity에 불러오도록 구현 */
    }
}