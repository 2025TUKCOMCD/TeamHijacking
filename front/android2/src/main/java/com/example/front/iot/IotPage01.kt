package com.example.front.iot

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.front.R
import com.example.front.databinding.FragmentIotPage01Binding
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.common.api.ApiException

class IotPage01 : Fragment() {

    //binding
    private var _binding: FragmentIotPage01Binding? = null
    private  val binding get() = _binding!!
    private val TAG = "Iot_page01"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIotPage01Binding.inflate(inflater, container, false)

        //임시 버튼
        val addViewBtn = binding.addViewBtn
        val iotScrollView = binding.iotScrollView
        val iotLinearLayout = binding.iotLinearLayout

        //데이터 전송 테스트
        sendData(requireContext(),"아무데이터","갔나?")

        addViewBtn.setOnClickListener {
            addIoTDeviceView("아무 이름")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    /*     ----   func area   ----      */

    private fun addIoTDeviceView(deviceName: String = "기본 이름 삽입") {
        // 1. 레이아웃 inflater 가져오기
        val inflater = LayoutInflater.from(requireContext())
        val newIotView = inflater.inflate(R.layout.iot_device_little_view, null)

        val iotNameTextView : TextView = newIotView.findViewById<TextView>(R.id.iotNameText)
        val iotDeviceDescription : TextView = newIotView.findViewById<TextView>(R.id.iotDeviceDescription)
        val iotImageView : ImageView = newIotView.findViewById<ImageView>(R.id.iotDeviceImageView)

        iotNameTextView.text = deviceName
        //iotDeviceDescription :: 이 곳에 정리


        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            val marginInPx = (20 * resources.displayMetrics.density).toInt()
            bottomMargin = marginInPx
        }
        newIotView.layoutParams = layoutParams          // layoutParams 적용

        binding.iotLinearLayout.addView(newIotView)
    }


    //android 로부터 watch 로 데이터 보내기 위한 테스트 코드
    private fun sendData(context: Context, key: String, value: String) {
        val dataClient = Wearable.getDataClient(context)
        val putDataReq = PutDataMapRequest.create("/my_data").run {
            dataMap.putString(key, value)
            asPutDataRequest()
        }

        val putDataTask: Task<DataItem> = dataClient.putDataItem(putDataReq)

        putDataTask.addOnSuccessListener {
            Log.d(TAG, "데이터 전송 성공: $key = $value")
            Toast.makeText(context, "데이터 전송 성공", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Log.e(TAG, "데이터 전송 실패: ${exception.message}", exception)
            Toast.makeText(context, "데이터 전송 실패: ${exception.message}", Toast.LENGTH_SHORT).show()

            if (exception is ApiException) {
                val apiException = exception
                Log.e(TAG, "API Exception Status Code: ${apiException.statusCode}")
                // 필요에 따라 추가적인 오류 처리 로직 구현 (예: 특정 상태 코드에 따른 다른 UI 표시)
            }
        }
    }

}