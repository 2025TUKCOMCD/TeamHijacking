package com.example.front.transportation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.front.R
import com.example.front.data.RouteProcessor
import com.example.front.databinding.ActivityTransportationMainBinding
import kotlinx.coroutines.launch

class TransportationMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportationMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportationMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val routeContainer: LinearLayout = binding.routeContainer

        lifecycleScope.launch {
            try {
                val result = RouteProcessor.fetchAndProcessRoutes(
                    startLat = 37.513841, // 잠실역
                    startLng = 127.101823,
                    endLat = 37.476813, // 낙성대역
                    endLng = 126.964156
                )

                result.forEach { routeInfo ->
                    val button = Button(this@TransportationMainActivity).apply {
                        text = routeInfo
                        setTextColor(resources.getColor(android.R.color.white))
                        textSize = 16f
                        setPadding(8, 8, 8, 8)
                    }
                    routeContainer.addView(button)
                }
            } catch (e: Exception) {
                Log.e("TransportationMainActivity", "Error fetching routes", e)
            }
        }
    }
}