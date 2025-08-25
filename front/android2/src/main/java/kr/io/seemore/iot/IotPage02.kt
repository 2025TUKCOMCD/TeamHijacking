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

class IotPage02 : Fragment() {

    private var _binding: FragmentIotPage02Binding? = null
    private val binding get() = _binding!!
    private val tag = "IoT_page02"
    private lateinit var telToSubwayBtn: Button

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
        telToSubwayBtn = binding.telToSubwayBtn

        // 초기 상태: 데이터가 수신되기 전에는 버튼을 숨깁니다.
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
        var phoneNumber: String = "00099990000"
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }

    fun selectCallNum() {
        // 이 함수는 현재 비어있지만, 추후 전화번호 선택 로직을 구현할 수 있습니다.
    }
}