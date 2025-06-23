package com.example.front.iot

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

class IotPage01 : Fragment() {

    //binding
    private var _binding: FragmentIotPage01Binding? = null
    private  val binding get() = _binding!!
    private val TAG = "Iot_page01"
//    private lateinit var deviceControlHelper: DeviceControlHelper  여기에 제어 기능을 넣을 생각은 없기에 배제
    private val deviceList = mutableListOf<Device>()

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




}