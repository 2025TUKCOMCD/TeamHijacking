package com.example.front.iot.SmartHome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.front.R

class DeviceAdapter(
    private val deviceItems: List<DeviceItem>, // DeviceItem 리스트로 변경
    private val onCommandClick: (Device, String) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceLabel: TextView = view.findViewById(R.id.deviceLabel)
        val btnTurnOn: Button = view.findViewById(R.id.btnTurnOn)
        val btnTurnOff: Button = view.findViewById(R.id.btnTurnOff)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_item, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val deviceItem = deviceItems[position]
        val device = deviceItem.device

        holder.deviceLabel.text = "${device.label} (${if (deviceItem.isOnline) "Online" else "Offline"})"

        holder.btnTurnOn.setOnClickListener { onCommandClick(device, "on") }
        holder.btnTurnOff.setOnClickListener { onCommandClick(device, "off") }

        // 선택된 기기 강조 표시
        holder.itemView.isSelected = deviceItem.isSelected
    }

    override fun getItemCount() = deviceItems.size

    // ✅ 기기 목록 반환 메서드 추가
    fun getDeviceList(): List<Device> {
        return deviceItems.map { it.device }
    }
}
