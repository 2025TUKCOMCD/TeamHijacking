package com.example.front.transportation

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.front.R
import com.example.front.databinding.ActivityTransportationSavedPathBinding
import com.example.front.transportation.processor.RouteProcessor
import android.util.Log
import com.example.front.transportation.data.DB.GetRouteResponseDTO
import kotlinx.coroutines.launch
import android.graphics.PorterDuff // PorterDuff.Mode.SRC_IN을 위해 필요

class TransportationSavedPathActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportationSavedPathBinding
    private val savedPathRootLayout: LinearLayout by lazy { findViewById(R.id.transSavedPathLayout) }

    // 즐겨찾기 상태를 관리하는 맵 (경로 주요 키 -> 즐겨찾기 여부)
    private val favoriteStatusMap: MutableMap<Int, Boolean> = mutableMapOf()

    // 새로운 맵: 경로 선택 상태를 관리 (경로 주요 키 -> 선택 여부)
    // 이 플래그는 사용자가 특정 경로 항목을 클릭했는지 여부를 나타냅니다.
    private val selectedPathMap: MutableMap<Int, Boolean> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransportationSavedPathBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSavedPathsFromDatabase()

        binding.someRootThing.setOnClickListener {
            val intent = Intent(this, TransportNewPathSearchActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * 서버(데이터베이스)에서 저장된 경로 목록을 가져와 UI에 동적으로 추가하는 함수.
     */
    private fun loadSavedPathsFromDatabase() {
        val currentUserLoginId = "3970421203" // 실제 사용자 ID로 교체 필요

        lifecycleScope.launch {
            Log.d("TransportationSavedPath", "저장된 경로 로드 시작 (loginId: $currentUserLoginId)")
            val savedRoutes: List<GetRouteResponseDTO>? =
                RouteProcessor.DBGetRoute(currentUserLoginId)

            if (savedRoutes != null) {
                if (savedRoutes.isNotEmpty()) {
                    Log.d("TransportationSavedPath", "총 ${savedRoutes.size}개의 경로 로드됨.")
                    savedPathRootLayout.removeAllViews() // 기존 동적 뷰 제거
                    favoriteStatusMap.clear() // 즐겨찾기 맵 초기화
                    selectedPathMap.clear() // 선택 상태 맵 초기화

                    savedRoutes.forEach { item ->
                        val routePrimaryKey = item.transportrouteKey

                        val nickname = item.savedRouteName ?: "Unnamed Route"
                        val departure = item.departureName ?: "Unknown Departure"
                        val destination = item.destinationName ?: "Unknown Destination"
                        val isFavorite = item.isFavorite ?: false

                        openView(
                            id = routePrimaryKey,
                            addressNicknameText = nickname,
                            departureText = departure,
                            destinationText = destination,
                            favouritePathStarValue = isFavorite,
                            originalDepartureName = item.departureName,
                            originalDestinationName = item.destinationName,
                            originalStartLat = item.startLat,
                            originalStartLng = item.startLng,
                            originalEndLat = item.endLat,
                            originalEndLng = item.endLng
                        )
                    }
                    Toast.makeText(this@TransportationSavedPathActivity, "저장된 경로를 불러왔습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("TransportationSavedPath", "저장된 경로가 없습니다.")
                    Toast.makeText(this@TransportationSavedPathActivity, "저장된 경로가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("TransportationSavedPath", "저장된 경로를 가져오는데 실패했습니다.")
                Toast.makeText(this@TransportationSavedPathActivity, "경로를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openView(
        id: Int, // Int형 transportRouteKey
        addressNicknameText: String,
        departureText: String,
        destinationText: String,
        favouritePathStarValue: Boolean,
        originalDepartureName: String?,
        originalDestinationName: String?,
        originalStartLat: Double?,
        originalStartLng: Double?,
        originalEndLat: Double?,
        originalEndLng: Double?
    ) {
        val inflater = layoutInflater
        val newLayout: LinearLayout = inflater.inflate(R.layout.trans_saved_path_button, savedPathRootLayout, false) as LinearLayout

        val addressNicknameTextView: TextView = newLayout.findViewById(R.id.addressNicknameTextview)
        val departureTextView: TextView = newLayout.findViewById(R.id.departureTextView)
        val destinationTextView: TextView = newLayout.findViewById(R.id.destinationTextView)
        val favouritePathStarBtt: ImageView = newLayout.findViewById(R.id.favouritePathStarBtt)

        addressNicknameTextView.text = addressNicknameText
        departureTextView.text = "출발지: $departureText"
        destinationTextView.text = "목적지: $destinationText"

        // 즐겨찾기 상태 초기화 및 UI 업데이트
        favoriteStatusMap[id] = favouritePathStarValue
        updateStarIcon(favouritePathStarBtt, favouritePathStarValue)

        // 선택 상태 초기화 (처음에는 선택되지 않은 상태)
        selectedPathMap[id] = false

        favouritePathStarBtt.setOnClickListener {
            // 별 버튼 클릭 시, 즐겨찾기 상태만 토글하고 UI 업데이트
            starBtnClickListener(id, favouritePathStarBtt)
        }

        newLayout.setOnClickListener {
            Log.d("TransportationSavedPath", "경로 항목 클릭됨: $addressNicknameText (ID: $id)")

            // 해당 경로 항목이 클릭되었음을 selectedPathMap에 기록합니다.
            // 필요에 따라 이전에 선택된 다른 항목들의 선택 상태를 false로 재설정할 수도 있습니다.
            // (예: 단일 선택만 허용하는 경우)
            // for (key in selectedPathMap.keys) {
            //     selectedPathMap[key] = false
            // }
            selectedPathMap[id] = true // 클릭된 항목을 true로 설정

            Log.d("TransportationSavedPath", "경로 선택 상태 업데이트 (ID: $id, 선택됨: ${selectedPathMap[id]})")

            val intent = Intent(this, TransportNewPathSearchActivity::class.java).apply {
                // 기존에 전달하던 경로 데이터를 그대로 유지합니다.
                originalStartLat?.let { putExtra("startLat", it) }
                originalStartLng?.let { putExtra("startLng", it) }
                originalEndLat?.let { putExtra("endLat", it) }
                originalEndLng?.let { putExtra("endLng", it) }
                putExtra("departureName", originalDepartureName)
                putExtra("destinationName", originalDestinationName)
                putExtra("savedRouteName", addressNicknameText)

                // --- 여기부터 추가된 부분입니다. ---
                // 경로의 고유 키 (transportRouteKey) 전달
                putExtra("transportRouteKey", id)

                // 현재 favoriteStatusMap에 저장된 즐겨찾기 상태 전달
                // 'favoriteStatusMap[id]'는 starBtnClickListener에 의해 업데이트된 최신 상태를 반영합니다.
                putExtra("isFavorite", favoriteStatusMap[id] ?: false)

                // 현재 selectedPathMap에 저장된 선택 상태 전달
                // 클릭 시 'selectedPathMap[id] = true'로 설정했으므로 항상 true가 됩니다.
                putExtra("isSelected", selectedPathMap[id] ?: false) // 이 시점에 true일 것임

                // 디버깅을 위한 로그
                Log.d("TransportationSavedPath", "Intent 데이터 전달: " +
                        "transportRouteKey: $id, " +
                        "isFavorite: ${favoriteStatusMap[id] ?: false}, " +
                        "isSelected: ${selectedPathMap[id] ?: false}"
                )
                // --- 추가된 부분 끝 ---
            }
            startActivity(intent)
        }

        savedPathRootLayout.addView(newLayout)
        savedPathRootLayout.requestLayout()
    }

    /**
     * 즐겨찾기 별 아이콘의 색상을 업데이트하는 헬퍼 함수.
     */
    /*todo::
    *    1. click 시 버튼의 색이 바뀌며, bool 로 지정한 isFavouriteRoot 의 값이 바뀌도록
    *       (단, 이는 데이터베이스 의 생성이 끝난 뒤에야 가능할 성 싶음)
    *    2. 그러니 현재는 버튼의 색을 바꾸는 데에 집중할 예정.
    *    3. 클릭 시 저장된 경로의 순서를 바꿀 수 있도록, 정렬 메소드 를 호출
    *       (이 역시 경로의 개발이 완료된 후 가능할 성 싶음)
    *    4. 이 곳에 필요한 것을 바탕 으로, database 에 들어 가야 할 내역에 대한
    *       체계적 정리 및 이슈화 필요
    *    5. charGPT 의 조언에 따르면, 각 savedPathRootLayout 에 개별적으로
    *       즐겨찾기 상태를 관리하기 위해서는 isFavouritePath 대신 각 LinearLayout
    *       또는 경로 마다 상태를 추적하는 별도의 데이터 구조가 필요 하다고 함. 그 부분에 대한
    *       추가적인 코드 수정이 요구됨 (Map <LinearLayout, Boolean> 이나 data class 를
    *       활용하는 등 */
    private fun updateStarIcon(starImageView: ImageView, isFavorite: Boolean) {
        if (isFavorite) {
            starImageView.setColorFilter(getColor(R.color.yellow), PorterDuff.Mode.SRC_IN)
        } else {
            starImageView.setColorFilter(getColor(R.color.gray), PorterDuff.Mode.SRC_IN)
        }
    }

    /**
     * 즐겨찾기 별 버튼 클릭 시 호출되는 함수.
     * 해당 경로의 즐겨찾기 상태를 토글하고 UI에 반영합니다 (서버 API 호출 없음).
     * @param routePrimaryKey 클릭된 경로의 서버 주요 키 (Int형).
     * @param favouritePathStar 클릭된 별 ImageView.
     */
    private fun starBtnClickListener(routePrimaryKey: Int, favouritePathStar: ImageView) { // 매개변수 타입 Int로 변경
        val currentStatus = favoriteStatusMap[routePrimaryKey] ?: false
        val newStatus = !currentStatus // 새로운 즐겨찾기 상태

        // 즐겨찾기 상태를 맵에 업데이트
        favoriteStatusMap[routePrimaryKey] = newStatus
        // 별 아이콘 UI 업데이트
        updateStarIcon(favouritePathStar, newStatus)

        Log.d("TransportationSavedPath", "경로 주요 키 '$routePrimaryKey'의 즐겨찾기 상태가 $newStatus 로 변경됨 (API 호출 없음)")

        // TODO: 나중에 이 즐겨찾기 상태(favoriteStatusMap)를 바탕으로 정렬하거나 다른 로직을 수행할 수 있습니다.
    }

//    /**
//     * 이 함수를 호출하여 현재 선택된 경로의 주요 키를 얻을 수 있습니다.
//     * 여러 개가 선택될 수 있다면 Map을 직접 순회해야 합니다.
//     * @return 현재 선택된 단일 경로의 주요 키 (Int), 없으면 null.
//     */
//    fun getSelectedRouteKey(): Int? {
//        // 단일 선택만 허용하는 시나리오를 가정합니다.
//        return selectedPathMap.entries.firstOrNull { it.value }?.key
//    }
//
//    /**
//     * 이 함수를 호출하여 특정 경로의 즐겨찾기 상태를 확인할 수 있습니다.
//     * @param routeKey 확인할 경로의 주요 키 (Int).
//     * @return 해당 경로의 즐겨찾기 여부 (Boolean), 없으면 false.
//     */
//    fun isRouteFavorite(routeKey: Int): Boolean {
//        return favoriteStatusMap[routeKey] ?: false
//    }
}