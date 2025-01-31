package com.example.front.transportation

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.front.R
import com.example.front.databinding.ActivityTransportNewPathSearchBinding
import com.example.front.transportation.processor.RouteProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*"경로 클릭시" 출력될 화면, 새 경로를 찾는 화면과 구 경로를
* 재사용할 때 동일하게 사용된다. 피그마의 경로 클릭시 경우 참고.*/
class TransportNewPathSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransportNewPathSearchBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransportNewPathSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*사용할 객체 바인딩*/
        val transitCountView: TextView = binding.transitCountView
        val totalTimeView: TextView = binding.totalTimeView
        val detatiledPathView: TextView = binding.detatiledPathView
        val routeStationAndBusesView: TextView = binding.routeStationsAndBusesView
        val mainTransitTypesView: TextView = binding.mainTransitTypesView
        //위도 경도 가져오기
        //val startLat = 37.340174 val startLng = 126.7335933 val endLat = 37.5414001 val endLng = 127.0900351
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

                result.forEach { route ->
                    val totalTime = route.totalTime
                    val transitCount = route.transitCount
                    val mainTransitTypes = route.mainTransitTypes
                    val detailedPath = route.detailedPath
                    val busDetail = route.busDetails

                    Log.d("RouteProcessor", "Total Time: $totalTime 분")
                    Log.d("RouteProcessor", "Transit Count: $transitCount 회")
                    Log.d("RouteProcessor", "Main Transit Types: $mainTransitTypes")
                    Log.d("RouteProcessor", "Detailed Path: $detailedPath")
                    Log.d("RouteProcessor", "busDetail: $busDetail")

                    // 메인 스레드에서 업데이트가 발생하도록 보장
                    runOnUiThread {
                        transitCountView.text = getString(R.string.transitCount, transitCount)
                        totalTimeView.text = "$totalTime 분"
                        detatiledPathView.text = detailedPath
                        routeStationAndBusesView.text = "$busDetail"
                        mainTransitTypesView.text = mainTransitTypes

                        val newPathLinearLayout = findViewById<LinearLayout>(R.id.newPathLinearLayout)
                        val dynamicBtn = Button(this@TransportNewPathSearchActivity)
                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        dynamicBtn.layoutParams = layoutParams
                        newPathLinearLayout.addView(dynamicBtn)
                    }
                }
            } catch (e: Exception) {
                Log.e("RouteProcessor", "경로 탐색 중 오류 발생", e)
            }
        }
    }
}