package com.example.front.transportation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityTransNewPathDetatilBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class TransNewPathDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransNewPathDetatilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransNewPathDetatilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val busString = intent.getStringExtra("bus")
        val predict1String = intent.getStringExtra("predict1")
        val predict2String = intent.getStringExtra("predict2")
        val routeIds = intent.getStringExtra("routeIds")

        val busList = busString?.let {
            if (it.startsWith("[") && it.endsWith("]")) {
                it.substring(1, it.length - 1).split(",").map { item -> item.trim() }
            } else {
                listOf(it.trim())
            }
        } ?: emptyList()

        val predict1List = predict1String?.let {
            if (it.startsWith("[") && it.endsWith("]")) {
                it.substring(1, it.length - 1).split(",").map { item -> item.trim() }
            } else {
                listOf(it.trim())
            }
        } ?: emptyList()

        val predict2List = predict2String?.let {
            if (it.startsWith("[") && it.endsWith("]")) {
                it.substring(1, it.length - 1).split(",").map { item -> item.trim() }
            } else {
                listOf(it.trim())
            }
        } ?: emptyList()

// 세 개의 리스트에서 가장 작은 크기에 맞춰 반복
        val size = minOf(busList.size, predict1List.size, predict2List.size)

        val combinedString = (0 until size).joinToString("\n") { index ->
            "${busList[index]}\n ${predict1List[index]}\n ${predict2List[index]}\n"
        }

        binding.tvRouteInfo.text = combinedString // 텍스트뷰에 추가

        binding.btnSelectRoute.setOnClickListener {
            val intent = Intent(this, TransportInfrmationActivity::class.java).apply {
                putExtra("routeIds", routeIds)
            }
            startActivity(intent)

        }
        // 대괄호 제거 후, trim() 적용
    }
}
