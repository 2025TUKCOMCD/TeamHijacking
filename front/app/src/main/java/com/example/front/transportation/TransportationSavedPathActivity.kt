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
import android.view.View
import com.example.front.presentation.userid
import com.example.front.transportation.error.InvalidResponseDataException
import com.example.front.transportation.error.NetworkConnectionException
import com.example.front.transportation.error.ServerErrorException
import com.example.front.transportation.error.UserNotFoundException

class TransportationSavedPathActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportationSavedPathBinding
    private val savedPathRootLayout: LinearLayout by lazy { findViewById(R.id.transSavedPathLayout) }

    private val favoriteStatusMap: MutableMap<Int, Boolean> = mutableMapOf()
    private val selectedPathMap: MutableMap<Int, Boolean> = mutableMapOf()
    private var _currentLoadedRoutes: MutableList<GetRouteResponseDTO> = mutableListOf()

    private lateinit var startTransportNewPathSearchActivity: ActivityResultLauncher<Intent>

    // RouteProcessor가 object로 정의되어 있으므로, 별도의 인스턴스 변수는 필요 없습니다.
    // private lateinit var routeProcessor: RouteProcessor // 이 줄은 이제 필요 없습니다.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransportationSavedPathBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RouteProcessor가 object로 정의되어 있다면, 별도의 초기화는 필요 없습니다.
        // 다만, BuildConfig.Host_URL 등의 설정이 RouteProcessor 내부에서 잘 되고 있는지 확인해야 합니다.
        // 만약 RouteProcessor를 class로 변경했다면, 이전에 제안했던 OkHttpClient, Retrofit, RouteService 초기화 코드를 여기에 넣고
        // routeProcessor = RouteProcessor(routeService) 와 같이 초기화해야 합니다.
        // 현재 RouteProcessor가 object이므로 이 코드는 제거합니다.

        startTransportNewPathSearchActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("TransportationSavedPath", "TransportNewPathSearchActivity에서 OK 결과 수신. DB 동기화 시작.")
                loadSavedPathsFromDatabase()
            } else {
                Log.d("TransportationSavedPath", "TransportNewPathSearchActivity에서 OK 아닌 결과 수신 또는 취소됨.")
            }
        }

        loadSavedPathsFromDatabase()
    }

    /**
     * 서버(데이터베이스)에서 저장된 경로 목록을 가져와 UI에 동적으로 추가하는 함수.
     * 이 함수는 데이터베이스의 최신 상태를 로드하고, 메모리 맵과 리스트를 업데이트한 후 UI를 재구성합니다.
     * 네트워크 연결 문제, 사용자 ID 없음(404), 그리고 저장된 경로 없음 상태를 명확히 구분하여 처리합니다.
     */
    private fun loadSavedPathsFromDatabase() {
        val app = applicationContext as? userid
        val currentUserLoginId = app?.receivedMessage

        // UI 초기화: 기존 뷰를 모두 제거하고, 접근성 설명을 초기화합니다.
        savedPathRootLayout.removeAllViews()
        savedPathRootLayout.contentDescription = null
        savedPathRootLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

        // 1. 사용자 ID 유효성 검사
        // 로그에서 `No User ID`가 넘어온 것을 확인했으므로, 이 부분을 강화합니다.
        if (currentUserLoginId.isNullOrEmpty() || currentUserLoginId == "No User ID") {
            Log.e("TransportationSavedPath", "사용자 로그인 ID가 유효하지 않거나 없습니다: $currentUserLoginId")
            savedPathRootLayout.contentDescription = "사용자 정보를 가져올 수 없습니다. 다시 로그인 해주세요."
            savedPathRootLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            Toast.makeText(this@TransportationSavedPathActivity, "사용자 정보를 불러올 수 없습니다. 다시 로그인 해주세요.", Toast.LENGTH_LONG).show()
            return // 유효하지 않은 ID면 함수를 여기서 종료합니다.
        }

        lifecycleScope.launch {
            Log.d("TransportationSavedPath", "저장된 경로 로드 시작 (loginId: $currentUserLoginId)")

            try {
                // RouteProcessor.DBGetRoute는 이제 예외를 던지므로, 반환값은 항상 List<GetRouteResponseDTO>입니다.
                val savedRoutes: List<GetRouteResponseDTO> = RouteProcessor.DBGetRoute(currentUserLoginId) // object이므로 RouteProcessor.메서드()로 직접 호출

                // 2. 경로가 성공적으로 로드되었지만 비어있는 경우 (저장된 경로 없음)
                if (savedRoutes.isEmpty()) {
                    Log.d("TransportationSavedPath", "저장된 경로가 없습니다.")
                    savedPathRootLayout.contentDescription = "저장된 경로가 없습니다. 새로운 경로를 추가해보세요."
                    savedPathRootLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                    Toast.makeText(this@TransportationSavedPathActivity, "저장된 경로가 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    // 3. 경로가 성공적으로 로드되고 데이터가 있는 경우
                    Log.d("TransportationSavedPath", "총 ${savedRoutes.size}개의 경로 로드됨.")

                    _currentLoadedRoutes.clear()
                    favoriteStatusMap.clear()
                    selectedPathMap.clear()

                    savedRoutes.forEach { item ->
                        _currentLoadedRoutes.add(item)
                        favoriteStatusMap[item.transportrouteKey] = item.isFavorite ?: false
                        selectedPathMap[item.transportrouteKey] = false
                    }

                    refreshDisplayedRoutes() // UI 갱신

                    Toast.makeText(this@TransportationSavedPathActivity, "저장된 경로를 불러왔습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: UserNotFoundException) {
                // 4. 사용자 ID를 찾을 수 없을 때 (HTTP 404에 해당)
                Log.e("TransportationSavedPath", "사용자 없음 오류 (404): ${e.message}", e)
                savedPathRootLayout.contentDescription = "사용자 정보를 찾을 수 없습니다. 로그인 정보를 다시 확인하거나 회원가입이 필요합니다."
                savedPathRootLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                Toast.makeText(this@TransportationSavedPathActivity, "사용자 정보를 찾을 수 없습니다. 다시 로그인 해주세요.", Toast.LENGTH_LONG).show()
            } catch (e: NetworkConnectionException) {
                // 5. 네트워크 연결 문제 (인터넷 연결 없음, 타임아웃 등)
                Log.e("TransportationSavedPath", "네트워크 연결 오류: ${e.message}", e)
                savedPathRootLayout.contentDescription = "경로를 불러오는데 실패했습니다. 네트워크 연결 상태를 확인해주세요."
                savedPathRootLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                Toast.makeText(this@TransportationSavedPathActivity, "네트워크 연결 오류. 인터넷 연결을 확인하세요.", Toast.LENGTH_LONG).show()
            } catch (e: ServerErrorException) {
                // 6. 기타 서버 오류 (5xx 등, RouteProcessor에서 정의한 ServerErrorException)
                Log.e("TransportationSavedPath", "서버 오류: ${e.message}", e)
                savedPathRootLayout.contentDescription = "서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요."
                savedPathRootLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                Toast.makeText(this@TransportationSavedPathActivity, "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
            } catch (e: InvalidResponseDataException) {
                // 7. 서버 응답은 받았지만 데이터 형식이 유효하지 않은 경우
                Log.e("TransportationSavedPath", "유효하지 않은 응답 데이터 오류: ${e.message}", e)
                savedPathRootLayout.contentDescription = "서버 응답 데이터 형식이 유효하지 않습니다. 앱을 업데이트하거나 다시 시도해주세요."
                savedPathRootLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                Toast.makeText(this@TransportationSavedPathActivity, "데이터 오류. 앱을 업데이트하거나 다시 시도해주세요.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                // 8. 예상치 못한 다른 모든 오류 (위에 정의되지 않은 모든 Exception)
                Log.e("TransportationSavedPath", "경로 로드 중 알 수 없는 오류 발생: ${e.message}", e)
                savedPathRootLayout.contentDescription = "경로를 불러오는 중 알 수 없는 오류가 발생했습니다."
                savedPathRootLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                Toast.makeText(this@TransportationSavedPathActivity, "알 수 없는 오류 발생. 다시 시도해주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 현재 메모리에 로드된 (_currentLoadedRoutes) 경로 데이터를 즐겨찾기 상태와 사용 횟수에 따라 정렬하고 UI에 표시합니다.
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
            val currentFavoriteStatus = favoriteStatusMap[routePrimaryKey] ?: false

            openView(
                id = routePrimaryKey,
                addressNicknameText = nickname,
                departureText = departure,
                destinationText = destination,
                favouritePathStarValue = currentFavoriteStatus,
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

        addressNicknameTextView.text = addressNicknameText.ifEmpty { "별명 없음" }
        addressNicknameTextView.contentDescription = "경로 이름: ${addressNicknameText.ifEmpty { "별명 없음" }}"
        departureTextView.text = "출발지: ${departureText.ifEmpty { "알 수 없음" }}"
        departureTextView.contentDescription = "출발지: ${departureText.ifEmpty { "알 수 없음" }}"
        destinationTextView.text = "목적지: ${destinationText.ifEmpty { "알 수 없음" }}"
        destinationTextView.contentDescription = "목적지: ${destinationText.ifEmpty { "알 수 없음" }}"

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
                putExtra("savedRouteName", addressNicknameText.ifEmpty { "별명 없음" })
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