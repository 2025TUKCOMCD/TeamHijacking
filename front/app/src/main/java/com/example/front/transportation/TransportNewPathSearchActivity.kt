package com.example.front.transportation

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityTransportNewPathSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*사용할 객체 바인딩*/
            //val someRootText: TextView = binding.someRootText
            //val someRootDescription: TextView = binding.someRootTextDescription
        val transitCountView: TextView = binding.transitCountView
        val totalTimeView: TextView = binding.totalTimeView
        val detatiledPathView: TextView = binding.detatiledPathView
        val routeStationAndBusesView: TextView = binding.routeStationsAndBusesView
        val mainTransitTypesView: TextView = binding.mainTransitTypesView

        //val startLat = 37.340174
        //val startLng = 126.7335933
        //val endLat = 37.5414001
        //val endLng = 127.0900351
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
// 결과 분리 및 로그 출력 + 버튼 생성
                result.forEach { route ->
                    // 각각의 값 분리
                    val routeStationsAndBuses = route.routeStationsAndBuses
                    val totalTime = route.totalTime
                    val transitCount = route.transitCount
                    val mainTransitTypes = route.mainTransitTypes
                    val detailedPath = route.detailedPath

                    // 로그 출력
                    Log.d("RouteProcessor", "routeStationsAndBuses = $routeStationsAndBuses")
                    Log.d("RouteProcessor", "Total Time: $totalTime 분")
                    Log.d("RouteProcessor", "Transit Count: $transitCount 회")
                    Log.d("RouteProcessor", "Main Transit Types: $mainTransitTypes")
                    Log.d("RouteProcessor", "Detailed Path: $detailedPath")

                    //임시로 한 Layout text에 들어가도록 설정
                    transitCountView.text = getString(R.string.transitCount, transitCount)
                    totalTimeView.text = Integer.toString(totalTime)+"분"
                    detatiledPathView.text = detailedPath
                    routeStationAndBusesView.text="$routeStationsAndBuses"
                    mainTransitTypesView.text= mainTransitTypes


                    // 동적 버튼 생성
//
//                        setOnClickListener {
//                            CoroutineScope(Dispatchers.Main).launch {
//                                val realtimeResult =
//                                    RouteProcessor.fetchRealtimeStation(routeStationsAndBuses)
//                                Log.d("RouteProcessor", "실시간 경로 데이터: $realtimeResult")
//                            }
//                        }
//                    }

                    // 생성된 버튼을 컨테이너에 추가

                }
            } catch (e: Exception) {
                Log.e("RouteProcessor", "경로 탐색 중 오류 발생", e)
            }
        }


    }
}