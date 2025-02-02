package com.example.front.transportation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.front.R
import com.example.front.databinding.ActivityTransportNewPathSearchBinding
import com.example.front.transportation.processor.RouteProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransportNewPathSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransportNewPathSearchBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransportNewPathSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val startLat = intent.getDoubleExtra("startLat", 37.340174)
        val startLng = intent.getDoubleExtra("startLng", 126.7335933)
        val endLat = intent.getDoubleExtra("endLat", 37.340174)
        val endLng = intent.getDoubleExtra("endLng",127.0900351)

        Log.d("현빈", startLat.toString())
        Log.d("현빈", startLng.toString())
        Log.d("현빈", endLat.toString())
        Log.d("현빈", endLng.toString())

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = RouteProcessor.fetchAndProcessRoutes(startLat, startLng, endLat, endLng)

                val views = listOf(
                    listOf(binding.transitCountView1, binding.totalTimeView1, binding.detailedPathView1, binding.mainTransitTypesView1),
                    listOf(binding.transitCountView2, binding.totalTimeView2, binding.detailedPathView2, binding.mainTransitTypesView2),
                    listOf(binding.transitCountView3, binding.totalTimeView3, binding.detailedPathView3, binding.mainTransitTypesView3)
                )

                result.forEachIndexed { index, route ->
                    if (index < views.size) {
                        val (transitCountView, totalTimeView, detailedPathView, mainTransitTypesView) = views[index]

                        transitCountView.text = getString(R.string.transitCount, route.transitCount)
                        totalTimeView.text = "${route.totalTime} 분"
                        detailedPathView.text = route.detailedPath
                        mainTransitTypesView.text = route.mainTransitTypes

                        listOf(
                            transitCountView to route.transitCount,
                            totalTimeView to route.totalTime,
                            detailedPathView to route.detailedPath,
                            mainTransitTypesView to route.mainTransitTypes
                        ).forEach { (view, data) ->
                            addClickListener(view, data, route.busDetails)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RouteProcessor", "경로 탐색 중 오류 발생", e)
            }
        }
    }

    private fun addClickListener(view: TextView, data: Any, busDetails: List<String>) {
    view.setOnClickListener {
        Log.d("ViewClick", "Clicked: $data")
        val busDetailsString = busDetails.joinToString(",")
        val intent = Intent(this, TransNewPathDetailActivity::class.java).apply {
            putExtra("routeStationsAndBuses", busDetailsString)
        }
        startActivity(intent)
    }
}
}