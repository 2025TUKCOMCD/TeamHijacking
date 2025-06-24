package com.example.front.iot

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.front.R
import com.example.front.databinding.FragmentIotPage01Binding
import com.example.front.iot.smartHome.Device
import com.example.front.login.processor.RetrofitClient
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.front.iot.smartHome.DeviceResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IotPage01 : Fragment() {

    //binding
    private var _binding: FragmentIotPage01Binding? = null
    private  val binding get() = _binding!!
    private val tag = "Iot_page01"
    private val deviceList = mutableListOf<Device>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIotPage01Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchDeviceListFromSmartThings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*     ----   func area   ----      */

    private fun fetchDeviceListFromSmartThings() {
        // 토큰을 SharedPreferences에서 가져옴
        val sharedPref = requireContext().getSharedPreferences("smartThingsPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("smartThingsToken", null)

        if(token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "API 토큰을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            Log.e("iot","API 토큰 찾을 수 없음")
            return
        }

        Log.d("iot", "토큰 잘 받오고 있나요? ${token}")
        val apiToken = "Bearer $token"
        val apiService = RetrofitClient.apiService

        apiService.getDevices(apiToken).enqueue(object : Callback<DeviceResponse> {
            override fun onResponse(call: Call<DeviceResponse>, response: Response<DeviceResponse>) {
                if(response.isSuccessful) {
                    val devices = response.body()?.items ?: emptyList()
                    deviceList.clear()
                    deviceList.addAll(devices)

                    //동적 뷰 추가
                    deviceList.forEach {
                        device -> addIoTDeviceView(device)
                    }
                } else {
                    Log.e(tag, "응답 실패. 코드: ${response.code()}, 메시지: ${response.message()}")
                    Log.e(tag, "에러 바디: ${response.errorBody()?.string()}")
                    Log.e(tag, "디바이스 불러오기 실패: ${response.code()}")
                    Toast.makeText(requireContext(), "기기 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeviceResponse>, t: Throwable) {
                Log.e(tag, "네트워크 오류: ${t.message}")
                Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addIoTDeviceView(device: Device) {
        val inflater = LayoutInflater.from(requireContext())
        val newView = inflater.inflate(R.layout.iot_device_little_view, null)

        val nameTextView = newView.findViewById<TextView>(R.id.iotNameText)
        val descTextView = newView.findViewById<TextView>(R.id.iotDeviceDescription)
        val imageView = newView.findViewById<ImageView>(R.id.iotDeviceImageView)

        nameTextView.text = device.label
        descTextView.text = device.name

        //이미지 설정은 상황에 맞게
        imageView.setImageResource(R.drawable.iot)

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            val marginPx = (16 * resources.displayMetrics.density).toInt()
            bottomMargin = marginPx
        }

        newView.layoutParams = params
        binding.iotLinearLayout.addView(newView)
    }

}