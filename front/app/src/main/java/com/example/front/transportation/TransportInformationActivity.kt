package com.example.front.transportation

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
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

//교통 안내용 Activity
class TransportInformationActivity : AppCompatActivity() {

    private lateinit var binding : ActivityTransportInformationBinding
    //imageSwitcher 에 사용할 imageView 배열 선언
    private var transInfoImgArray = intArrayOf(1,2,3,4,5,6)
    private lateinit var transInfoImgSwitcher: ImageSwitcher
    private var transOrder = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var curLat: Double = 0.0
    private var curLon: Double = 0.0

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                getLastLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                getLastLocation() // 필요에 따라 처리
            } else -> {
            // No location access granted.
            Log.e("Location", "Location permission denied.")
        }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //위치 코드
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

        // 권한이 이미 있는 경우 바로 위치 정보 가져오기
        getLastLocation()

        if (!hasGps()) {
            Log.d("GPS", "This hardware doesn't have GPS.")
            // Fall back to functionality that doesn't use location or
            // warn the user that location function isn't available.
        }
        else{
            Log.d("GPS", "This hardware has GPS.")
        }
        print(hasGps())

        //바인딩
        val imsiBtt2: Button = binding.imsiBtt2
        val imsiBtt3: Button = binding.imsiBtt3
        transInfoImgSwitcher = binding.transInfoImgSwitcher

        //imageSwitcher 에 imageView 설정 하여 이미지 표시
        initTransImgSwitcher()

        //val pathTransitType = intent.getIntegerArrayListExtra("pathTransitType")
        val pathTransitType = mutableListOf(3,1,3,2,3)
        // Log.d("log", "pathTransitType: $pathTransitType")
        //val transitTypeNo = intent.getStringArrayListExtra("transitTypeNo")
        val transitTypeNo = intent.getStringArrayListExtra("transitTypeNo")
        // Log.d("log", "transitTypeNo: $transitTypeNo") //
        //val routeIds = intent.getSerializableExtra("routeIds") as? ArrayList<RouteId>
        val routeIds = intArrayOf(3,2,3,1,3)
        // 더미 데이터 실제 데이터로 바꿔야함
        val messagelist = listOf("[10]번째 전역 (곡산) n분후 도착","목표역에 거의 다 접근함" ,"[10]번째 전역 (곡산)", "목표역에 거의 다 접근함" , "도착지까지 n m 남음")
        // 이미지를 클릭시 소리가 나게 세팅하고 싶음


        Log.d("log", "routeIds: $routeIds")
        pathTransitType.add(4)
        Log.d("현빈", pathTransitType.toString())
        updateButtonImages(transOrder,pathTransitType, messagelist)
        // 주석 처리된 임시 버튼
        imsiBtt2.setOnClickListener {
            if(transOrder!=0){
                updateButtonImages(--transOrder,pathTransitType, messagelist)
            }
            else{
                Toast.makeText(this, "첫번째 경로입니다.", Toast.LENGTH_SHORT).show()
            }

        }


        imsiBtt3.setOnClickListener {
            if(transOrder+1 < pathTransitType.size) {
                updateButtonImages(++transOrder, pathTransitType, messagelist)
            }
            else{
                Toast.makeText(this, "마지막 경로입니다.", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun hasGps(): Boolean =
        packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

    private fun updateButtonImages(order: Int,pathTransitType: List<Int>?, messagelist: List<String> = listOf()) {
        if (pathTransitType == null) return

        val imageResource = when (pathTransitType[order]) {
            1 -> R.drawable.train_btt
            2 -> R.drawable.bus_btt
            3 -> R.drawable.human_btt
            4 -> R.drawable.complete_btt
            else -> R.drawable.default_btt // 기본 이미지
        }
        Log.d("현빈", imageResource.toString())
        transInfoImgArray[pathTransitType[order]] = imageResource

        transInfoImgSwitcher.setImageResource(transInfoImgArray[pathTransitType[order]])
        //업데이트 할때 이미지 contentDescription도 같이 해서
        transInfoImgSwitcher.contentDescription = messagelist[transOrder]
    }


    private fun getCurrentLocation(): Location {
        // Implement logic to get the current location
        // This is a placeholder implementation
        return Location("provider")
    }

    /* dialog 관련 function */
    private fun transSavedDialogShow(){
        // 뷰 바인딩 확인
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

        //Alertdialog.Builder 를 통한 다이얼로그 생성
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(transSavedDialogBinding.root)
            .create()

        //dialog 동작 후 크기 조정
        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 화면 너비의 90%
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        //버튼 관련 변수 설정
        val yesSavedBtt: TextView = transSavedDialogBinding.YesSavedBtt
        val noSavedBtt: TextView = transSavedDialogBinding.NoSavedBtt

        //버튼 관련 설정 진행
        yesSavedBtt.setOnClickListener{
            //Yes 클릭 시 동작
            transSavedNicknameDialogShow()
            dialog.dismiss()
            //원하는 추가 작업
        }

        noSavedBtt.setOnClickListener{
            dialog.dismiss()
        }
    }

    //다이얼로그 의 yes 버튼 클릭 시 동작 되어야 할 코드.
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

        //Alertdialog.Builder 를 통한 다이얼로그 생성
        val dialog2 = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(transSavedConfirmDialogBinding.root)
            .create()

        //다이얼로그 동작 후 크기 조정
        dialog2.show()
        dialog2.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 화면 너비의 90%
            LinearLayout.LayoutParams.WRAP_CONTENT)

        //요소 바인딩
        val transSavedConfirmBtt: TextView = transSavedConfirmDialogBinding.transSavedConfirmBtt
        val cancelBtt: TextView = transSavedConfirmDialogBinding.cancelBtt
        val addressNickNameEditText: EditText = transSavedConfirmDialogBinding.addressNickNameEditText
        val savedConfirmTextView: TextView = transSavedConfirmDialogBinding.savedConfirmTextView

        transSavedConfirmBtt.setOnClickListener{
            //텍스트 입력 확인
            if(!TextUtils.isEmpty(addressNickNameEditText.getText())){
                //만약 textLine 내에 text 가 입력 되었다면
                //그 text 를 외부로 전달,  경로의 값을 데이터베이스 에 save 하도록 구현 필요
                Log.d("log","텍스트 전달됨. ${addressNickNameEditText.getText()}")
                dialog2.dismiss()
            }else{
                //만약 비어 있다면, "별명을 빈 칸으로 지정할 수 없습니다" 출력
                savedConfirmTextView.text = "별명을 빈 칸으로 지정할 수 없습니다."
            }
        }

        //취소 버튼
        cancelBtt.setOnClickListener {
            dialog2.dismiss();
        }
    }

    //imgSwitcher 초기화 function
    private fun initTransImgSwitcher(){
        Log.d("현빈", "함수입성")
        transInfoImgSwitcher.setFactory({val imgView = ImageView(applicationContext)
            imgView.scaleType = ImageView.ScaleType.FIT_CENTER
            Log.d("현빈", "버그1")
            //imgView.setPadding(2, 2, 2, 2)
            imgView
        })
        Log.d("현빈", "버그2")
        //imageSwitcher 에 imageView 설정
        transInfoImgSwitcher.setImageResource(R.drawable.default_btt)
        Log.d("현빈", "버그3")
    }

    /* 교통 안내 변경 시, 버스, 지하철, ... 에 따라 사진 바뀌도록 구현 */
    private fun whatIsNext(index:Int = 0) {
        //만약 배열의 크기 보다 크다면 0으로 바꾼다.
        if(index>=transInfoImgArray.size) {
            transInfoImgSwitcher.setImageResource(transInfoImgArray[0])
        }else{
            transInfoImgSwitcher.setImageResource(transInfoImgArray[index])
        }

        return;
    }
    private fun getLastLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        curLat = it.latitude
                        curLon = it.longitude
                        Log.d("현빈", "Latitude: $curLat, Longitude: $curLon")
                        // 얻은 위도, 경도 사용
                    } ?: run {
                        Log.w("현빈", "Last known location was null, try requesting location updates.")
                        requestLocationUpdates() // 마지막 위치가 없는 경우 업데이트 요청
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Location", "Failed to get last location: ${e.message}")
                }
        } catch (securityException: SecurityException) {
            Log.e("Location", "Security exception while getting last location.")
        }
    }

    private fun requestLocationUpdates() {
        // 위치 업데이트 요청 설정 (원하는 정확도, 빈도 등)
        // ...

        // fusedLocationClient.requestLocationUpdates(...) // 구현 필요
        Log.w("Location", "requestLocationUpdates() not fully implemented.")
    }
}
