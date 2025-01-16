package com.example.front.transportation

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityTransNewPathDetatilBinding


/*경로 상세 정보 페이지, 경로 선택 시 상세 경로를 나타낼
* 주요 기능은 다음에 올 대중교통이 몇 분 뒤에 오는지 등이다.*/
class TransNewPathDetatilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransNewPathDetatilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransNewPathDetatilBinding.inflate(layoutInflater)
        setContentView(binding.main)

        val tvRouteInfo: TextView = binding.tvRouteInfo

    }
}