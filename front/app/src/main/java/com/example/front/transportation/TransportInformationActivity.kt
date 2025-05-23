package com.example.front.transportation

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

import android.widget.Button
import android.widget.EditText
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.front.R
import com.example.front.databinding.ActivityTransportInformationBinding

import com.example.front.databinding.TransSavedConfirmDialogBinding
import com.example.front.databinding.TransSavedDialogBinding
import com.example.front.transportation.data.searchPath.RouteId

//교통 안내용 Activity
class TransportInformationActivity : AppCompatActivity() {

    private lateinit var binding : ActivityTransportInformationBinding
    //imageSwitcher에 사용할 imageView 배열 선언
    private val transInfoImgArray = intArrayOf(R.drawable.default_btt, R.drawable.train_btt, R.drawable.human_btt,
        R.drawable.bus_btt, R.drawable.complete_btt)
    private var imgIndex = 0
    private lateinit var transInfoImgSwitcher: ImageSwitcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!hasGps()) {
            Log.d(TAG, "This hardware doesn't have GPS.")
            // Fall back to functionality that doesn't use location or
            // warn the user that location function isn't available.
        }

        //바인딩
        val imsiBtt2: Button = binding.imsiBtt2
        val imsiBtt3: Button = binding.imsiBtt3
        transInfoImgSwitcher = binding.transInfoImgSwitcher

        //imageSwitcher에 imageView 설정하여 이미지 표시
        initTransImgSwitcher()

        val pathTransitType = intent.getIntegerArrayListExtra("pathTransitType")
        // Log.d("log", "pathTransitType: $pathTransitType")
        val transitTypeNo = intent.getStringArrayListExtra("transitTypeNo")
        // Log.d("log", "transitTypeNo: $transitTypeNo") //
        val routeIds = intent.getSerializableExtra("routeIds") as? ArrayList<RouteId>
        Log.d("log", "routeIds: $routeIds")
        updateButtonImages(pathTransitType)
        // 주석처리된 임시 버튼
        /*
        imsiBtt2.setOnClickListener {
            transSavedDialogShow()
        }

        imsiBtt3.setOnClickListener {
            whatIsNext(++imgIndex)
        }
        */

        //updateButtonImages(pathTransitType)

    }
    private fun hasGps(): Boolean =
        packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

    private fun updateButtonImages(pathTransitType: List<Int>?) {
        if (pathTransitType == null) return

        pathTransitType.forEachIndexed { index, type ->
            val imageResource = when (type) {
                1 -> R.drawable.train_btt
                2 -> R.drawable.bus_btt
                3 -> R.drawable.human_btt
                else -> R.drawable.default_btt // 기본 이미지
            }
            // 배열 범위를 벗어날 경우를 대비한 방어 코드
            if (index < transInfoImgArray.size) {
                transInfoImgArray[index] = imageResource
            }
        }

        // 첫 번째 이미지를 표시하도록 설정
        imgIndex = 0
        transInfoImgSwitcher.setImageResource(transInfoImgArray[imgIndex])
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

        //Alertdialog.Builder를 통한 다이얼로그 생성
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

    //다이얼로그의 yes 버튼 클릭 시 동작되어야 할 코드.
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

        //Alertdialog.Builder를 통한 다이얼로그 생성
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
            //텍스트 입력되었는지 확인하기
            if(!TextUtils.isEmpty(addressNickNameEditText.getText())){
                //만약 textLine 내에 text가 입력되었다면
                //그 text를 외부로 전달, 정확히는.. 경로의 값을 데이터베이스에 save하도록 구현 필요
                Log.d("log","텍스트 전달됨. ${addressNickNameEditText.getText()}")
                dialog2.dismiss()
            }else{
                //만약 비어있다면, "별명을 빈 칸으로 지정할 수 없습니다" 출력
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
        transInfoImgSwitcher.setFactory({val imgView = ImageView(applicationContext)
            imgView.scaleType = ImageView.ScaleType.FIT_CENTER
            //imgView.setPadding(2, 2, 2, 2)
            imgView
        })
        //imageSwitcher에 imageView 설정
        transInfoImgSwitcher.setImageResource(transInfoImgArray[imgIndex])
    }

    /* 교통 안내 변경 시, 버스, 지하철, ... 에 따라 사진 바뀌도록 구현 */
    private fun whatIsNext(index:Int = 0) {
        //만약 배열의 크기보다 크다면 0으로 바꾼다.
        if(index>=transInfoImgArray.size) {
            imgIndex = 0
            transInfoImgSwitcher.setImageResource(transInfoImgArray[0])
        }else{
            transInfoImgSwitcher.setImageResource(transInfoImgArray[index])
        }

        return;
    }
}
