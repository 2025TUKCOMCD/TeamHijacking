package com.example.front.iot.SmartHome

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.front.R

class DeviceAdapter(
    private val devices: List<Device>,
    private val onDeviceClick: (Device) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() { // ViewHolder 타입을 RecyclerView.ViewHolder로 변경

    // 무드등
    inner class LightViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceLabel: TextView = view.findViewById(R.id.deviceLabel)
        init {
            view.setOnClickListener {
                onDeviceClick(devices[adapterPosition])
            }
        }
    }

    inner class AiSpeakerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceLabel: TextView = view.findViewById(R.id.deviceLabel) // R.id.deviceLabel을 각 레이아웃에 맞게 변경
        init {
            view.setOnClickListener {
                onDeviceClick(devices[adapterPosition])
            }
        }
    }

    // ... 필요한 다른 장치 유형에 대한 ViewHolder 추가

    override fun getItemViewType(position: Int): Int {
        return when (devices[position].name) {
            "Galaxy Home Mini (3NPH)" -> VIEW_TYPE_AISPEAKER
            "Hejhome Smart Mood Light" -> VIEW_TYPE_LIGHT
            // ... 다른 장치 유형에 대한 View Type 반환
            else -> {
                Log.d("현빈", "그런 기기 없음")}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LIGHT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.device_itemlist_light, parent, false)
                LightViewHolder(view)
            }
            VIEW_TYPE_AISPEAKER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.device_itemlist_aispeak, parent, false)
                AiSpeakerViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val device = devices[position]
        when (holder.itemViewType) {
            VIEW_TYPE_LIGHT -> {
                (holder as LightViewHolder).apply {
                    deviceLabel.text = device.label
                    // Light 관련 데이터 바인딩
                }
            }
            VIEW_TYPE_AISPEAKER -> {
                (holder as AiSpeakerViewHolder).apply {
                    deviceLabel.text = device.label
                    // Temperature 관련 데이터 바인딩 (예: device.currentTemperature 또는 유사한 속성)
                    // temperatureValue.text = "${device.currentTemperature}°C"
                }
            }
        }
    }

    override fun getItemCount(): Int = devices.size

    companion object {
        private const val VIEW_TYPE_LIGHT = 0
        private const val VIEW_TYPE_AISPEAKER = 1
        // ... 필요한 다른 View Type 상수 추가
    }
}