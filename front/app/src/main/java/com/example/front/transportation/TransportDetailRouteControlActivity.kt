package com.example.front.transportation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.front.R
import com.example.front.transportation.DetailRouteFragment.BusFragment
import com.example.front.transportation.DetailRouteFragment.SubwayFragment
import com.example.front.transportation.DetailRouteFragment.WalkFragment

data class TransportData(val type: String, val data: Bundle?)


class TransportDetailRouteControlActivity : AppCompatActivity() {
    private val transportList = mutableListOf<TransportData>()
    private var currentIndex = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transport_detail_route_control)
        // Intent에서 데이터 추출 및 리스트에 추가
        val walkData1 = Bundle().apply { putString("walkDistance", "500m"); putString("walkTime", "5분") }
        val busData = Bundle().apply { putString("busNumber", "123"); putString("busStation", "강남역") }
        val subwayData = Bundle().apply { putString("subwayLine", "2호선"); putString("subwayStation", "역삼역") }
        val walkData2 = Bundle().apply { putString("walkDistance", "1km"); putString("walkTime", "15분") }

        transportList.add(TransportData("walk", walkData1))
        transportList.add(TransportData("bus", busData))
        transportList.add(TransportData("subway", subwayData))
        transportList.add(TransportData("walk", walkData2))

        showNextFragment()
    }
    private fun showNextFragment() {
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
            // 모든 Fragment 표시 완료
            Toast.makeText(this, "모든 정보를 표시했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}