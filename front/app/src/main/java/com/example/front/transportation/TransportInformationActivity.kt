package com.example.front.transportation

import android.Manifest
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

import android.widget.Button
import android.widget.EditText
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.res.stringArrayResource
import androidx.core.app.ActivityCompat
import com.example.front.R
import com.example.front.databinding.ActivityTransportInformationBinding

import com.example.front.databinding.TransSavedConfirmDialogBinding
import com.example.front.databinding.TransSavedDialogBinding
import com.example.front.transportation.data.searchPath.RouteId
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat.startActivity
import androidx.core.location.LocationManagerCompat.requestLocationUpdates
import androidx.lifecycle.lifecycleScope
import com.example.front.presentation.MainActivity
import com.example.front.presentation.userid
import com.example.front.transportation.data.realTime.RealtimeDTO
import com.example.front.transportation.data.realTime.RealtimeResponseDTO
import com.example.front.transportation.processor.RealtimeProcessor
import com.example.front.transportation.processor.RouteProcessor
import kotlinx.coroutines.launch
import com.example.front.presentation.userid as userid1


class TransportInformationActivity : AppCompatActivity() {

    private lateinit var binding : ActivityTransportInformationBinding
    //imageSwitcher 에 사용할 imageView 배열 선언
    private var transInfoImgArray = intArrayOf(1,2,3,4,5,6) // 교통 안내 이미지 배열 (현재 코드에서는 직접 사용되지 않음)
    private lateinit var transInfoImgSwitcher: ImageSwitcher // 교통 안내 이미지 스위처
    private var transOrder = 0 // 현재 경로 단계 (0부터 시작)
    private lateinit var fusedLocationClient: FusedLocationProviderClient // FusedLocationProviderClient 객체
    private var curLat: Double = 0.0 //현재 위도
    private var curLon: Double = 0.0 //현재 경도
    private var startLat: Double = 0.0
    private var startLng: Double = 0.0
    private var endLat: Double = 0.0
    private var endLng: Double = 0.0
    private var departureName: String = "출발지" // 출발지 이름
    private var destinationName: String = "도착지" // 도착지 이름
    private var loginId: String = "No User ID"

    private var receivedTransportRouteKey: Int? = null
    private var receivedIsFavorite: Boolean = false
    private var receivedIsSelected: Boolean = false
    private var receivedSavedRouteName: String? = null

    private var isWalkingStep: Boolean = false // 도보 단계인지 나타내는 플래그

    // Intent에서 전달받을 데이터
    private var pathTransitType: ArrayList<Int>? = null
    private var transitTypeNo: ArrayList<String>? = null
    private var routeIds: ArrayList<RouteId>? = null

    // 메시지 목록 (contentDescription 설정에 사용, 실제 도착 정보와는 무관)
    private var messagelist: MutableList<String> = mutableListOf(
        "교통 정보 업데이트 중", // 첫 번째 항목도 일반적인 대기 메시지로
        "교통 정보 업데이트 중", // 두 번째 항목도 일반적인 대기 메시지로
        "도착지까지 n m 남음",
        "도착!"
    )

    // 특정 transportLocalID들이 DBUsage = 1을 사용하는 경우를 위한 Set
    private val SPECIFIC_ROUTE_IDS_FOR_DB_USAGE = setOf(
        107, 110, 115, 21, 22, 71, 72, 73, 74, 78, 79, 41, 42, 43, 48, 31, 51
    )

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getLastLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getLastLocation()
            } else -> {
            Log.e("Location", "Location permission denied.")
        }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val app = applicationContext as userid // 사용자 ID를 가져오기 위한 앱 컨텍스트
        loginId = app?.receivedMessage ?: "No User ID"

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 위치 권한 확인 및 요청
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        getLastLocation()

        // GPS 사용 가능 여부 로그
        if (!hasGps()) {
            Log.d("GPS", "This hardware doesn't have GPS.")
        } else {
            Log.d("GPS", "This hardware has GPS.")
        }
        println("GPS available: ${hasGps()}")

        val imsiBtt2: Button = binding.imsiBtt2 // 이전 경로 버튼
        val imsiBtt3: Button = binding.imsiBtt3 // 다음 경로 버튼
        transInfoImgSwitcher = binding.transInfoImgSwitcher

        initTransImgSwitcher()

        // Intent에서 데이터 수신
        pathTransitType = intent.getIntegerArrayListExtra("pathTransitType")
        transitTypeNo = intent.getStringArrayListExtra("transitTypeNo")
        routeIds = intent.getSerializableExtra("routeIds") as? ArrayList<RouteId>

        startLat = intent.getDoubleExtra("startLat", 0.0)
        startLng = intent.getDoubleExtra("startLng", 0.0)
        endLat = intent.getDoubleExtra("endLat", 0.0)
        endLng = intent.getDoubleExtra("endLng", 0.0)
        departureName = intent.getStringExtra("departureName") ?: "출발지"
        destinationName = intent.getStringExtra("destinationName") ?: "도착지"
        intent.extras?.let {
            receivedTransportRouteKey = it.getInt("transportRouteKey", -1).takeIf { key -> key != -1 }
            receivedIsFavorite = it.getBoolean("isFavorite", false)
            receivedIsSelected = it.getBoolean("isSelected", false)
            receivedSavedRouteName = it.getString("savedRouteName")
        }
        Log.d("log", "pathTransitType: $pathTransitType")
        Log.d("log", "transitTypeNo: $transitTypeNo")
        Log.d("log", "routeIds: $routeIds")

        // 초기 교통 정보 업데이트
        pathTransitType?.let {
            updateCurrentTransportationInfo(transOrder)
        }

        // 이전 경로 버튼 클릭 리스너
        imsiBtt2.setOnClickListener {
            if(transOrder > 0){
                transOrder--
                pathTransitType?.let {
                    updateCurrentTransportationInfo(transOrder)
                }
            } else {
                Toast.makeText(this, "첫 번째 경로입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 다음 경로 버튼 클릭 리스너
        imsiBtt3.setOnClickListener {
            pathTransitType?.let { list ->
                if(transOrder + 1 < list.size) {
                    transOrder++
                    updateCurrentTransportationInfo(transOrder)
                } else {
                    // 마지막 경로 단계 표시
                    if (receivedIsSelected) {
                        // --- 변경된 로직: isSelected가 true일 때 Toast 없이 데이터 전송 후 종료 ---
                        lifecycleScope.launch {
                            val responseDTO = RouteProcessor.DBSaveRoute(
                                departureName,
                                destinationName,
                                startLat,
                                startLng,
                                endLat,
                                endLng,
                                receivedSavedRouteName ?: "$departureName - $destinationName", // 기존 저장된 이름 사용 또는 기본값
                                loginId,
                                receivedTransportRouteKey,
                                receivedIsFavorite

                            )

                            if (responseDTO != null && responseDTO.success) {
                                Log.d(
                                    "TransportInfoActivity",
                                    "저장된 경로 데이터 업데이트 성공: ${responseDTO.message}"
                                )
                                // Toast를 보여주지 않고 바로 다음 액티비티로 이동
                            } else {
                                Log.e(
                                    "TransportInfoActivity",
                                    "저장된 경로 데이터 업데이트 실패: ${responseDTO?.message ?: "알 수 없는 오류"}"
                                )
                                // 실패 시에도 사용자 경험을 위해 강제로 이동
                            }

                            // 지연 없이 바로 다음 액티비티로 이동
                            val intent =
                                Intent(this@TransportInformationActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish() // 현재 액티비티 종료
                        }

                    } else {
                        // 새로운 경로를 통해 넘어온 경우: 저장 여부 다이얼로그 표시 (기존 로직 유지)
                        transSavedDialogShow()
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Activity 종료 시 실시간 폴링 중지
        RealtimeProcessor.stopPolling()
    }

    // gps 여부
    private fun hasGps(): Boolean =
        packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

    // 대중교통 index 헬퍼 함수
    private fun getTransitRouteIndex(currentOrder: Int): Int {
        var transitCount = 0
        pathTransitType?.let { types ->
            for (i in 0..currentOrder) {
                val type = types.getOrNull(i)
                if (type == 1 || type == 2) { // 지하철 또는 버스인 경우
                    transitCount++
                }
            }
        }
        return transitCount - 1
    }

    // 데이터베이스 플래그 설정
    private fun getDBUsage(transportLocalId: Int): Int {
        return if (SPECIFIC_ROUTE_IDS_FOR_DB_USAGE.contains(transportLocalId)) {
            1
        } else {
            0
        }
    }

    // 탑승 전 메시지 업데이트
    private fun updateCurrentTransportationInfo(order: Int) {
        if (pathTransitType == null || routeIds == null) return

        val currentTransitType = pathTransitType?.getOrNull(order)
        val isLastStepOfOverallRoute = (order == (pathTransitType?.size ?: 0) - 1)

        // ImageSwitcher 이미지 업데이트 (기존 로직 유지)
        transInfoImgSwitcher.setImageResource(when (currentTransitType) {
            1 -> R.drawable.train_btt
            2 -> R.drawable.bus_btt
            3 -> R.drawable.human_btt
            4 -> R.drawable.complete_btt
            else -> R.drawable.default_btt
        })

        // 현재 단계가 도보인지 대중교통인지 플래그 설정
        isWalkingStep = (currentTransitType == 3)

        // RealtimeProcessor가 요청할 초기 데이터 DTO 구성
        val initialRealtimeDTO: RealtimeDTO? = when (currentTransitType) {
            // 지하철인 경우 (type 1)
            1 -> {
                val routeIndex = getTransitRouteIndex(order)
                val currentRouteId = routeIds?.getOrNull(routeIndex) // RouteId에서 도착정보 가져오기
                currentRouteId?.let { route ->
                    val initialDBUsage = getDBUsage(route.transportLocalID)
                    RealtimeDTO(
                        type = 1, boarding = 1, transportLocalID = route.transportLocalID,
                        dbUsage = initialDBUsage,
                        trainNo = "0",
                        startName = route.transferStations?.getOrNull(0) ?: "",
                        secondName = route.transferStations?.getOrNull(1) ?: "",
                        endName = route.transferStations?.lastOrNull() ?: "",
                        direction = (route.trainDirection ?: ""),
                        location = "0" // 지하철 초기 location (서버와 협의 필요)
                    )
                }
            }
            // 버스인 경우 (type 2)
            2 -> {
                val routeIndex = getTransitRouteIndex(order)
                val currentRouteId = routeIds?.getOrNull(routeIndex)
                currentRouteId?.let { route ->
                    val initialStartNameForBus = route.transferStations?.firstOrNull() ?: ""
                    RealtimeDTO(
                        type = 2, boarding = 1, transportLocalID = route.transportLocalID,
                        stationId = (route.stationInfo?.first() ?: 0), // route.stationInfo는 List<Int>로 가정하고 첫 번째 ID 사용
                        vehid = "0",
                        startOrd = (route.startStationInfo ?: 0),
                        endOrd = (route.endStationInfo ?: 0),
                        location = "0", // 버스 초기 location (서버와 협의 필요)
                        startName = initialStartNameForBus
                    )
                }
            }

            // 도보 `order + 1`을 통해 다음 경로 단계를 미리 확인하여 DTO를 구성.
            3 -> {
                val nextOrder = order + 1
                if (nextOrder < (pathTransitType?.size ?: 0)) { // 다음 경로 단계가 존재할 경우
                    val nextTransitType = pathTransitType?.getOrNull(nextOrder)
                    val nextRouteIndex = getTransitRouteIndex(nextOrder)
                    val nextRouteId = routeIds?.getOrNull(nextRouteIndex)

                    if (nextTransitType == 1 || nextTransitType == 2) { // 다음 단계가 대중교통인 경우
                        nextRouteId?.let { route ->
                            when (nextTransitType) {
                                1 -> { // 다음이 지하철
                                    val initialDBUsage = getDBUsage(route.transportLocalID)
                                    RealtimeDTO(
                                        type = 1, // 다음 경로의 대중교통 타입
                                        boarding = 1, // 'pre-request'임을 나타내는 boarding 값 (서버와 협의)
                                        transportLocalID = route.transportLocalID,
                                        dbUsage = initialDBUsage, trainNo = "0",
                                        startName = route.transferStations?.getOrNull(0) ?: "", // 다음 대중교통의 시작역/정류장
                                        secondName = route.transferStations?.getOrNull(1) ?: "",
                                        endName = route.transferStations?.lastOrNull() ?: "",
                                        direction = (route.trainDirection ?: ""),
                                        location = "0" // 'pre-request'임을 나타내는 마커 (서버와 협의)
                                    )
                                }
                                2 -> { // 다음이 버스
                                    val initialStartNameForBus = route.transferStations?.firstOrNull() ?: ""
                                    RealtimeDTO(
                                        type = 2, // 다음 경로의 대중교통 타입
                                        boarding = 1, // 'pre-request'임을 나타내는 boarding 값 (서버와 협의)
                                        transportLocalID = route.transportLocalID,
                                        stationId = (route.stationInfo?.first() ?: 0),
                                        vehid = "0",
                                        startOrd = (route.startStationInfo ?: 0),
                                        endOrd = (route.endStationInfo ?: 0),
                                        location = "0", // 'pre-request'임을 나타내는 마커 (서버와 협의)
                                        startName = initialStartNameForBus
                                    )
                                }
                                else -> null // 다음 단계가 대중교통이 아니면 null
                            }
                        }
                    } else {
                        // 다음 단계가 도보 또는 알 수 없는 경우, RealtimeProcessor 요청할 필요 없음
                        null
                    }
                } else {
                    // 마지막 경로 단계가 도보인 경우, 더 이상 다음 대중교통 정보가 없으므로 RealtimeProcessor 요청하지 않음
                    null
                }
            }
            else -> null // 알 수 없는 교통수단 타입
        }

        // RealtimeProcessor 시작/중지 로직
        initialRealtimeDTO?.let {
            RealtimeProcessor.stopPolling() // 새로운 경로 단계에 맞게 기존 폴링 중지
            RealtimeProcessor.startPolling(
                endpoint = "/api/realTime", // 동일한 엔드포인트 사용
                initialRealtimeData = it, // 현재 단계에 맞는 DTO 또는 다음 대중교통 DTO 전달
                onResponse = onRealtimeResponseCallback(currentTransitType, order), // 응답 콜백 설정
                onError = { errorMessage, errorCode ->
                    runOnUiThread {
                        if (errorCode == 500) {
                            Toast.makeText(this@TransportInformationActivity,
                                "현재 노선은 실시간 정보 조회를 지원하지 않습니다.",
                                Toast.LENGTH_LONG).show()
                            RealtimeProcessor.stopPolling() // 서버에서 지원하지 않으면 폴링 중지
                            // 메시지 업데이트
                            if (order < messagelist.size) {
                                messagelist[order] = "해당 노선은 실시간 정보 조회를 지원하지 않습니다."
                            } else {
                                // messagelist 크기 부족할 경우 확장
                                while (messagelist.size <= order) { messagelist.add("") }
                                messagelist[order] = "해당 노선은 실시간 정보 조회를 지원하지 않습니다."
                            }
                            transInfoImgSwitcher.contentDescription = messagelist[order]
                        } else {
                            Toast.makeText(this@TransportInformationActivity,
                                "실시간 정보 조회 중 오류 발생: $errorMessage",
                                Toast.LENGTH_LONG).show()
                            // 메시지 업데이트
                            if (order < messagelist.size) {
                                messagelist[order] = "실시간 정보 조회 중 오류가 발생했습니다: $errorMessage"
                            } else {
                                // messagelist 크기 부족할 경우 확장
                                while (messagelist.size <= order) { messagelist.add("") }
                                messagelist[order] = "실시간 정보 조회 중 오류가 발생했습니다: $errorMessage"
                            }
                            transInfoImgSwitcher.contentDescription = messagelist[order]
                        }
                    }
                }
            )
        } ?: run {
            // RealtimeDTO를 구성할 수 없는 경우 (예: 마지막 도보 단계, 또는 다음 대중교통이 없는 도보 단계)
            RealtimeProcessor.stopPolling() // 불필요한 폴링 방지
            runOnUiThread {
                var defaultMessage = "경로 정보를 업데이트할 수 없습니다."
                if (currentTransitType == 3 && isLastStepOfOverallRoute) {
                    defaultMessage = "도보로 최종 목적지에 도착합니다. 경로를 저장하시겠습니까?"
                } else if (currentTransitType == 3) {
                    defaultMessage = "도보 경로입니다. 다음 대중교통 정보가 없습니다." // 다음 대중교통이 아예 없는 경우
                }
                if (order < messagelist.size) {
                    messagelist[order] = defaultMessage
                } else {
                    while (messagelist.size <= order) {
                        messagelist.add("경로 정보 없음")
                    }
                    messagelist[order] = defaultMessage
                }
                transInfoImgSwitcher.contentDescription = messagelist[order]
            }
        }
    }

    // 탑승 후 메시지 업데이트
    private fun onRealtimeResponseCallback(currentTransitType: Int?, currentOrder: Int): (RealtimeResponseDTO) -> Unit {
        // 반환될 람다 정의
        return responseLambda@{ response -> // <--- 여기에 'responseLambda@' 레이블을 추가했습니다.
            Log.d("RealtimeResponse", "Received: $response, isWalkingStep: $isWalkingStep, currentOrder: $currentOrder")

            // UI 업데이트는 UI 스레드에서
            runOnUiThread {
                var dynamicMessage = ""
                val predict1 = response.predictTimes1
                val predict2 = response.predictTimes2
                val location = response.location ?: "알 수 없음"

                // 도보 플래그 true 일 경우
                if (isWalkingStep) {
                    val nextOrder = currentOrder + 1
                    val nextRouteId = routeIds?.getOrNull(getTransitRouteIndex(nextOrder))
                    val nextTransitTypeInPath = pathTransitType?.getOrNull(nextOrder)

                    if (nextRouteId != null && (nextTransitTypeInPath == 1 || nextTransitTypeInPath == 2)) {
                        val nextTransportName = when (nextTransitTypeInPath) {
                            1 -> "지하철"
                            2 -> "버스"
                            else -> "대중교통"
                        }
                        val nextStartStationName = nextRouteId.transferStations?.firstOrNull() ?: "다음 탑승지"

                        dynamicMessage = "현재 도보 경로입니다. ${nextStartStationName}에서 ${nextTransportName} "
                        if (predict1 != "데이터 없음" && predict1 != null && predict1 != "운행종료") {
                            dynamicMessage += "첫차 ${predict1} 후 도착 예정, "
                        }else if(predict1 != null && predict1 == "운행종료") {
                            dynamicMessage += "첫차 운행 종료, "
                        }
                        if (predict2 != "데이터 없음" && predict2 != null && predict1 != "운행종료") {
                            dynamicMessage += "둘째차 ${predict2} 후 도착 예정."
                        }else if(predict2 != null && predict2 == "운행종료") {
                            dynamicMessage += "둘째차 운행 종료."
                        }
                        if ((predict1 == "데이터 없음" || predict1 == null) && (predict2 == "데이터 없음" || predict2 == null)) {
                            dynamicMessage += "도착 예정 정보 없음."
                        }
                    } else {
                        dynamicMessage = "도보 경로입니다. 다음 대중교통 정보가 없습니다."
                    }

                }
                // 도보 플래그 false 일경우 메시지 업데이트
                else {
                    val currentBoardingStatus = RealtimeProcessor.currentRealtimeData?.boarding ?: 1
                    val typeText = when (currentTransitType) {
                        1 -> "지하철"
                        2 -> "버스"
                        else -> "교통수단"
                    }
                    val boardingText = when (currentBoardingStatus) {
                        1 -> "탑승 전"
                        2 -> "탑승 중"
                        else -> "상태 알 수 없음"
                    }

                    // 대중교통 종류
                    when (currentTransitType) {
                        // 버스 경우
                        2 -> { // 버스 로직
                            val currentRouteId = routeIds?.getOrNull(getTransitRouteIndex(currentOrder)) // RouteIds 가져오기
                            val startStationInfo = currentRouteId?.startStationInfo
                            val transferStations = currentRouteId?.transferStations
                            val currentStationNameFromDTO = RealtimeProcessor.currentRealtimeData?.startName

                            // 탑승 전의 경우
                            val busCurrentStationText: String = if (currentBoardingStatus == 1) {
                                currentStationNameFromDTO ?: "알 수 없는 정류장"
                            } // 탑승 후의 경우
                            else {
                                // 정류장 위치
                                val locationAsInt = location.toIntOrNull()
                                // 현재 정류장 위치 추적
                                val indexOffset = if (startStationInfo != null && locationAsInt != null) {
                                    locationAsInt - startStationInfo
                                } else {
                                    null
                                }
                                // 에러 처리
                                if (indexOffset != null && indexOffset >= 0 && indexOffset < (transferStations?.size ?: 0)) {
                                    transferStations?.getOrNull(indexOffset) ?: "알 수 없는 정류장 (ORD ${locationAsInt})"
                                } else {
                                    currentStationNameFromDTO ?: "알 수 없는 정류장 (ORD ${locationAsInt})"
                                }
                            }

                            // 버스 , 탑승 전/후 첫차 ?분 후, 둘째차 ?분 후 도착 예정
                            dynamicMessage = "$typeText, $boardingText. 현재 ${busCurrentStationText}. "
                            if (predict1 != "데이터 없음" && predict1 != null && predict1 != "운행종료") {
                                dynamicMessage += "첫차 ${predict1}. "
                            }else if(predict1 != null && predict1 == "운행종료") {
                                dynamicMessage += "첫차 운행 종료. "
                            }
                            if (predict2 != "데이터 없음" && predict2 != null && predict1 != "운행종료") {
                                dynamicMessage += "둘째차 ${predict2}."
                            }else if( predict2 != null && predict2 == "운행종료") {
                                dynamicMessage += "둘째차 운행 종료."
                            }
                            if ((predict1 == "데이터 없음" || predict1 == null) && (predict2 == "데이터 없음" || predict2 == null)) {
                                dynamicMessage += "도착 예정 정보 없음."
                            }
                        }
                        // 지하철 경우
                        1 -> {
                            // 탑승 전의 경우
                            val subwayCurrentStationText = if (currentBoardingStatus == 1) {
                                RealtimeProcessor.currentRealtimeData?.startName + " 역"
                            } // 탑승 후의 경우
                            else {
                                RealtimeProcessor.currentRealtimeData?.startName + " 역"
                            }

                            // 지하철 , 탑승 전/후 첫차 ?분 후, 둘째차 ?분 후 도착 예정
                            dynamicMessage = "$typeText, $boardingText. 현재 ${subwayCurrentStationText}. "
                            if (predict1 != null && predict1 != "도착 정보 없음") {
                                dynamicMessage += "첫차 ${predict1}. "
                            }
                            if (predict2 != null && predict2 != "도착 정보 없음") {
                                dynamicMessage += "둘째차 ${predict2}."
                            }
                            if ((predict1 == null || predict1 == "도착 정보 없음") && (predict2 == null || predict2 == "도착 정보 없음")) {
                                dynamicMessage += "도착 예정 정보 없음."
                            }
                        }
                    }
                }

                // 메시지 업데이트
                if (currentOrder < messagelist.size) {
                    messagelist[currentOrder] = dynamicMessage
                } else { // 에러 처리
                    while (messagelist.size <= currentOrder) {
                        messagelist.add("경로 정보 없음")
                    }
                    messagelist[currentOrder] = dynamicMessage
                }
                transInfoImgSwitcher.contentDescription = messagelist[currentOrder]
                Log.d("TalkBack", "Updated messagelist[${currentOrder}]: ${messagelist[currentOrder]}")
                Log.d("TalkBack", "Updated contentDescription: ${transInfoImgSwitcher.contentDescription}")
            }


            // 도보 플래그 false 현재 대중교통 처리
            if (!isWalkingStep) {
                var updatedRealtimeDTO: RealtimeDTO? = null
                val currentRealtimeData = RealtimeProcessor.currentRealtimeData

                // 대중교통 타입
                when (currentTransitType) {
                    // 버스 경우
                    2 -> {
                        val receivedVehId = response.vehId
                        val receivedLocation = response.location
                        val locationInt = receivedLocation?.toIntOrNull() ?: 0

                        var currentBoarding = currentRealtimeData?.boarding ?: 1
                        val currentRouteId = routeIds?.getOrNull(getTransitRouteIndex(currentOrder))
                        val startStationInfo = currentRouteId?.startStationInfo
                        val endStationInfo = currentRouteId?.endStationInfo
                        val transferStations = currentRouteId?.transferStations
                        var indexOffset : Int = 0 // 초기값 0 할당 유지

                        // 탑승 여부 판별
                        if (locationInt == startStationInfo && currentBoarding == 1 ) {
                            // 버스 위치가 시작 지점과 일치
                            // currentBoarding = 2 // 다이얼로그에서 사용자의 선택에 따라 설정되도록 이 줄은 제거하거나 주석 처리
                            Log.d("BusRealtime", "버스 위치가 시작 지점과 일치. 탑승 여부 확인 다이얼로그 표시.")

                            runOnUiThread { // UI 조작은 UI 스레드에서 해야 합니다.
                                val dialog = Dialog(this@TransportInformationActivity)
                                dialog.setContentView(R.layout.transportation_arrival_dialog) // 커스텀 레이아웃 설정
                                dialog.setCancelable(false) // 외부 탭 또는 뒤로가기 버튼으로 닫히지 않도록 방지

                                // *** 다이얼로그를 중앙에 띄우기 위한 코드 시작 ***
                                val layoutParams = WindowManager.LayoutParams().apply {
                                    copyFrom(dialog.window?.attributes)
                                    gravity = Gravity.CENTER // 다이얼로그를 중앙에 위치
                                    width = WindowManager.LayoutParams.WRAP_CONTENT // 내용에 맞게 너비 조절
                                    height = WindowManager.LayoutParams.WRAP_CONTENT // 내용에 맞게 높이 조절
                                }
                                dialog.window?.attributes = layoutParams
                                // *** 다이얼로그를 중앙에 띄우기 위한 코드 끝 ***

                                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                                // 커스텀 다이얼로그의 뷰 참조 가져오기
                                val messageTextView = dialog.findViewById<TextView>(R.id.dialog_message)
                                val btnYesBoarded = dialog.findViewById<TextView>(R.id.btn_yes_boarded)
                                val btnNoBoarded = dialog.findViewById<TextView>(R.id.btn_no_boarded)

                                // 동적 메시지 설정 (버스 정류장 이름 사용)
                                // extractedStartName과 같은 변수를 미리 정의하거나 이곳에서 가져와야 합니다.
                                // 여기서는 transferStations의 첫 번째 값을 예시로 사용합니다.
                                val currentStationName = transferStations?.firstOrNull() ?: "현재 정류장"
                                messageTextView.text = "현재 ${currentStationName} 정류장에 버스가 도착했습니다. 탑승하셨나요?"

                                btnYesBoarded.setOnClickListener {
                                    currentBoarding = 2 // 사용자가 '네'를 선택하면 탑승 상태를 2로 변경
                                    Log.d("BusRealtime", "사용자 탑승 확인. boarding을 2(탑승 중)으로 변경.")

                                    // RealtimeDTO 업데이트 및 전송
                                    updatedRealtimeDTO = RealtimeDTO(
                                        type = 2, // 버스 타입
                                        boarding = currentBoarding,
                                        transportLocalID = currentRouteId?.transportLocalID ?: 0,
                                        // 버스 DTO에 필요한 다른 필드들 (지하철 DTO와 다를 수 있음)
                                        stationId = (currentRouteId?.stationInfo?.getOrNull(indexOffset) ?: 0),
                                        vehid = receivedVehId,
                                        startOrd = (startStationInfo ?: 0),
                                        endOrd = (endStationInfo ?: 0),
                                        location = receivedLocation,
                                        startName = transferStations?.firstOrNull() ?: "" // 탑승 후 startName은 첫 번째 역
                                    )
                                    updatedRealtimeDTO?.let { RealtimeProcessor.requestUpdate(it) }
                                    dialog.dismiss() // 다이얼로그 닫기
                                }

                                btnNoBoarded.setOnClickListener {
                                    currentBoarding = 1 // 사용자가 '아니요'를 선택하면 탑승 상태를 1로 유지
                                    Log.d("BusRealtime", "사용자 탑승 취소. boarding을 1(탑승 전)으로 유지.")

                                    // RealtimeDTO 업데이트 및 전송 (탑승하지 않았으므로 상태 유지)
                                    // 필요하다면 trainNo처럼 busId 등을 "0"으로 초기화할 수도 있습니다.
                                    updatedRealtimeDTO = RealtimeDTO(
                                        type = 2, // 버스 타입
                                        boarding = currentBoarding,
                                        transportLocalID = currentRouteId?.transportLocalID ?: 0,
                                        // 버스 DTO에 필요한 다른 필드들
                                        stationId = (currentRouteId?.stationInfo?.getOrNull(indexOffset) ?: 0), // 탑승 전이라도 0번 인덱스 사용 가능
                                        vehid = receivedVehId, // 변경 없을 수 있음
                                        startOrd = (startStationInfo ?: 0),
                                        endOrd = (endStationInfo ?: 0),
                                        location = receivedLocation,
                                        startName = transferStations?.firstOrNull() ?: "" // 탑승 전 startName은 첫 번째 역
                                    )
                                    updatedRealtimeDTO?.let { RealtimeProcessor.requestUpdate(it) }
                                    dialog.dismiss() // 다이얼로그 닫기
                                }

                                dialog.show() // 커스텀 다이얼로그 표시
                            }
                            return@responseLambda // 다이얼로그를 띄웠으므로 현재 람다 실행 종료
                        }

                        // 탑승 전 현재 정류장 이름 (다이얼로그에서 설정된 currentBoarding 값에 따라 달라짐)
                        val updatedStartNameForBus = if (currentBoarding == 1) {
                            transferStations?.firstOrNull() ?: ""
                        } else {
                            indexOffset = if (startStationInfo != null && locationInt != null) {
                                val calculatedOffset = locationInt - startStationInfo
                                if (calculatedOffset < 0) 0 else calculatedOffset
                            } else {
                                0
                            }
                            if (indexOffset >= 0 && indexOffset < (transferStations?.size ?: 0)) {
                                transferStations?.getOrNull(indexOffset) ?: ""
                            } else {
                                currentRealtimeData?.startName ?: ""
                            }
                        }

                        // RealtimeDTO 업데이트 (이 부분은 다이얼로그에서 이미 처리되었을 수 있으므로 로직 흐름 검토 필요)
                        // 다이얼로그가 뜨고 return@responseLambda 되면 이 아래 코드는 실행되지 않습니다.
                        // 다이얼로그를 띄우지 않는 다른 케이스에서만 실행되도록 하거나,
                        // 다이얼로그 내부에서 DTO를 업데이트하지 않고, 다이얼로그 결과에 따라 여기서 업데이트되도록 재구성할 수 있습니다.
                        updatedRealtimeDTO = RealtimeDTO(
                            type = 2, boarding = currentBoarding, transportLocalID = currentRouteId?.transportLocalID ?: 0,
                            stationId = (currentRouteId?.stationInfo?.getOrNull(indexOffset) ?: 0),
                            vehid = receivedVehId,
                            startOrd = (startStationInfo ?: 0),
                            endOrd = (endStationInfo ?: 0),
                            location = receivedLocation,
                            startName = updatedStartNameForBus
                        )

                        if (response.predictTimes1 == "데이터 없음" && response.predictTimes2 == "데이터 없음" && currentBoarding == 1) {
                            runOnUiThread {
                                Toast.makeText(this@TransportInformationActivity,
                                    "현재 버스 노선은 도착 예정 시간을 지원하지 않습니다.",
                                    Toast.LENGTH_LONG).show()
                            }
                        }

                        if (locationInt == (endStationInfo ?: -1) && (currentOrder == (pathTransitType?.size ?: 0) - 1)) {
                            Log.d("BusRealtime", "버스 최종 목적지에 도착했습니다. 경로 종료.")
                            runOnUiThread {
                                Toast.makeText(this@TransportInformationActivity, "{목적지에 도착했습니다}!", Toast.LENGTH_LONG).show()
                                RealtimeProcessor.stopPolling()
                                transInfoImgSwitcher.setImageResource(R.drawable.complete_btt)
                                messagelist[currentOrder] = "도착!"
                                transInfoImgSwitcher.contentDescription = messagelist[currentOrder]
                                //transSavedDialogShow() // 필요에 따라 다이얼로그 호출
                            }
                            return@responseLambda
                        }
                    }
                    // 지하철 경우
                    1 -> {
                        val receivedPredictTimes1 = response.predictTimes1
                        val receivedTrainNo = response.trainNo
                        val receivedLocation = response.location

                        var currentBoarding = currentRealtimeData?.boarding ?: 1
                        val currentRouteId = routeIds?.getOrNull(getTransitRouteIndex(currentOrder))
                        val transportLocalID = currentRouteId?.transportLocalID ?: 0
                        val initialDBUsage = getDBUsage(transportLocalID)
                        val trainDirection = currentRouteId?.trainDirection

                        val previousTrainNo: String = currentRealtimeData?.trainNo ?: "0"
                        var finalTrainNo: String = previousTrainNo

                        if (!receivedTrainNo.equals("0")) {
                            finalTrainNo = receivedTrainNo
                            Log.d("SubwayRealtime", "새로운 trainNo ${finalTrainNo} 수신 및 반영.")
                        }
                        Log.d("SubwayRealtime", "현재 transportLocalID: $transportLocalID, 결정된 dbUsage: $initialDBUsage")

                        val transferStations = currentRouteId?.transferStations
                        var extractedStartName = currentRealtimeData?.startName ?: (transferStations?.getOrNull(0) ?: "")
                        var extractedSecondName = currentRealtimeData?.secondName ?: (transferStations?.getOrNull(1) ?: "")
                        val extractedEndName = currentRouteId?.transferStations?.lastOrNull() ?: ""

                        var actualLocationForComparison: String? = null
                        if (initialDBUsage == 1) {
                            actualLocationForComparison = receivedLocation
                        } else {
                            val locationIndex = receivedLocation?.toIntOrNull()
                            if (locationIndex != null && locationIndex >= 0 && locationIndex < (transferStations?.size ?: 0)) {
                                actualLocationForComparison = transferStations?.getOrNull(locationIndex)
                            } else {
                                actualLocationForComparison = receivedLocation
                            }
                        }

                        if (initialDBUsage == 1 && currentBoarding == 1 && receivedPredictTimes1 == "0분 후") {
                            Log.d("SubwayRealtime", "DBUsage 1, 탑승 전 (1), '0분 후' 도착. 탑승 여부 확인 다이얼로그 표시.")
                            runOnUiThread {
                                runOnUiThread {
                                    val dialog = Dialog(this@TransportInformationActivity)
                                    dialog.setContentView(R.layout.transportation_arrival_dialog) // 커스텀 레이아웃 설정
                                    dialog.setCancelable(false) // 외부 탭 또는 뒤로가기 버튼으로 닫히지 않도록 방지

                                    // *** 다이얼로그를 중앙에 띄우기 위한 코드 시작 ***
                                    val layoutParams = WindowManager.LayoutParams().apply {
                                        copyFrom(dialog.window?.attributes)
                                        gravity = Gravity.CENTER // 다이얼로그를 중앙에 위치
                                        width = WindowManager.LayoutParams.WRAP_CONTENT // 내용에 맞게 너비 조절
                                        height = WindowManager.LayoutParams.WRAP_CONTENT // 내용에 맞게 높이 조절
                                    }
                                    dialog.window?.attributes = layoutParams
                                    // *** 다이얼로그를 중앙에 띄우기 위한 코드 끝 ***

                                    // 주변 투명하게 보이게 함 (선택 사항, 다이얼로그 배경이 XML에서 설정된 경우)
                                    // XML의 background 속성 (예: @drawable/setting_view)이 투명도가 없으면 이 코드는 효과가 미미할 수 있습니다.
                                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                                    // 커스텀 다이얼로그의 뷰 참조 가져오기
                                    // 주의: transportation_arrival_dialog.xml에 dialog_title 이라는 ID가 없으므로,
                                    // 해당 TextView를 사용하려면 XML에 <TextView android:id="@+id/dialog_title" ... /> 을 추가해야 합니다.
                                    val messageTextView = dialog.findViewById<TextView>(R.id.dialog_message)
                                    // XML의 구성에 맞춰 TextView로 참조합니다.
                                    val btnYesBoarded = dialog.findViewById<TextView>(R.id.btn_yes_boarded)
                                    val btnNoBoarded = dialog.findViewById<TextView>(R.id.btn_no_boarded)

                                    // 다이얼로그 제목 설정 (XML에 dialog_title이 있는 경우에만 유효)
                                    // 현재 XML에는 dialog_title이 없으므로, 이 줄은 효과가 없거나 titleTextView가 null일 수 있습니다.

                                    // 동적 메시지 설정
                                    messageTextView.text = "현재 ${extractedStartName}역에 열차가 도착했습니다. 지하철에 탑승하셨나요?"

                                    btnYesBoarded.setOnClickListener {
                                        val newBoarding = 2
                                        Log.d("SubwayRealtime", "사용자 탑승 확인 (DBUsage 0). boarding을 2(탑승 중)으로 변경.")
                                        updatedRealtimeDTO = RealtimeDTO(
                                            type = 1, boarding = newBoarding, transportLocalID = transportLocalID,
                                            dbUsage = initialDBUsage, trainNo = finalTrainNo,
                                            startName = extractedStartName, secondName = extractedSecondName, endName = extractedEndName,
                                            direction = (trainDirection ?: ""), location = receivedLocation
                                        )
                                        updatedRealtimeDTO?.let { RealtimeProcessor.requestUpdate(it) }
                                        dialog.dismiss() // 커스텀 다이얼로그 닫기
                                    }

                                    btnNoBoarded.setOnClickListener {
                                        val newBoarding = 1
                                        val newTrainNo = "0" // 사용자 요청에 따라 추가된 로직
                                        Log.d("SubwayRealtime", "사용자 탑승 취소 (DBUsage 0). boarding 1(탑승 전) 유지, trainNo ${newTrainNo}으로 초기화.")
                                        updatedRealtimeDTO = RealtimeDTO(
                                            type = 1, boarding = newBoarding, transportLocalID = transportLocalID,
                                            dbUsage = initialDBUsage, trainNo = newTrainNo, // 사용자 요청에 따라 추가된 로직 적용 startName = extractedStartName,
                                            secondName = extractedSecondName, endName = extractedEndName,
                                            direction = (trainDirection ?: ""), location = receivedLocation
                                        )
                                        updatedRealtimeDTO?.let { RealtimeProcessor.requestUpdate(it) }
                                        dialog.dismiss() // 커스텀 다이얼로그 닫기
                                    }

                                    dialog.show() // 커스텀 다이얼로그 표시
                                }
                            }
                            return@responseLambda // <--- 람다 레이블을 사용하여 해당 람다의 실행을 종료합니다.
                        } else if (initialDBUsage == 1 && currentBoarding == 2 && receivedPredictTimes1 == "0분 후") {
                            Log.d("SubwayRealtime", "DBUsage 1, 탑승 중 (2), '0분 후' 도착. 다음 역으로 이동.")
                            val currentStationIndex = transferStations?.indexOf(extractedStartName) ?: -1
                            if (currentStationIndex != -1 && currentStationIndex < (transferStations?.size ?: 0) - 1) {
                                val nextStationName = transferStations?.getOrNull(currentStationIndex + 1) ?: extractedStartName
                                val nextNextStationName = transferStations?.getOrNull(currentStationIndex + 2) ?: ""
                                Log.d("SubwayRealtime", "현재 역 (${extractedStartName}) -> 다음 역으로 이동: ${nextStationName}")
                                runOnUiThread {
                                    if (nextStationName == extractedEndName) {
                                        Toast.makeText(this@TransportInformationActivity, "${nextStationName}역입니다. 내리실 준비를 해주세요.", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(this@TransportInformationActivity, "${nextStationName}역에 도착했습니다. 다음 역으로 진행합니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                updatedRealtimeDTO = RealtimeDTO(
                                    type = 1, boarding = 2, transportLocalID = transportLocalID,
                                    dbUsage = initialDBUsage, trainNo = finalTrainNo,
                                    startName = nextStationName, secondName = nextNextStationName,
                                    endName = extractedEndName, direction = (trainDirection ?: ""),
                                    location = receivedLocation
                                )
                            } else if (extractedStartName == extractedEndName && (currentOrder == (pathTransitType?.size ?: 0) - 1)) {
                                Log.d("SubwayRealtime", "DBUsage 1, 최종 목적지에 도착했습니다. 경로 종료.")
                                runOnUiThread {
                                    Toast.makeText(this@TransportInformationActivity,
                                        "목적지에 도착했습니다!",
                                        Toast.LENGTH_LONG).show()
                                    RealtimeProcessor.stopPolling()
                                    transInfoImgSwitcher.setImageResource(R.drawable.complete_btt)
                                    messagelist[currentOrder] = "도착!"
                                    transInfoImgSwitcher.contentDescription = messagelist[currentOrder]
                                }
                                return@responseLambda // <--- 람다 레이블을 사용하여 해당 람다의 실행을 종료합니다.
                            }
                            updatedRealtimeDTO?.let { RealtimeProcessor.requestUpdate(it) }
                            return@responseLambda // <--- 람다 레이블을 사용하여 해당 람다의 실행을 종료합니다.
                        }
                        else {
                            if (currentBoarding == 1 && actualLocationForComparison == extractedStartName) {
                                Log.d("SubwayRealtime", "DBUsage 0, location(${receivedLocation})이 출발역(${extractedStartName})과 일치. 탑승 여부 확인 다이얼로그 표시.")
                                runOnUiThread {
                                    runOnUiThread {
                                        val dialog = Dialog(this@TransportInformationActivity)
                                        dialog.setContentView(R.layout.transportation_arrival_dialog) // 커스텀 레이아웃 설정
                                        dialog.setCancelable(false) // 외부 탭 또는 뒤로가기 버튼으로 닫히지 않도록 방지

                                        // *** 다이얼로그를 중앙에 띄우기 위한 코드 시작 ***
                                        val layoutParams = WindowManager.LayoutParams().apply {
                                            copyFrom(dialog.window?.attributes)
                                            gravity = Gravity.CENTER // 다이얼로그를 중앙에 위치
                                            width = WindowManager.LayoutParams.WRAP_CONTENT // 내용에 맞게 너비 조절
                                            height = WindowManager.LayoutParams.WRAP_CONTENT // 내용에 맞게 높이 조절
                                        }
                                        dialog.window?.attributes = layoutParams
                                        // *** 다이얼로그를 중앙에 띄우기 위한 코드 끝 ***

                                        // 주변을 투명하게 보이게 함 (XML의 background 속성에서 투명도가 이미 설정되어 있을 경우 효과적)
                                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                                        // 커스텀 다이얼로그의 뷰 참조 가져오기
                                        val messageTextView = dialog.findViewById<TextView>(R.id.dialog_message)
                                        val btnYesBoarded = dialog.findViewById<TextView>(R.id.btn_yes_boarded)
                                        val btnNoBoarded = dialog.findViewById<TextView>(R.id.btn_no_boarded)

                                        // 동적 메시지 설정
                                        messageTextView.text = "현재 ${extractedStartName}역에 열차가 도착했습니다. 지하철에 탑승하셨나요?"

                                        // '네, 탑승했습니다.' 버튼 클릭 리스너
                                        btnYesBoarded.setOnClickListener {
                                            val newBoarding = 2
                                            Log.d("SubwayRealtime", "사용자 탑승 확인 (DBUsage 0). boarding을 2(탑승 중)으로 변경.")
                                            updatedRealtimeDTO = RealtimeDTO(
                                                type = 1,
                                                boarding = newBoarding,
                                                transportLocalID = transportLocalID,
                                                dbUsage = initialDBUsage,
                                                trainNo = finalTrainNo,
                                                startName = extractedStartName,
                                                secondName = extractedSecondName,
                                                endName = extractedEndName,
                                                direction = (trainDirection ?: ""),
                                                location = receivedLocation
                                            )
                                            updatedRealtimeDTO?.let { RealtimeProcessor.requestUpdate(it) }
                                            dialog.dismiss() // 다이얼로그 닫기
                                        }

                                        // '아니요, 아직입니다.' 버튼 클릭 리스너
                                        btnNoBoarded.setOnClickListener {
                                            val newBoarding = 1
                                            val newTrainNo = "0" // 사용자 요청에 따라 추가된 로직 (탑승 취소 시 trainNo 초기화)
                                            Log.d("SubwayRealtime", "사용자 탑승 취소 (DBUsage 0). boarding 1(탑승 전) 유지, trainNo ${newTrainNo}으로 초기화.")
                                            updatedRealtimeDTO = RealtimeDTO(
                                                type = 1,
                                                boarding = newBoarding,
                                                transportLocalID = transportLocalID,
                                                dbUsage = initialDBUsage,
                                                trainNo = newTrainNo, // 사용자 요청에 따라 추가된 로직 적용
                                                startName = extractedStartName,
                                                secondName = extractedSecondName,
                                                endName = extractedEndName,
                                                direction = (trainDirection ?: ""),
                                                location = receivedLocation
                                            )
                                            updatedRealtimeDTO?.let { RealtimeProcessor.requestUpdate(it) }
                                            dialog.dismiss() // 다이얼로그 닫기
                                        }

                                        dialog.show() // 커스텀 다이얼로그 표시
                                    }
                                }
                                return@responseLambda // <--- 람다 레이블을 사용하여 해당 람다의 실행을 종료합니다.
                            } else if (currentBoarding == 2 && actualLocationForComparison == extractedSecondName) {
                                Log.d("SubwayRealtime", "DBUsage 0, 탑승 중 (2), 다음 역 (${extractedSecondName})에 도착.")
                                val currentStationIndex = transferStations?.indexOf(extractedStartName) ?: -1
                                if (currentStationIndex != -1 && currentStationIndex < (transferStations?.size ?: 0) - 1) {
                                    val nextStationName = transferStations?.getOrNull(currentStationIndex + 1) ?: extractedStartName
                                    val nextNextStationName = transferStations?.getOrNull(currentStationIndex + 2) ?: ""
                                    Log.d("SubwayRealtime", "현재 역 (${extractedStartName}) -> 다음 역으로 이동: ${nextStationName}")
                                    runOnUiThread {
                                        if (nextStationName == extractedEndName) {
                                            Toast.makeText(this@TransportInformationActivity,
                                                "${nextStationName}역입니다. 내리실 준비를 해주세요.",
                                                Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(this@TransportInformationActivity,
                                                "${nextStationName}역에 도착했습니다. 다음 역으로 진행합니다.",
                                                Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    updatedRealtimeDTO = RealtimeDTO(
                                        type = 1,
                                        boarding = 2,
                                        transportLocalID = transportLocalID,
                                        dbUsage = initialDBUsage,
                                        trainNo = finalTrainNo,
                                        startName = nextStationName,
                                        secondName = nextNextStationName,
                                        endName = extractedEndName,
                                        direction = (trainDirection ?: ""),
                                        location = receivedLocation
                                    )
                                } else if (extractedStartName == extractedEndName && (currentOrder == (pathTransitType?.size ?: 0) - 1)) {
                                    Log.d("SubwayRealtime", "DBUsage 0, 최종 목적지에 도착했습니다. 경로 종료.")
                                    runOnUiThread {
                                        Toast.makeText(this@TransportInformationActivity,
                                            "목적지에 도착했습니다!",
                                            Toast.LENGTH_LONG).show()
                                        RealtimeProcessor.stopPolling()
                                        transInfoImgSwitcher.setImageResource(R.drawable.complete_btt)
                                        messagelist[currentOrder] = "도착!"
                                        transInfoImgSwitcher.contentDescription = messagelist[currentOrder]
                                        transSavedDialogShow()
                                    }
                                    return@responseLambda // <--- 람다 레이블을 사용하여 해당 람다의 실행을 종료합니다.
                                }
                                updatedRealtimeDTO?.let { RealtimeProcessor.requestUpdate(it) }
                                return@responseLambda // <--- 람다 레이블을 사용하여 해당 람다의 실행을 종료합니다.
                            }
                        }
                        updatedRealtimeDTO = RealtimeDTO(
                            type = 1,
                            boarding = currentBoarding,
                            transportLocalID = transportLocalID,
                            dbUsage = initialDBUsage,
                            trainNo = finalTrainNo,
                            startName = extractedStartName,
                            secondName = extractedSecondName,
                            endName = extractedEndName,
                            direction = (trainDirection ?: ""),
                            location = receivedLocation
                        )
                    }
                }

                updatedRealtimeDTO?.let {
                    RealtimeProcessor.requestUpdate(it)
                }
            }
        }
    }


    // 위치 정보를 가져오는 함수 (더미 구현)
    private fun getCurrentLocation(): Location {
        // 실제로는 FusedLocationProviderClient.getCurrentLocation 또는 LocationRequest 등을 사용하여 위치를 가져와야 합니다.
        // 현재는 더미 Location 객체를 반환합니다.
        return Location("provider")
    }

    // 경로 저장 다이얼로그
    private fun transSavedDialogShow(){
        val transSavedDialogBinding = try {
            TransSavedDialogBinding.inflate(layoutInflater)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (transSavedDialogBinding == null) {
            println("TransSavedDialogBinding.inflate 실패")
            return
        }

        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(transSavedDialogBinding.root)
            .create()

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 화면 너비의 90%
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val yesSavedBtt: TextView = transSavedDialogBinding.YesSavedBtt
        val noSavedBtt: TextView = transSavedDialogBinding.NoSavedBtt

        yesSavedBtt.setOnClickListener{
             transSavedNicknameDialogShow() // '예' 선택 시 별명 설정 다이얼로그 표시
            dialog.dismiss()
        }

        noSavedBtt.setOnClickListener{
            dialog.dismiss() // '아니오' 선택 시 다이얼로그 닫기
            val intent = Intent(this@TransportInformationActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


  // 별정 저장 다이얼로그
    private fun transSavedNicknameDialogShow(){
        val transSavedConfirmDialogBinding = try {
            TransSavedConfirmDialogBinding.inflate(layoutInflater)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if(transSavedConfirmDialogBinding == null) {
            println("TransSavedConfigDialogBinding.inflate fail")
            return
        }

        val dialog2 = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(transSavedConfirmDialogBinding.root)
            .create()

        dialog2.show()
        dialog2.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT)

        val transSavedConfirmBtt: TextView = transSavedConfirmDialogBinding.transSavedConfirmBtt
        val cancelBtt: TextView = transSavedConfirmDialogBinding.cancelBtt
        val addressNickNameEditText: EditText = transSavedConfirmDialogBinding.addressNickNameEditText
        val savedConfirmTextView: TextView = transSavedConfirmDialogBinding.savedConfirmTextView

        // 여기서 addressNickNameEditText의 기본값을 설정합니다.
        addressNickNameEditText.setText("$departureName - $destinationName")

        transSavedConfirmBtt.setOnClickListener{
            if(!TextUtils.isEmpty(addressNickNameEditText.text)){ // 별명이 비어있지 않은 경우
                Log.d("log","텍스트 전달됨. ${addressNickNameEditText.text}")
                lifecycleScope.launch {
                    Log.d("startLat", startLat.toString())
                    Log.d("startLng",startLng.toString())
                    Log.d("endLat",endLat.toString())
                    Log.d("endLng",endLng.toString())
                    val responseDTO = RouteProcessor.DBSaveRoute( // 변수명을 responseDTO로 변경하여 명확하게
                        departureName,
                        destinationName,
                        startLat,
                        startLng,
                        endLat,
                        endLng,
                        addressNickNameEditText.text.toString(), // 별명
                        loginId
                    )

                    // 응답 DTO가 null이 아니고, 성공 플래그가 true인 경우
                    if (responseDTO != null && responseDTO.success) {
                        // 백엔드로부터 받은 'message'를 직접 토스트로 표시하여 더 자세한 피드백 제공
                        Toast.makeText(this@TransportInformationActivity, responseDTO.message, Toast.LENGTH_SHORT).show()
                        dialog2.dismiss() // 저장 성공 시 다이얼로그 닫기

                        // --- MainActivity로 이동 ---
                        val intent = Intent(this@TransportInformationActivity, MainActivity::class.java)
                        kotlinx.coroutines.delay(2000L)
                        startActivity(intent)
                        finish() // 현재 TransportationInformationActivity 종료
                    } else {
                        // 응답 DTO가 null이거나 success가 false일 경우의 오류 메시지
                        // responseDTO가 null이면 일반적인 실패 메시지, 아니면 responseDTO.message 사용
                        val errorMessage = responseDTO?.message ?: "경로 저장에 실패했습니다. 다시 시도해주세요."
                        Toast.makeText(this@TransportInformationActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        // 실패 시 다이얼로그는 닫지 않거나, 다른 피드백 제공 (여기서는 닫지 않음)
                    }
                }
            }else{
                savedConfirmTextView.text = "별명을 빈 칸으로 지정할 수 없습니다." // 별명이 빈 경우 오류 메시지
            }
        }

        cancelBtt.setOnClickListener {
            dialog2.dismiss()
        }
    }

    // ImageSwitcher를 초기화하고 기본 이미지를 설정합니다.
    private fun initTransImgSwitcher(){
        Log.d("hyunbin", "함수 입성")
        transInfoImgSwitcher.setFactory({
            val imgView = ImageView(applicationContext)
            imgView.scaleType = ImageView.ScaleType.FIT_CENTER
            Log.d("hyunbin", "버그1")
            imgView
        })
        Log.d("hyunbin", "버그2")
        transInfoImgSwitcher.setImageResource(R.drawable.default_btt)
        Log.d("hyunbin", "버그3")
    }

    // (현재 사용되지 않는) ImageSwitcher의 이미지를 다음으로 변경하는 함수입니다.
    private fun whatIsNext(index:Int = 0) {
        if(index >= transInfoImgArray.size) {
            transInfoImgSwitcher.setImageResource(transInfoImgArray[0])
        }else{
            transInfoImgSwitcher.setImageResource(transInfoImgArray[index])
        }
        return
    }

    // 마지막으로 알려진 위치를 가져오거나, 가져올 수 없을 경우 위치 업데이트를 요청합니다.
    private fun getLastLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        curLat = it.latitude
                        curLon = it.longitude
                        Log.d("hyunbin", "Latitude: $curLat, Longitude: $curLon")
                    } ?: run {
                        Log.w("hyunbin", "Last known location was null, try requesting location updates.")
                        requestLocationUpdates() // 마지막 위치가 없으면 위치 업데이트 요청
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Location", "Failed to get last location: ${e.message}")
                }
        } catch (securityException: SecurityException) {
            Log.e("Location", "Security exception while getting last location.")
        }
    }

    // * 위치 업데이트를 요청하는 함수입니다. 현재는 구현이 미완성입니다.
    private fun requestLocationUpdates() {
        Log.w("Location", "requestLocationUpdates() not fully implemented. Implement LocationRequest and LocationCallback for continuous updates.")
        // TODO: LocationRequest 및 LocationCallback을 사용하여 주기적인 위치 업데이트 로직 구현
    }
}