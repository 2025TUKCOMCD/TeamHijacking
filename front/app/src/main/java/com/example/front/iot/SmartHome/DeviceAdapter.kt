package com.example.front.iot.SmartHome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.front.R
import com.example.front.iot.SmartHome.Device

class DeviceAdapter(
    private val devices: List<Device>,
    private val onCommandClick: (Device, String) -> Unit // 기기 및 명령 전달
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    // 1. ViewHolder 클래스: RecyclerView 항목의 UI 요소를 참조
    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceLabel: TextView = view.findViewById(R.id.deviceLabel)
        val btnTurnOn: Button = view.findViewById(R.id.btnTurnOn)
        val btnTurnOff: Button = view.findViewById(R.id.btnTurnOff)
    }

    // 2. onCreateViewHolder: 항목의 레이아웃을 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_item, parent, false) // device_item.xml 사용
        return DeviceViewHolder(view)
    }

    // 3. onBindViewHolder: 데이터를 UI에 바인딩
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]

        holder.deviceLabel.text = device.label // 기기 이름 표시

        // 버튼 클릭 이벤트 처리
        holder.btnTurnOn.setOnClickListener { onCommandClick(device, "on") }
        holder.btnTurnOff.setOnClickListener { onCommandClick(device, "off") }
    }

    // 4. getItemCount: 항목의 개수를 반환
    override fun getItemCount() = devices.size
}
