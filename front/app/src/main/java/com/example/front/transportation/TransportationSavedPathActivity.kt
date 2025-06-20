package com.example.front.transportation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.front.R
import com.example.front.databinding.ActivityTransportationSavedPathBinding
import com.example.front.transportation.processor.RouteProcessor
import android.util.Log
import com.example.front.transportation.data.DB.GetRouteResponseDTO
import kotlinx.coroutines.launch
import android.graphics.PorterDuff // PorterDuff.Mode.SRC_IN 사용을 위해 필요

class TransportationSavedPathActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportationSavedPathBinding
    private val savedPathRootLayout: LinearLayout by lazy { findViewById(R.id.transSavedPathLayout) }

    // 즐겨찾기 상태를 관리하는 맵 (경로 주요 키 -> 즐겨찾기 여부). 데이터베이스에서 불러온 초기 상태와 별 버튼 클릭으로 변경된 현재 메모리 상태를 관리합니다.
    private val favoriteStatusMap: MutableMap<Int, Boolean> = mutableMapOf()

    // 경로 선택 상태를 관리하는 맵 (경로 주요 키 -> 선택 여부). 이 플래그는 사용자가 특정 경로 항목을 클릭했는지 나타냅니다.
    private val selectedPathMap: MutableMap<Int, Boolean> = mutableMapOf()

    // 현재 로드된 경로 데이터를 보관하는 리스트. 이 리스트를 바탕으로 UI를 갱신하고 정렬합니다.
    private var _currentLoadedRoutes: MutableList<GetRouteResponseDTO> = mutableListOf()

    // TransportNewPathSearchActivity를 시작하고 결과를 받기 위한 런처
    private lateinit var startTransportNewPathSearchActivity: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransportationSavedPathBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ActivityResultLauncher 초기화
        startTransportNewPathSearchActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // TransportNewPathSearchActivity에서 OK 결과 수신 시, DB에서 최신 데이터 다시 불러와 UI 재정렬
                Log.d("TransportationSavedPath", "TransportNewPathSearchActivity에서 OK 결과 수신. DB 동기화 시작.")
                loadSavedPathsFromDatabase()
            } else {
                Log.d("TransportationSavedPath", "TransportNewPathSearchActivity에서 OK 아닌 결과 수신 또는 취소됨.")
            }
        }

        loadSavedPathsFromDatabase()

        binding.someRootThing.setOnClickListener {
            val intent = Intent(this, TransportNewPathSearchActivity::class.java)
            // 새로운 경로 검색 활동을 시작할 때, 결과를 다시 받기 위해 런처를 사용합니다.
            startTransportNewPathSearchActivity.launch(intent)
        }
    }

    /**
     * 서버(데이터베이스)에서 저장된 경로 목록을 가져와 UI에 동적으로 추가하는 함수.
     * 이 함수는 데이터베이스의 최신 상태를 로드하고, 메모리 맵과 리스트를 업데이트한 후 UI를 재구성합니다.
     */
    private fun loadSavedPathsFromDatabase() {
        val currentUserLoginId = "3970421203" // TODO: 실제 사용자 ID로 교체 필요

        lifecycleScope.launch {
            Log.d("TransportationSavedPath", "저장된 경로 로드 시작 (loginId: $currentUserLoginId)")
            val savedRoutes: List<GetRouteResponseDTO>? =
                RouteProcessor.DBGetRoute(currentUserLoginId)

            if (savedRoutes != null) {
                if (savedRoutes.isNotEmpty()) {
                    Log.d("TransportationSavedPath", "총 ${savedRoutes.size}개의 경로 로드됨.")

                    // 기존 메모리 상태 초기화
                    _currentLoadedRoutes.clear()
                    favoriteStatusMap.clear()
                    selectedPathMap.clear()

                    // 불러온 경로 데이터를 메모리 리스트에 추가하고 즐겨찾기 맵 초기화
                    savedRoutes.forEach { item ->
                        _currentLoadedRoutes.add(item)
                        // DB에서 불러온 초기 즐겨찾기 상태로 맵을 초기화합니다.
                        favoriteStatusMap[item.transportrouteKey] = item.isFavorite ?: false
                        selectedPathMap[item.transportrouteKey] = false // 초기에는 선택되지 않음
                    }

                    // 메모리 리스트를 바탕으로 UI 업데이트 및 정렬
                    refreshDisplayedRoutes()

                    Toast.makeText(this@TransportationSavedPathActivity, "저장된 경로를 불러왔습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("TransportationSavedPath", "저장된 경로가 없습니다.")
                    // 경로가 없을 때 UI를 비워줍니다.
                    savedPathRootLayout.removeAllViews()
                    Toast.makeText(this@TransportationSavedPathActivity, "저장된 경로가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("TransportationSavedPath", "저장된 경로를 가져오는데 실패했습니다.")
                Toast.makeText(this@TransportationSavedPathActivity, "경로를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 현재 메모리에 로드된 (_currentLoadedRoutes) 경로 데이터를 즐겨찾기 상태와 사용 횟수에 따라 정렬하고 UI에 표시합니다.
     * 이 함수는 DB를 다시 조회하지 않고 메모리 상태를 기반으로 UI를 갱신합니다.
     */
    private fun refreshDisplayedRoutes() {
        // 경로 정렬: 1. 즐겨찾기 여부 (true가 먼저), 2. userRouteCount 내림차순 (높은 카운트가 먼저)
        val sortedRoutes = _currentLoadedRoutes.sortedWith(
            // favoriteStatusMap의 현재 상태를 사용하여 즐겨찾기 여부를 판단하고 내림차순 정렬합니다.
            compareByDescending<GetRouteResponseDTO> { favoriteStatusMap[it.transportrouteKey] ?: false }
                // 즐겨찾기 상태가 같으면 userRouteCount를 사용하여 내림차순 정렬합니다. (높은 카운트가 먼저)
                .thenByDescending { it.userRouteCount ?: 0 }
        )

        // 정렬된 경로를 확인하기 위해 로그 출력
        Log.d("TransportationSavedPath", "--- 정렬된 경로 (refreshDisplayedRoutes) ---")
        sortedRoutes.forEachIndexed { index, item ->
            // 현재 favoriteStatusMap의 상태와 DTO의 userRouteCount를 로그에 출력합니다.
            Log.d("TransportationSavedPath", "인덱스 ${index}: 닉네임='${item.savedRouteName}', 즐겨찾기=${favoriteStatusMap[item.transportrouteKey]}, 사용횟수=${item.userRouteCount}")
        }
        Log.d("TransportationSavedPath", "---------------------")

        savedPathRootLayout.removeAllViews() // 기존 동적 뷰 제거: 이 시점에서 UI가 비워집니다.

        // 정렬된 순서대로 UI 요소를 다시 추가합니다.
        sortedRoutes.forEach { item ->
            val routePrimaryKey = item.transportrouteKey

            val nickname = item.savedRouteName ?: "Unnamed Route"
            val departure = item.departureName ?: "Unknown Departure"
            val destination = item.destinationName ?: "Unknown Destination"
            // openView 호출 시, favoriteStatusMap의 현재 즐겨찾기 상태를 전달합니다.
            val currentFavoriteStatus = favoriteStatusMap[routePrimaryKey] ?: false

            openView(
                id = routePrimaryKey,
                addressNicknameText = nickname,
                departureText = departure,
                destinationText = destination,
                favouritePathStarValue = currentFavoriteStatus, // 메모리 맵의 현재 즐겨찾기 상태 전달
                originalDepartureName = item.departureName,
                originalDestinationName = item.destinationName,
                originalStartLat = item.startLat,
                originalStartLng = item.startLng,
                originalEndLat = item.endLat,
                originalEndLng = item.endLng
            )
        }
        // 모든 뷰가 추가된 후 레이아웃 갱신을 요청합니다.
        savedPathRootLayout.requestLayout()
        Log.d("TransportationSavedPath", "UI 갱신 완료: 뷰가 새롭게 추가되었습니다.")
    }

    private fun openView(
        id: Int, // Int형 transportRouteKey
        addressNicknameText: String,
        departureText: String,
        destinationText: String,
        favouritePathStarValue: Boolean, // 이 값은 favoriteStatusMap의 현재 상태를 반영함
        originalDepartureName: String?,
        originalDestinationName: String?,
        originalStartLat: Double?,
        originalStartLng: Double?,
        originalEndLat: Double?,
        originalEndLng: Double?
    ) {
        // openView가 호출될 때마다 해당 항목의 상태를 로그로 출력하여 UI 구성 순서를 확인합니다.
        Log.d("TransportationSavedPath", "openView called for: $addressNicknameText (ID: $id), Favorite: $favouritePathStarValue, Departure: $departureText, Destination: $destinationText")

        val inflater = layoutInflater
        val newLayout: LinearLayout = inflater.inflate(R.layout.trans_saved_path_button, savedPathRootLayout, false) as LinearLayout

        val addressNicknameTextView: TextView = newLayout.findViewById(R.id.addressNicknameTextview)
        val departureTextView: TextView = newLayout.findViewById(R.id.departureTextView)
        val destinationTextView: TextView = newLayout.findViewById(R.id.destinationTextView)
        val favouritePathStarBtt: ImageView = newLayout.findViewById(R.id.favouritePathStarBtt)

        addressNicknameTextView.text = addressNicknameText
        addressNicknameTextView.contentDescription = "경로 이름: $addressNicknameText"
        departureTextView.text = "출발지: $departureText"
        departureTextView.contentDescription = "출발지: $departureText"
        destinationTextView.text = "목적지: $destinationText"
        destinationTextView.contentDescription = "목적지: $destinationText"

        // UI에 즐겨찾기 상태 반영 (favoriteStatusMap에서 가져온 값 사용)
        updateStarIcon(favouritePathStarBtt, favouritePathStarValue)
        favouritePathStarBtt.contentDescription = if (favouritePathStarValue) "즐겨찾기에 추가됨, 별 버튼" else "즐겨찾기에 추가되지 않음, 별 버튼"

        // 선택 상태 초기화 (처음에는 선택되지 않은 상태)
        selectedPathMap[id] = false

        favouritePathStarBtt.setOnClickListener {
            // 별 버튼 클릭 시, 메모리 내 즐겨찾기 상태만 토글하고 UI 업데이트 후 리스트 재정렬
            starBtnClickListener(id, favouritePathStarBtt)
        }

        newLayout.setOnClickListener {
            Log.d("TransportationSavedPath", "경로 항목 클릭됨: $addressNicknameText (ID: $id)")

            selectedPathMap[id] = true // 클릭된 항목을 true로 설정

            Log.d("TransportationSavedPath", "경로 선택 상태 업데이트 (ID: $id, 선택됨: ${selectedPathMap[id]})")

            // 해당 경로의 현재 userRouteCount를 가져옴 (여기서 증가시키지 않음)
            val currentUserRouteCount = _currentLoadedRoutes.find { it.transportrouteKey == id }?.userRouteCount ?: 0

            // 인텐트 생성 및 데이터 전달
            val intent = Intent(this@TransportationSavedPathActivity, TransportNewPathSearchActivity::class.java).apply {
                // 메모리상의 최신 데이터를 바탕으로 전달 (isFavorite는 favoriteStatusMap, userRouteCount는 현재 값)
                putExtra("startLat", originalStartLat)
                putExtra("startLng", originalStartLng)
                putExtra("endLat", originalEndLat)
                putExtra("endLng", originalEndLng)
                putExtra("departureName", originalDepartureName)
                putExtra("destinationName", originalDestinationName)
                putExtra("savedRouteName", addressNicknameText)
                putExtra("transportRouteKey", id)
                putExtra("isFavorite", favoriteStatusMap[id] ?: false) // 최신 메모리 상태 반영
                putExtra("isSelected", true) // 클릭되었으므로 true
                putExtra("userRouteCount", currentUserRouteCount) // 증가시키지 않고 현재 userRouteCount 전달
            }
            // TransportNewPathSearchActivity를 런처를 통해 시작
            startTransportNewPathSearchActivity.launch(intent)
            // 이 시점에서는 DB 업데이트를 하지 않음. DB 업데이트는 TransportNewPathSearchActivity에서 돌아왔을 때 수행될 것임.
        }

        newLayout.contentDescription = "$addressNicknameText 경로 항목. 출발지는 $departureText, 목적지는 $destinationText." +
                if (favouritePathStarValue) " 즐겨찾기에 추가됨." else ""

        savedPathRootLayout.addView(newLayout)
    }

    /**
     * 즐겨찾기 별 아이콘의 색상을 업데이트하는 헬퍼 함수.
     */
    private fun updateStarIcon(starImageView: ImageView, isFavorite: Boolean) {
        if (isFavorite) {
            starImageView.setColorFilter(getColor(R.color.yellow), PorterDuff.Mode.SRC_IN)
        } else {
            starImageView.setColorFilter(getColor(R.color.gray), PorterDuff.Mode.SRC_IN)
        }
    }

    /**
     * 즐겨찾기 별 버튼 클릭 시 호출되는 함수.
     * 해당 경로의 즐겨찾기 상태를 메모리에서 토글하고 UI에 반영하며, 리스트를 재정렬합니다.
     * 이 시점에서는 서버에 업데이트하지 않습니다.
     * @param routePrimaryKey 클릭된 경로의 서버 주요 키 (Int형).
     * @param favouritePathStar 클릭된 별 ImageView.
     */
    private fun starBtnClickListener(routePrimaryKey: Int, favouritePathStar: ImageView) {
        val currentStatus = favoriteStatusMap[routePrimaryKey] ?: false
        val newStatus = !currentStatus // 새로운 즐겨찾기 상태

        // 1. 즐겨찾기 상태를 맵에 업데이트 (메모리만)
        favoriteStatusMap[routePrimaryKey] = newStatus

        // 2. _currentLoadedRoutes 내의 해당 GetRouteResponseDTO 객체의 isFavorite 상태도 업데이트
        //    (GetRouteResponseDTO가 data class이고 isFavorite가 val이라면 copy()를 사용합니다.)
        val updatedItemIndex = _currentLoadedRoutes.indexOfFirst { it.transportrouteKey == routePrimaryKey }
        if (updatedItemIndex != -1) {
            val oldItem = _currentLoadedRoutes[updatedItemIndex]
            _currentLoadedRoutes[updatedItemIndex] = oldItem.copy(isFavorite = newStatus)
            Log.d("TransportationSavedPath", " _currentLoadedRoutes 내 DTO의 isFavorite 업데이트: ID $routePrimaryKey -> $newStatus")
        }


        // 3. 별 아이콘 UI 업데이트
        updateStarIcon(favouritePathStar, newStatus)
        // 4. 접근성을 위한 contentDescription 업데이트
        favouritePathStar.contentDescription = if (newStatus) "즐겨찾기에 추가됨, 별 버튼" else "즐겨찾기에 추가되지 않음, 별 버튼"

        Log.d("TransportationSavedPath", "경로 주요 키 '$routePrimaryKey'의 즐겨찾기 상태가 $newStatus 로 메모리에서 변경됨 (서버 업데이트 없음).")

        // 5. 메모리상의 변경된 즐겨찾기 상태를 기준으로 리스트를 다시 정렬하고 UI를 갱신
        refreshDisplayedRoutes()
    }
}
