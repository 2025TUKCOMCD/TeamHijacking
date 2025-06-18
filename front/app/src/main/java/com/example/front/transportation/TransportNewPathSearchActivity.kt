package com.example.front.transportation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log

import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
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
                // 예를 들어, UI에 '저장된 경로'임을 알리는 표시를 하거나,
                // 저장된 경로 이름을 상단 바에 설정할 수 있습니다.
                receivedSavedRouteName?.let { name ->
                    // binding.toolbarTitle.text = name // 툴바 제목 등 UI에 표시
                    Log.d("TransportNewPathSearchActivity", "저장된 경로 '$name'가 선택되어 넘어왔습니다.")
                }
            }

            if (receivedIsFavorite) {
                // 이 경로는 이전에 즐겨찾기로 설정되었던 경로입니다.
                // 예를 들어, 화면 상단의 즐겨찾기 버튼 아이콘을 노란색으로 초기 설정할 수 있습니다.
                // binding.favoriteButton.setColorFilter(getColor(R.color.yellow))
                Log.d("TransportNewPathSearchActivity", "이 경로는 즐겨찾기 상태입니다.")
            }

            // receivedTransportRouteKey를 사용하여, 나중에 이 경로를 다시 저장/업데이트할 때
            // 이 키 값을 서버에 함께 전송하여 기존 경로를 수정할 수 있도록 할 수 있습니다.
        }
        // --- 플래그 데이터 받기 끝 ---

        Log.d("TransportNewPathSearchActivity", "startLat: $startLat, startLng: $startLng, endLat: $endLng, endLng: $endLng")


        val loadingSpinner = findViewById<ProgressBar>(R.id.loadingSpinner)
        val dataLayout = findViewById<LinearLayout>(R.id.newPathLinearLayout)

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
                    // 데이터 없는 경우 처리
                    // binding.noRoutesFoundText.visibility = View.VISIBLE
                }
            }
        })

        // 경로 데이터 가져오기
        routeViewModel.fetchRoute(startLat, startLng, endLat , endLng )

        // TODO: "저장" 버튼 클릭 리스너 예시
        // 만약 이 화면에서 경로를 저장하는 버튼이 있다면,
        // 해당 버튼 클릭 시 receivedTransportRouteKey와 현재 즐겨찾기 상태 등을 사용하여
        // 서버에 저장 또는 업데이트 요청을 보낼 수 있습니다.
        /*
        binding.saveRouteButton.setOnClickListener {
            // 사용자가 선택한 최종 경로 정보를 가져오거나, 새로운 경로라면 이를 구성합니다.
            val selectedRoute = routes.firstOrNull() // 예시: 첫 번째 경로를 저장한다고 가정

            if (selectedRoute != null) {
                // 저장 요청을 보낼 때 receivedTransportRouteKey와 receivedIsFavorite 등을 함께 보냅니다.
                // 이 키가 null이 아니면 기존 경로 업데이트, null이면 새 경로 저장
                val routeToSave = SaveRouteRequestDTO(
                    transportRouteKey = receivedTransportRouteKey, // 기존 키가 있으면 업데이트, 없으면 서버에서 생성
                    departureName = departureName,
                    destinationName = destinationName,
                    startLat = startLat,
                    startLng = startLng,
                    endLat = endLat,
                    endLng = endLng,
                    isFavorite = receivedIsFavorite, // 현재 화면의 즐겨찾기 상태
                    savedRouteName = receivedSavedRouteName ?: "새로운 저장 경로" // 사용자 입력 또는 기본값
                    // loginId 등 다른 필요한 필드 추가
                )
                // RouteProcessor.saveRoute(routeToSave) // 가정: 저장 API 호출
                Toast.makeText(this, "경로 저장/업데이트 요청", Toast.LENGTH_SHORT).show()
            }
        }
        */
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
        val route1Views = listOf(binding.transitCountView1, binding.totalTimeView1, binding.detailedPathView1, binding.mainTransitTypesView1)
        val route2Views = listOf(binding.transitCountView2, binding.totalTimeView2, binding.detailedPathView2, binding.mainTransitTypesView2)
        val route3Views = listOf(binding.transitCountView3, binding.totalTimeView3, binding.detailedPathView3, binding.mainTransitTypesView3)

        val routeLayouts = listOf(binding.someRootLayout1, binding.someRootLayout2, binding.someRootLayout3)
        val routeViews = listOf(route1Views, route2Views, route3Views)

        routes.forEachIndexed { index, route ->
            if (index < routeViews.size) {
                val (transitCountView, totalTimeView, detailedPathView, mainTransitTypesView) = routeViews[index]
                detailedPathView.text = route.transitTypeNo.joinToString(", ")
                transitCountView.text = getString(R.string.transitCount, route.transitCount)
                totalTimeView.text = "${route.totalTime} 분"
                mainTransitTypesView.text = route.mainTransitType

                // Set click listener for each route layout
                routeLayouts[index].setOnClickListener {
                    // Create the Intent and add data
                    val intent = Intent(this, TransportInformationActivity::class.java)
                    intent.putIntegerArrayListExtra("pathTransitType", ArrayList(route.pathTransitType)) // 경로 유형
                    intent.putStringArrayListExtra("transitTypeNo", ArrayList(route.transitTypeNo)) // 경로 한글 버전
                    intent.putExtra("routeIds", ArrayList(route.routeIds))

                    // 파라미터로 받은 startLat, startLng, endLat, endLng 사용
                    intent.putExtra("startLat", startLat)
                    Log.d("startLat", startLat.toString())
                    intent.putExtra("startLng", startLng)
                    Log.d("startLng", startLng.toString())
                    intent.putExtra("endLat", endLat)
                    Log.d("endLat", endLat.toString())
                    intent.putExtra("endLng", endLng)
                    Log.d("endLng", endLng.toString())
                    intent.putExtra("departureName", departureName) // 출발지 주소
                    intent.putExtra("destinationName", destinationName) // 도착지 주소


                    passedTransportRouteKey?.let { key -> intent.putExtra("transportRouteKey", key) }
                    intent.putExtra("isFavorite", passedIsFavorite)
                    intent.putExtra("isSelected", passedIsSelected)

                    startActivity(intent)
                }
            }
        }
    }
}