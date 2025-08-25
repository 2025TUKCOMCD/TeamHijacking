package kr.io.seemore.iot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kr.io.seemore.R
import kr.io.seemore.databinding.FragmentIotPage02Binding
import android.net.Uri
import android.widget.ImageButton

class IotPage02 : Fragment() {

    private var _binding: FragmentIotPage02Binding? = null
    private val binding get() = _binding!!
    private val tag = "IoT_page02"
    private lateinit var telToSubwayBtn: ImageButton

    // 데이터 수신을 위한 BroadcastReceiver
    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                // 수정: Int 배열 데이터를 받도록 변경
                val receivedPathTypes: ArrayList<Int>? = it.getIntegerArrayListExtra("pathTransitType")
                Log.d(tag, "Received path transit types: $receivedPathTypes")

                // pathTransitType 배열에 1이 포함되어 있는지 확인
                val isConditionMet = receivedPathTypes?.contains(1) == true

                if (isConditionMet) {
                    telToSubwayBtn.visibility = View.VISIBLE
                    Log.d(tag, "pathTransitType에 1이 포함되어 있어 버튼을 보이게 합니다.")
                } else {
                    telToSubwayBtn.visibility = View.GONE
                    Log.d(tag, "pathTransitType에 1이 없어 버튼을 숨깁니다.")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 브로드캐스트 리시버 등록
        // 이 액션은 PhoneMessageListenerService에서 보낸 액션과 동일해야 합니다.
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            dataReceiver, IntentFilter("kr.io.seemore.ACTION_ALL_TRANS_DATA")
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIotPage02Binding.inflate(inflater, container, false)
        telToSubwayBtn = binding.subwayPhone
        telToSubwayBtn.visibility = View.GONE

        telToSubwayBtn.setOnClickListener {
            callToSubway()
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        // Fragment가 소멸될 때 브로드캐스트 리시버 해제
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(dataReceiver)
        _binding = null
    }

    //------------custom func 전화번호 선택 함수
    fun callToSubway() {
        var phoneNumber: String = selectCallNum() //selectCallNum
        //위치 정보 받아왔다면 여기에서 체크::
        //혹은 위치에 따라 다른 func 하도록 구현

        //전화하기
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }

    fun selectCallNum(lineName:Int = 0, stationName:String = "기본" ): String {
        val line1Seoul: Array<String> = arrayOf("서울역","시청","종각","종로3가","종로5가","동대문","동묘앞","신설동","제기동","청량리")
        val line3Seoul: Array<String> = arrayOf("지축","구파발","연신내","불광","녹번","홍제","무악재","독립문","경복궁","안국","종로3가","을지로3가","충무로","동대입구","약수","금호","옥수","압구정","신사","잠원","고속터미널","교대","남부터미널","양재","매봉","도곡","대치","학여울","대청","일원","수서","가락시장","경찰병원","오금")
        val line4Seoul: Array<String> = arrayOf("당고개","상계","노원","창동","쌍문","수유","미아","미아삼거리","길음","성신여대입구","돈암","혜화","동대문","동대문운동장","충무로","명동","회현","남대문시장","서울역","숙대입구","삼각지","신용산","이촌","동작","총신대입구","이수","사당","남태령")
        //당고개 남태령
        var resultStr:String = ""

        resultStr = when {
            (lineName >= 5 && lineName <= 8) || lineName == 2 -> "15771234"
            lineName == 9 -> "0226560009"
            /*역 번호가 다를 때에*/
            //코레일과 서울교통공사가 함께 공유하는
            lineName == 1 && (stationName in line1Seoul) -> "15771234"
            lineName == 3 && (stationName in line3Seoul) -> "15771234"
            lineName == 4 && (stationName in line4Seoul) -> "15771234"
            else -> "15447788"
        }

        return resultStr
    }

    //-------------전화 text func 관련 이 곳에 기입 예정
}