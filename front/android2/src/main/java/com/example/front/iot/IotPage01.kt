package com.example.front.iot

import android.content.Context
import android.content.Intent
import android.net.Uri
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