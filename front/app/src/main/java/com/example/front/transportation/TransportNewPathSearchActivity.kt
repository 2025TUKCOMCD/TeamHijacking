package com.example.front.transportation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater // LayoutInflater를 사용하기 위해 import 추가
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView // TextView를 사용하기 위해 import 추가
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.front.R
import com.example.front.databinding.ActivityTransportNewPathSearchBinding
import com.example.front.transportation.data.searchPath.Route


import androidx.activity.viewModels


class TransportNewPathSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransportNewPathSearchBinding
    private val routeViewModel: RouteViewModel by viewModels()

    // Intent로 받은 데이터를 클래스 멤버 변수로 선언하여 다른 메서드에서도 접근할 수 있도록 합니다.
    private var receivedTransportRouteKey: Int? = null
    private var receivedIsFavorite: Boolean = false
    private var receivedIsSelected: Boolean = false
    private var receivedSavedRouteName: String? = null // 저장된 경로 이름도 필요할 수 있으니 추가

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransportNewPathSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // onCreate 스코프에 있는 지역 변수 (기존 지리적 데이터)
        val startLat = intent.getDoubleExtra("startLat", 37.340174)
        val startLng = intent.getDoubleExtra("startLng", 126.7335933)
        val endLat = intent.getDoubleExtra("endLat", 37.340174)
        val endLng = intent.getDoubleExtra("endLng", 127.0900351)
        val departureName = intent.getStringExtra("departureName") ?: "출발지"
        val destinationName = intent.getStringExtra("destinationName") ?: "도착지"

        // --- Intent에서 플래그 데이터 받기 ---
        val bundle = intent.extras
        bundle?.let {
            receivedTransportRouteKey = it.getInt("transportRouteKey", -1).takeIf { it != -1 } // -1이면 null 처리
            receivedIsFavorite = it.getBoolean("isFavorite", false)
            receivedIsSelected = it.getBoolean("isSelected", false)
            receivedSavedRouteName = it.getString("savedRouteName")

            Log.d("TransportNewPathSearchActivity", "받은 데이터: " +
                    "transportRouteKey: $receivedTransportRouteKey, " +
                    "isFavorite: $receivedIsFavorite, " +
                    "isSelected: $receivedIsSelected, " +
                    "savedRouteName: $receivedSavedRouteName"
            )

            // !!! 받은 플래그 데이터를 활용하는 로직 예시 !!!
            if (receivedIsSelected) {
                // 이 경로는 '저장된 경로' 화면에서 선택되어 넘어온 경로입니다.
                receivedSavedRouteName?.let { name ->
                    Log.d("TransportNewPathSearchActivity", "저장된 경로 '$name'가 선택되어 넘어왔습니다.")
                }
            }

            if (receivedIsFavorite) {
                Log.d("TransportNewPathSearchActivity", "이 경로는 즐겨찾기 상태입니다.")
            }
        }
        // --- 플래그 데이터 받기 끝 ---

        Log.d("TransportNewPathSearchActivity", "startLat: $startLat, startLng: $startLng, endLat: $endLat, endLng: $endLng")

        // binding을 사용하여 뷰 참조
        val loadingSpinner = binding.loadingSpinner
        val dataLayout = binding.newPathLinearLayout // 동적 추가될 컨테이너

        // 데이터 로딩 시작
        loadingSpinner.visibility = View.VISIBLE
        dataLayout.visibility = View.GONE

        // ViewModel 의 LiveData 관찰
        routeViewModel.routeData.observe(this, Observer { routes ->
            // 데이터 로드 완료 후 처리
            loadingSpinner.visibility = View.GONE
            dataLayout.visibility = View.VISIBLE

            // 데이터 UI에 설정
            routes?.let {
                if (it.isNotEmpty()) {
                    // updateRouteViews 호출 시 start/end Lat/Lng, 출발/도착지 이름,
                    // 그리고 받은 플래그 데이터들을 함께 넘겨줍니다.
                    updateRouteViews(
                        it,
                        startLat, startLng, endLat, endLng,
                        departureName, destinationName,
                        receivedTransportRouteKey, // 전달받은 transportRouteKey
                        receivedIsFavorite,       // 전달받은 isFavorite
                        receivedIsSelected        // 전달받은 isSelected
                    )
                } else {
                    Log.d("TransportNewPathSearchActivity", "검색된 경로가 없습니다.")
                    // TODO: "경로를 찾을 수 없습니다" 메시지 표시 등
                }
            } ?: run {
                Log.e("TransportNewPathSearchActivity", "경로 데이터 로드 실패: routes is null")
                // TODO: 사용자에게 오류 메시지 표시
            }
        })

        // 경로 데이터 가져오기
        routeViewModel.fetchRoute(startLat, startLng, endLat , endLng )
    }

    // updateRouteViews 메서드 시그니처를 변경하여 모든 필요한 플래그를 받도록 합니다.
    private fun updateRouteViews(
        routes: List<Route>,
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        departureName : String,
        destinationName : String,
        // 새로 추가된 파라미터들
        passedTransportRouteKey: Int?, // 이전 화면에서 전달받은 transportRouteKey
        passedIsFavorite: Boolean,     // 이전 화면에서 전달받은 isFavorite
        passedIsSelected: Boolean      // 이전 화면에서 전달받은 isSelected
    ) {

        // 기존에 추가된 모든 동적 뷰를 제거하여 중복 생성을 방지합니다.
        binding.newPathLinearLayout.removeAllViews()

        routes.forEachIndexed { index, route ->
            val routeIndices = mutableListOf<Int>()

            route.pathTransitType.forEachIndexed { pathIndex, pathType ->
                if (pathType != 3) {
                    routeIndices.add(pathIndex)
                }
            }


            val routeItemView = LayoutInflater.from(this).inflate(R.layout.trans_new_path_search_view, binding.newPathLinearLayout, false)

            val mainTransitTypesView: TextView = routeItemView.findViewById(R.id.mainTransitTypesView)
            val transitCountView: TextView = routeItemView.findViewById(R.id.transitCountView)
            val totalTimeView: TextView = routeItemView.findViewById(R.id.totalTimeView)
            val detailedPathView: TextView = routeItemView.findViewById(R.id.detatiledPathView) // 오타 주의: detatiledPathView
            val predictTimeView: TextView = routeItemView.findViewById(R.id.predictTimeView)

            mainTransitTypesView.text = route.mainTransitType
            mainTransitTypesView.contentDescription = "주요 교통수단은 ${route.mainTransitType}입니다."

            transitCountView.text = getString(R.string.transitCount, route.transitCount)
            transitCountView.contentDescription = "환승 횟수는 ${route.transitCount}회입니다."

            totalTimeView.text = "${route.totalTime} 분"
            totalTimeView.contentDescription = "총 소요 시간은 ${route.totalTime}분입니다."

            detailedPathView.text = route.transitTypeNo.joinToString(", ")
            // detailedPathView의 contentDescription을 더 명확하게 구성
            val detailedPathDescription = if (route.transitTypeNo.isNotEmpty()) {
                "상세 경로는 ${route.transitTypeNo.joinToString(" 이용, ")} 이용입니다."
            } else {
                "상세 경로 정보가 없습니다."
            }
            detailedPathView.contentDescription = detailedPathDescription


            // 예측 시간 데이터를 빌드할 StringBuilder
            val predictTimesTextBuilder = StringBuilder()
            // predictTimeView의 contentDescription에 사용될 StringBuilder
            val predictTimesContentDescriptionBuilder = StringBuilder()

            routeIndices.forEachIndexed { i, routeIndex ->
                val transit = route.transitTypeNo[routeIndex]
                var predictTime = routes[index].routeIds.getOrNull(i)?.predictTimes1

                // 예측 시간이 "데이터 없음"인 경우 "서비스 지원 안함"으로 변경
                if (predictTime.isNullOrBlank() || predictTime.equals("데이터 없음", ignoreCase = true)) {
                    predictTime = "서비스 지원 안함"
                }

                // UI에 표시될 텍스트 추가 (줄 바꿈 포함)
                predictTimesTextBuilder.append("${transit} : ${predictTime} \n")
                // 접근성을 위한 contentDescription 텍스트 추가 (더 자연스러운 문장으로)
                predictTimesContentDescriptionBuilder.append("대중교통 ${transit}의 예상 시간은 ${predictTime}입니다. ")
            }

            // predictTimeView의 텍스트와 contentDescription 설정
            predictTimeView.text = predictTimesTextBuilder.toString().trimEnd('\n') // 마지막 줄 바꿈 제거
            predictTimeView.contentDescription = predictTimesContentDescriptionBuilder.toString().trim() // 마지막 공백 제거

            // 각 동적 생성된 경로 항목에 클릭 리스너 설정
            routeItemView.setOnClickListener {
                val intent = Intent(this, TransportInformationActivity::class.java)
                intent.putIntegerArrayListExtra("pathTransitType", ArrayList(route.pathTransitType))
                intent.putStringArrayListExtra("transitTypeNo", ArrayList(route.transitTypeNo))
                intent.putExtra("routeIds", ArrayList(route.routeIds))

                intent.putExtra("startLat", startLat)
                intent.putExtra("startLng", startLng)
                intent.putExtra("endLat", endLat)
                intent.putExtra("endLng", endLng)
                intent.putExtra("departureName", departureName)
                intent.putExtra("destinationName", destinationName)

                passedTransportRouteKey?.let { key -> intent.putExtra("transportRouteKey", key) }
                intent.putExtra("isFavorite", passedIsFavorite)
                intent.putExtra("isSelected", passedIsSelected)

                startActivity(intent)
            }

            // 모든 설정이 완료된 routeItemView를 컨테이너에 추가합니다.
            binding.newPathLinearLayout.addView(routeItemView)
            Log.d("TransportNewPathSearchActivity", "경로 항목 추가됨: ${route.transitTypeNo.joinToString()}")
        }
    }
}