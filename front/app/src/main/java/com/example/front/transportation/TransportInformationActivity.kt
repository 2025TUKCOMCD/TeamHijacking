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
import androidx.core.location.LocationManagerCompat.requestLocationUpdates
import androidx.lifecycle.lifecycleScope
import com.example.front.presentation.MainActivity
import com.example.front.transportation.data.realTime.RealtimeDTO
import com.example.front.transportation.data.realTime.RealtimeResponseDTO
import com.example.front.transportation.processor.RealtimeProcessor
import com.example.front.transportation.processor.RouteProcessor
import kotlinx.coroutines.launch

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

    private var receivedTransportRouteKey: Int? = null
    private var receivedIsFavorite: Boolean = false
    private var receivedIsSelected: Boolean = false
    private var receivedSavedRouteName: String? = null

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
                                receivedTransportRouteKey,
                                receivedIsFavorite
                                 // isSelected 정보 전달
                                // loginId 등 다른 필요한 필드 추가
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
                        Toast.makeText(this, "마지막 경로입니다. 경로를 저장하시겠습니까?", Toast.LENGTH_SHORT).show()
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

    /**
     * 기기에 GPS 기능이 있는지 확인합니다.
     */
    private fun hasGps(): Boolean =
        packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

    /**
     * pathTransitType의 현재 order까지 대중교통(1 또는 2)이 몇 번 등장했는지 계산하여
     * routeIds에 접근할 인덱스를 반환하는 헬퍼 함수
     */
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


    /**
     * transportLocalId에 따라 DBUsage 값을 결정하는 헬퍼 함수
     * 특정 route ID에 대해 dbUsage가 1로 설정됩니다.
     */
    private fun getDBUsage(transportLocalId: Int): Int {
        return if (SPECIFIC_ROUTE_IDS_FOR_DB_USAGE.contains(transportLocalId)) {
            1
        } else {
            0
        }
    }


    /**
     * 현재 경로 단계에 따라 교통 정보를 업데이트하고 RealtimeProcessor를 제어합니다.
     */
    private fun updateCurrentTransportationInfo(order: Int) {
        if (pathTransitType == null || routeIds == null) return

        val currentTransitType = pathTransitType?.getOrNull(order)
        val isLastStepOfOverallRoute = (order == (pathTransitType?.size ?: 0) - 1)

        transInfoImgSwitcher.setImageResource(when (currentTransitType) {
            1 -> R.drawable.train_btt
            2 -> R.drawable.bus_btt
            3 -> R.drawable.human_btt
            4 -> R.drawable.complete_btt
            else -> R.drawable.default_btt
        })

        if (order < messagelist.size) {
            transInfoImgSwitcher.contentDescription = messagelist[order]
        } else {
            transInfoImgSwitcher.contentDescription = "교통 정보"
        }

        if (currentTransitType == 3) {
            RealtimeProcessor.stopPolling()
            println("RealtimeProcessor: Current step is walking (type 3), stopping polling.")
            runOnUiThread {
                val walkingMessage = "도보 경로입니다."
                if (order < messagelist.size) {
                    messagelist[order] = walkingMessage
                } else {
                    while (messagelist.size <= order) {
                        messagelist.add("경로 정보 없음")
                    }
                    messagelist[order] = walkingMessage
                }
                transInfoImgSwitcher.contentDescription = messagelist[order]
                // 도보 경로의 마지막 단계에서는 별도의 다이얼로그가 필요 없다면 호출하지 않음
                // if (isLastStepOfOverallRoute) { transSavedDialogShow() }
            }
        } else if (currentTransitType == 1 || currentTransitType == 2) {
            val routeIndex = getTransitRouteIndex(order)
            val currentRouteId = routeIds?.getOrNull(routeIndex)

            currentRouteId?.let { route ->
                val transportLocalID = route.transportLocalID
                val transferStations = route.transferStations // List<String> (순서대로 정류장 이름)
                val startStationInfo = route.startStationInfo // ORD 값 (transferStations의 첫 번째에 해당)
                val endStationInfo = route.endStationInfo     // ORD 값 (transferStations의 마지막에 해당)
                val trainDirection = route.trainDirection

                val onRealtimeResponse: (RealtimeResponseDTO) -> Unit = realtimeResponse@{ response ->
                    Log.d("RealtimeResponse", "Received: $response")

                    var updatedRealtimeDTO: RealtimeDTO? = null
                    var currentRealtimeData = RealtimeProcessor.currentRealtimeData

                    runOnUiThread {
                        val predict1 = response.predictTimes1
                        val predict2 = response.predictTimes2
                        val location = response.location ?: "알 수 없음" // 노선 ORD 값 (String)
                        val locationAsInt = location.toIntOrNull() // 노선 ORD 값 (Int)
                        val currentBoardingStatus = currentRealtimeData?.boarding ?: 1
                        // RealtimeDTO의 startName이 갱신된 최신 정류장 이름이어야 함
                        val currentStationNameFromDTO = currentRealtimeData?.startName

                        var dynamicMessage = ""
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

                        when (currentTransitType) {
                            2 -> { // 버스 로직
                                val busCurrentStationText: String

                                if (currentBoardingStatus == 1) { // 탑승 전
                                    // 탑승 전이면 transferStations의 첫 번째 정류장 이름 사용
                                    busCurrentStationText = transferStations?.firstOrNull()
                                        ?: currentStationNameFromDTO // DTO 이름 폴백
                                                ?: "알 수 없는 정류장 (탑승 전)"
                                    Log.d("BusRealtime", "탑승 전: 현재 정류장은 경로 시작점: $busCurrentStationText")
                                } else { // 탑승 중 (currentBoardingStatus == 2)
                                    // `locationAsInt` (현재 ORD 값)와 `startStationInfo` (경로 시작 ORD 값)의 차이를 계산하여
                                    // `transferStations` 리스트의 인덱스로 활용
                                    val indexOffset = if (startStationInfo != null && locationAsInt != null) {
                                        locationAsInt - startStationInfo
                                    } else {
                                        null
                                    }

                                    busCurrentStationText = if (indexOffset != null && indexOffset >= 0 && indexOffset < (transferStations?.size ?: 0)) {
                                        // 계산된 인덱스를 사용하여 transferStations에서 정류장 이름 추출
                                        transferStations?.getOrNull(indexOffset) ?: "알 수 없는 정류장 (ORD ${locationAsInt})"
                                    } else {
                                        // 인덱스 계산이 유효하지 않거나, transferStations가 null인 경우
                                        currentStationNameFromDTO ?: "알 수 없는 정류장 (ORD ${locationAsInt})"
                                    }
                                    Log.d("BusRealtime", "탑승 중: location ORD $locationAsInt, 계산된 인덱스 $indexOffset -> $busCurrentStationText")
                                }

                                dynamicMessage = "$typeText, $boardingText. 현재 ${busCurrentStationText}. "
                                if (predict1 != "데이터 없음" && predict1 != null) {
                                    dynamicMessage += "첫차 ${predict1}. "
                                }
                                if (predict2 != "데이터 없음" && predict2 != null) {
                                    dynamicMessage += "둘째차 ${predict2}."
                                }
                                if ((predict1 == "데이터 없음" || predict1 == null) && (predict2 == "데이터 없음" || predict2 == null)) {
                                    dynamicMessage += "도착 예정 정보 없음."
                                }
                            }
                            1 -> { // 지하철 로직 (이전 버전에서 수정된 부분 유지)
                                val subwayCurrentStationText = if (currentStationNameFromDTO != null && currentStationNameFromDTO.isNotEmpty()) {
                                    "$currentStationNameFromDTO 역"
                                } else if (location != "알 수 없음" && location.isNotEmpty()) {
                                    // 지하철 location이 ORD 인덱스이고 transferStations가 이름 리스트라면
                                    // location을 인덱스로 활용 (DBUsage 0 로직과 유사)
                                    val locationIndex = location.toIntOrNull()
                                    if (locationIndex != null && locationIndex >= 0 && locationIndex < (transferStations?.size ?: 0)) {
                                        transferStations?.getOrNull(locationIndex) + " 역"
                                    } else {
                                        "$location 역" // 매칭되지 않으면 ORD 값 그대로 표시
                                    }
                                } else {
                                    "알 수 없는 역"
                                }

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

                        if (order < messagelist.size) {
                            messagelist[order] = dynamicMessage
                        } else {
                            while (messagelist.size <= order) {
                                messagelist.add("경로 정보 없음")
                            }
                            messagelist[order] = dynamicMessage
                        }
                        transInfoImgSwitcher.contentDescription = messagelist[order]
                        Log.d("TalkBack", "Updated messagelist[$order]: ${messagelist[order]}")
                        Log.d("TalkBack", "Updated contentDescription: ${transInfoImgSwitcher.contentDescription}")
                    }

                    // --- RealtimeDTO 업데이트 로직 ---
                    when (currentTransitType) {
                        2 -> { // 버스 로직
                            val receivedVehId = response.vehId
                            val receivedLocation = response.location // 서버에서 받은 ORD 값 (String)
                            val locationInt = receivedLocation?.toIntOrNull() ?: 0 // ORD 값 (Int)

                            var currentBoarding = currentRealtimeData?.boarding ?: 1

                            // 현재 위치(ORD)가 시작 정류장(ORD)과 같으면 탑승 중으로 변경
                            if (locationInt == startStationInfo) {
                                currentBoarding = 2
                                Log.d("BusRealtime", "버스 위치가 시작 지점과 일치, boarding을 2(탑승 중)으로 변경")
                            }


                            val currentStationIdForBus = route.stationInfo?.getOrNull(locationInt) ?: 0 // ORD 값을 인덱스로 사용! 검토 필요

                            // RealtimeDTO의 startName을 현재 위치의 이름으로 업데이트
                            val updatedStartNameForBus = if (currentBoarding == 1) {
                                transferStations?.firstOrNull() ?: ""
                            } else {
                                // 탑승 중일 때 location (ORD)에 해당하는 정류장 이름을 찾아서 startName으로 업데이트
                                val indexOffset = if (startStationInfo != null && locationInt != null) {
                                    locationInt - startStationInfo
                                } else {
                                    null
                                }
                                if (indexOffset != null && indexOffset >= 0 && indexOffset < (transferStations?.size ?: 0)) {
                                    transferStations?.getOrNull(indexOffset) ?: ""
                                } else {
                                    currentRealtimeData?.startName ?: "" // 매핑 실패 시 기존 이름 유지
                                }
                            }

                            updatedRealtimeDTO = RealtimeDTO(
                                type = 2,
                                boarding = currentBoarding,
                                transportLocalID = transportLocalID,
                                stationId = currentStationIdForBus, // Int 타입 (주의: ORD를 인덱스로 쓰는 것이 맞는지 확인 필요)
                                vehid = receivedVehId,
                                startOrd = (startStationInfo ?: 0),
                                endOrd = (endStationInfo ?: 0),
                                location = receivedLocation, // 받은 ORD 값 그대로 유지
                                startName = updatedStartNameForBus // DTO의 startName 업데이트
                            )

                            if (response.predictTimes1 == "데이터 없음" && response.predictTimes2 == "데이터 없음") {
                                runOnUiThread {
                                    Toast.makeText(this@TransportInformationActivity,
                                        "현재 버스 노선은 도착 예정 시간을 지원하지 않습니다.",
                                        Toast.LENGTH_LONG).show()
                                    RealtimeProcessor.stopPolling()
                                }
                            }

                            // 버스 최종 목적지에 도착했을 때 다이얼로그 표시 (전체 경로 마지막 단계 확인 추가)
                            if (locationInt == (endStationInfo ?: -1) && isLastStepOfOverallRoute) {
                                Log.d("BusRealtime", "버스 최종 목적지에 도착했습니다. 경로 종료.")
                                runOnUiThread {
                                    Toast.makeText(this@TransportInformationActivity, "{목적지에 도착했습니다}!", Toast.LENGTH_LONG).show()
                                    RealtimeProcessor.stopPolling()
                                    transInfoImgSwitcher.setImageResource(R.drawable.complete_btt)
                                    transInfoImgSwitcher.contentDescription = "도착!"
                                    //transSavedDialogShow() // 다이얼로그 호출
                                }
                                return@realtimeResponse
                            }
                        }
                        1 -> { // 지하철 로직
                            val receivedPredictTimes1 = response.predictTimes1
                            val receivedTrainNo = response.trainNo
                            val receivedLocation = response.location

                            var currentBoarding = currentRealtimeData?.boarding ?: 1
                            val currentDBUsage = getDBUsage(transportLocalID)

                            val previousTrainNo: String = currentRealtimeData?.trainNo ?: "0"
                            var finalTrainNo: String = previousTrainNo

                            if (!receivedTrainNo.equals("0")) {
                                finalTrainNo = receivedTrainNo
                                Log.d("SubwayRealtime", "새로운 trainNo ${finalTrainNo} 수신 및 반영.")
                            }
                            Log.d("SubwayRealtime", "현재 transportLocalID: $transportLocalID, 결정된 dbUsage: $currentDBUsage")

                            // transferStations는 List<String>이므로, 인덱스 기반으로 접근
                            var extractedStartName = currentRealtimeData?.startName ?: (transferStations?.getOrNull(0) ?: "")
                            var extractedSecondName = currentRealtimeData?.secondName ?: (transferStations?.getOrNull(1) ?: "")
                            val extractedEndName = currentRealtimeData?.endName ?: (transferStations?.lastOrNull() ?: "")

                            var actualLocationForComparison: String? = null
                            if (currentDBUsage == 1) {
                                actualLocationForComparison = receivedLocation
                            } else { // currentDBUsage == 0
                                val locationIndex = receivedLocation?.toIntOrNull()
                                if (locationIndex != null && locationIndex >= 0 && locationIndex < (transferStations?.size ?: 0)) {
                                    actualLocationForComparison = transferStations?.getOrNull(locationIndex)
                                    Log.d("SubwayRealtime", "DBUsage 0: Converted location index $receivedLocation to station name: $actualLocationForComparison")
                                } else {
                                    actualLocationForComparison = receivedLocation
                                    Log.w("SubwayRealtime", "DBUsage 0: Invalid location index received or not an index. Using raw string: $receivedLocation")
                                }
                            }

                            // DBUsage가 1일 때 로직 (기존과 동일)
                            if (currentBoarding == 1 && receivedPredictTimes1 == "0분 후") {
                                Log.d("SubwayRealtime", "DBUsage 1, 탑승 전 (1), '0분 후' 도착. 탑승 여부 확인 다이얼로그 표시.")
                                runOnUiThread {
                                    val dialog = Dialog(this@TransportInformationActivity)
                                    dialog.setContentView(R.layout.transportation_arrival_dialog) // 커스텀 레이아웃 설정
                                    dialog.setCancelable(false) // 외부 탭 또는 뒤로가기 버튼으로 닫히지 않도록 방지

                                    // 커스텀 다이얼로그의 뷰 참조 가져오기
                                    val titleTextView = dialog.findViewById<TextView>(R.id.dialog_title)
                                    val messageTextView = dialog.findViewById<TextView>(R.id.dialog_message)
                                    val btnYesBoarded = dialog.findViewById<Button>(R.id.btn_yes_boarded)
                                    val btnNoBoarded = dialog.findViewById<Button>(R.id.btn_no_boarded)

                                    // *** 다이얼로그를 중앙에 띄우기 위한 코드 시작 ***
                                    val layoutParams = WindowManager.LayoutParams().apply {
                                        copyFrom(dialog.window?.attributes)
                                        gravity = Gravity.CENTER // 다이얼로그를 중앙에 위치
                                        width = WindowManager.LayoutParams.WRAP_CONTENT // 내용에 맞게 너비 조절
                                        height = WindowManager.LayoutParams.WRAP_CONTENT // 내용에 맞게 높이 조절
                                    }


                                    // 주변 투명하게 보이게 함
                                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                    dialog.window?.attributes = layoutParams
                                    // *** 다이얼로그를 중앙에 띄우기 위한 코드 끝 ***

                                    // 동적 메시지 설정
                                    messageTextView.text = "현재 ${extractedStartName}역에 열차가 도착했습니다. 탑승하셨나요?"

                                    btnYesBoarded.setOnClickListener {
                                        val newBoarding = 2
                                        Log.d("SubwayRealtime", "사용자 탑승 확인 (0분 후, DBUsage 1). boarding을 2(탑승 중)으로 변경.")
                                        updatedRealtimeDTO = RealtimeDTO(
                                            type = 1, boarding = newBoarding, transportLocalID = transportLocalID,
                                            dbUsage = currentDBUsage, trainNo = finalTrainNo,
                                            startName = extractedStartName, secondName = extractedSecondName,
                                            endName = extractedEndName, direction = (trainDirection ?: ""),
                                            location = receivedLocation
                                        )
                                        updatedRealtimeDTO?.let { RealtimeProcessor.requestUpdate(it) }
                                        dialog.dismiss() // 커스텀 다이얼로그 닫기
                                    }

                                    btnNoBoarded.setOnClickListener {
                                        Log.d("SubwayRealtime", "사용자 탑승 취소 (0분 후, DBUsage 1). boarding 1(탑승 전) 유지.")
                                        updatedRealtimeDTO = RealtimeDTO(
                                            type = 1, boarding = currentBoarding, transportLocalID = transportLocalID,
                                            dbUsage = currentDBUsage, trainNo = finalTrainNo,
                                            startName = extractedStartName, secondName = extractedSecondName,
                                            endName = extractedEndName, direction = (trainDirection ?: ""),
                                            location = receivedLocation
                                        )
                                        updatedRealtimeDTO?.let { RealtimeProcessor.requestUpdate(it) }
                                        dialog.dismiss() // 커스텀 다이얼로그 닫기
                                    }

                                    dialog.show() // 커스텀 다이얼로그 표시
                                }
                                return@realtimeResponse
                            } else if (currentBoarding == 2 && receivedPredictTimes1 == "0분 후") {
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
                                        dbUsage = currentDBUsage, trainNo = finalTrainNo,
                                        startName = nextStationName, secondName = nextNextStationName,
                                        endName = extractedEndName, direction = (trainDirection ?: ""),
                                        location = receivedLocation
                                    )
                                } else if (extractedStartName == extractedEndName && isLastStepOfOverallRoute) {
                                    Log.d("SubwayRealtime", "DBUsage 1, 최종 목적지에 도착했습니다. 경로 종료.")
                                    runOnUiThread {
                                        Toast.makeText(this@TransportInformationActivity,
                                            "목적지에 도착했습니다!",
                                            Toast.LENGTH_LONG).show()
                                        RealtimeProcessor.stopPolling()
                                        transInfoImgSwitcher.setImageResource(R.drawable.complete_btt)
                                        transInfoImgSwitcher.contentDescription = "도착!"
                                        //transSavedDialogShow() // 다이얼로그 호출
                                    }
                                    return@realtimeResponse
                                }
                                updatedRealtimeDTO?.let { RealtimeProcessor.requestUpdate(it) }
                                return@realtimeResponse
                            }
                            // --- DBUsage가 0일 때: location 비교가 우선적으로 중요 ---
                            else {
                                if (currentBoarding == 1 && actualLocationForComparison == extractedStartName) {
                                    Log.d("SubwayRealtime", "DBUsage 0, location(${receivedLocation})이 출발역(${extractedStartName})과 일치. 탑승 여부 확인 다이얼로그 표시.")
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
                                        // 주변 투명하게 보이게 함
                                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                                        // 커스텀 다이얼로그의 뷰 참조 가져오기
                                        val messageTextView = dialog.findViewById<TextView>(R.id.dialog_message)
                                        val btnYesBoarded = dialog.findViewById<Button>(R.id.btn_yes_boarded)
                                        val btnNoBoarded = dialog.findViewById<Button>(R.id.btn_no_boarded)

                                        // 동적 메시지 설정 (기존 AlertDialog의 setMessage에 해당)
                                        messageTextView.text = "현재 ${extractedStartName}역에 열차가 도착했습니다. 지하철에 탑승하셨나요?"

                                        btnYesBoarded.setOnClickListener {
                                            val newBoarding = 2
                                            Log.d("SubwayRealtime", "사용자 탑승 확인 (DBUsage 0). boarding을 2(탑승 중)으로 변경.")
                                            updatedRealtimeDTO = RealtimeDTO(
                                                type = 1,
                                                boarding = newBoarding,
                                                transportLocalID = transportLocalID,
                                                dbUsage = currentDBUsage,
                                                trainNo = finalTrainNo,
                                                startName = extractedStartName,
                                                secondName = extractedSecondName,
                                                endName = extractedEndName,
                                                direction = (trainDirection ?: ""),
                                                location = receivedLocation
                                            )
                                            updatedRealtimeDTO?.let { RealtimeProcessor.requestUpdate(it) }
                                            dialog.dismiss() // 커스텀 다이얼로그 닫기
                                        }

                                        btnNoBoarded.setOnClickListener {
                                            val newBoarding = 1
                                            val newTrainNo = "0" // 추가된 로직
                                            Log.d("SubwayRealtime", "사용자 탑승 취소 (DBUsage 0). boarding 1(탑승 전) 유지, trainNo ${newTrainNo}으로 초기화.")
                                            updatedRealtimeDTO = RealtimeDTO(
                                                type = 1,
                                                boarding = newBoarding,
                                                transportLocalID = transportLocalID,
                                                dbUsage = currentDBUsage,
                                                trainNo = newTrainNo, // 추가된 로직 적용
                                                startName = extractedStartName,
                                                secondName = extractedSecondName,
                                                endName = extractedEndName,
                                                direction = (trainDirection ?: ""),
                                                location = receivedLocation
                                            )
                                            updatedRealtimeDTO?.let { RealtimeProcessor.requestUpdate(it) }
                                            dialog.dismiss() // 커스텀 다이얼로그 닫기
                                        }

                                        dialog.show() // 커스텀 다이얼로그 표시
                                    }
                                    return@realtimeResponse
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
                                            dbUsage = currentDBUsage,
                                            trainNo = finalTrainNo,
                                            startName = nextStationName,
                                            secondName = nextNextStationName,
                                            endName = extractedEndName,
                                            direction = (trainDirection ?: ""),
                                            location = receivedLocation
                                        )
                                    } else if (extractedStartName == extractedEndName && isLastStepOfOverallRoute) {
                                        Log.d("SubwayRealtime", "DBUsage 0, 최종 목적지에 도착했습니다. 경로 종료.")
                                        runOnUiThread {
                                            Toast.makeText(this@TransportInformationActivity,
                                                "목적지에 도착했습니다!",
                                                Toast.LENGTH_LONG).show()
                                            RealtimeProcessor.stopPolling()
                                            transInfoImgSwitcher.setImageResource(R.drawable.complete_btt)
                                            transInfoImgSwitcher.contentDescription = "도착!"
                                            transSavedDialogShow() // 다이얼로그 호출
                                        }
                                        return@realtimeResponse
                                    }
                                    updatedRealtimeDTO?.let { RealtimeProcessor.requestUpdate(it) }
                                    return@realtimeResponse
                                }
                            }
                            updatedRealtimeDTO = RealtimeDTO(
                                type = 1,
                                boarding = currentBoarding,
                                transportLocalID = transportLocalID,
                                dbUsage = currentDBUsage,
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

                val initialRealtimeDTO: RealtimeDTO? = when (currentTransitType) {
                    2 -> { // 버스 초기 DTO
                        // 초기 boarding은 1 (탑승 전)이므로, transferStations의 첫 번째 이름을 startName으로 사용
                        val initialStartNameForBus = transferStations?.firstOrNull() ?: ""
                        RealtimeDTO(
                            type = 2, boarding = 1, transportLocalID = transportLocalID,
                            stationId = (route.stationInfo?.first() ?: 0), // route.stationInfo는 List<Int>로 가정하고 첫 번째 ID 사용
                            vehid = null,
                            startOrd = (startStationInfo ?: 0),
                            endOrd = (endStationInfo ?: 0),
                            location = "0", // 초기 location은 0으로 설정
                            startName = initialStartNameForBus // 초기 startName 설정
                        )
                    }
                    1 -> { // 지하철 초기 DTO
                        val initialDBUsage = getDBUsage(transportLocalID)
                        Log.d("SubwayRealtime", "초기 RealtimeDTO - transportLocalID: $transportLocalID, initialDBUsage: $initialDBUsage")
                        RealtimeDTO(
                            type = 1, boarding = 1, transportLocalID = transportLocalID,
                            dbUsage = initialDBUsage, trainNo = "0",
                            startName = transferStations?.getOrNull(0) ?: "",
                            secondName = transferStations?.getOrNull(1) ?: "",
                            endName = transferStations?.lastOrNull() ?: "",
                            direction = (trainDirection ?: ""),
                            location = "null"
                        )
                    }
                    else -> null
                }

                initialRealtimeDTO?.let {
                    RealtimeProcessor.stopPolling()
                    RealtimeProcessor.startPolling(
                        endpoint = "/api/realTime",
                        initialRealtimeData = it,
                        onResponse = onRealtimeResponse,
                        onError = { errorMessage, errorCode ->
                            runOnUiThread {
                                if (errorCode == 500) {
                                    Toast.makeText(this@TransportInformationActivity,
                                        "현재 노선은 실시간 정보 조회를 지원하지 않습니다.",
                                        Toast.LENGTH_LONG).show()
                                    RealtimeProcessor.stopPolling()
                                } else {
                                    Toast.makeText(this@TransportInformationActivity,
                                        "실시간 정보 조회 중 오류 발생: $errorMessage",
                                        Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                } ?: run {
                    RealtimeProcessor.stopPolling()
                }
            } ?: run {
                RealtimeProcessor.stopPolling()
                Log.e("RouteInfo", "currentRouteId is null for order $order. Stopping polling.")
            }
        }
    }

    // 기존 transSavedDialogShow 함수는 변경 없이 유지됩니다.



    /**
     * 현재 위치를 가져오는 더미 함수. 실제 구현에서는 FusedLocationProviderClient를 사용하여 위치를 요청해야 합니다.
     * `getLastLocation`에서 실제 위치를 가져오므로 이 함수는 사용되지 않을 것으로 보입니다.
     */
    private fun getCurrentLocation(): Location {
        // 실제로는 FusedLocationProviderClient.getCurrentLocation 또는 LocationRequest 등을 사용하여 위치를 가져와야 합니다.
        // 현재는 더미 Location 객체를 반환합니다.
        return Location("provider")
    }

    /* dialog 관련 function */

    /**
     * 경로 저장 여부를 묻는 다이얼로그를 표시합니다.
     */
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
        }
    }


    /**
     * 저장할 경로의 별명을 입력받는 다이얼로그를 표시합니다.
     */
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
                        addressNickNameEditText.text.toString() // 별명
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

    /**
     * ImageSwitcher를 초기화하고 기본 이미지를 설정합니다.
     */
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


    /**
     * (현재 사용되지 않는) ImageSwitcher의 이미지를 다음으로 변경하는 함수입니다.
     * `transInfoImgArray`가 더미 데이터로 보이며, `updateCurrentTransportationInfo`에서 직접 이미지를 설정합니다.
     */
    private fun whatIsNext(index:Int = 0) {
        if(index >= transInfoImgArray.size) {
            transInfoImgSwitcher.setImageResource(transInfoImgArray[0])
        }else{
            transInfoImgSwitcher.setImageResource(transInfoImgArray[index])
        }
        return
    }


    /**
     * 마지막으로 알려진 위치를 가져오거나, 가져올 수 없을 경우 위치 업데이트를 요청합니다.
     */
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


    /**
     * 위치 업데이트를 요청하는 함수입니다. 현재는 구현이 미완성입니다.
     * 주기적인 위치 업데이트가 필요하다면 이 함수를 완성해야 합니다.
     */
    private fun requestLocationUpdates() {
        Log.w("Location", "requestLocationUpdates() not fully implemented. Implement LocationRequest and LocationCallback for continuous updates.")
        // TODO: LocationRequest 및 LocationCallback을 사용하여 주기적인 위치 업데이트 로직 구현
    }
}