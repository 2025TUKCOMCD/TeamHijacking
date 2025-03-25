package com.example.front.transportation

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.front.R
import com.example.front.databinding.ActivityTransportDetailRouteControlBinding
import com.example.front.transportation.DetailRouteFragment.BusFragment
import com.example.front.transportation.DetailRouteFragment.SubwayFragment
import com.example.front.transportation.DetailRouteFragment.WalkFragment

interface FragmentNavigation {
    fun showNextFragment()
}

class TransportDetailRouteControlActivity : AppCompatActivity(), FragmentNavigation { // FragmentNavigation 인터페이스 구현

    private lateinit var binding: ActivityTransportDetailRouteControlBinding

    data class TransportData(val type: String, val data: Bundle?)
    private val transportList = mutableListOf<TransportData>()
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTransportDetailRouteControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ... (데이터 설정 및 Fragment 표시 코드)
        val walkData1 = Bundle().apply { putString("walkDistance", "500m"); putString("walkTime", "5분") }
        val busData = Bundle().apply { putString("busNumber", "123"); putString("busStation", "강남역") }
        val subwayData = Bundle().apply { putString("subwayLine", "2호선"); putString("subwayStation", "역삼역") }
        val walkData2 = Bundle().apply { putString("walkDistance", "1km"); putString("walkTime", "15분") }

        transportList.add(TransportData("walk", walkData1))
        transportList.add(TransportData("bus", busData))
        transportList.add(TransportData("subway", subwayData))
        transportList.add(TransportData("walk", walkData2))
        Log.d("현빈", transportList.toString())

        showNextFragment()
    }

    override fun showNextFragment() { // FragmentNavigation 인터페이스 메서드 구현 (override 추가)
        if (currentIndex < transportList.size) {
            val transportData = transportList[currentIndex]
            val fragment = when (transportData.type) {
                "walk" -> WalkFragment()
                "bus" -> BusFragment()
                "subway" -> SubwayFragment()
                else -> return
            }

            fragment.arguments = transportData.data
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()

            currentIndex++
        } else {
            Toast.makeText(this, "모든 정보를 표시했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}