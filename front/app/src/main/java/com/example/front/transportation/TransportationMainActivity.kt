package com.example.front.transportation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.front.R
import com.example.front.data.searchPath.PathRouteResult
import com.example.front.databinding.ActivityTransportationMainBinding
import com.example.front.processor.RouteProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransportationMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportationMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportationMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val startLat = 37.5665
        val startLng = 126.9780
        val endLat = 37.5651
        val endLng = 126.9895

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = RouteProcessor.fetchAndProcessRoutes(startLat, startLng, endLat, endLng)

                result.forEachIndexed { index, pathRouteResult ->
                    val button = Button(this@TransportationMainActivity).apply {
                        text = "경로 ${index + 1}\n총 소요 시간 ${pathRouteResult.totalTime}분\n환승 횟수 ${pathRouteResult.transitCount}\n환승 정보\n${pathRouteResult.mainTransitTypes}"
                        setTextColor(resources.getColor(android.R.color.white))
                        textSize = 16f
                        setPadding(32, 24, 32, 24)
                        setBackgroundColor(resources.getColor(android.R.color.black))
                        setOnClickListener {
                            RouteProcessor.fetchRealtimeStationData(pathRouteResult, index)
                        }
                    }
                    binding.routeContainer.addView(button)
                }

            } catch (e: Exception) {
                Log.e("TransportationMainActivity", "Error fetching routes", e)
            }
        }
    }
}