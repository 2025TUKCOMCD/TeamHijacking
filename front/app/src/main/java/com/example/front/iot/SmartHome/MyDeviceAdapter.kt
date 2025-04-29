package com.example.front.iot.SmartHome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.front.R

class MyDeviceAdapter(
    private val devices: List<Device>,
    private val onDeviceClick: (Device) -> Unit
) : RecyclerView.Adapter<MyDeviceAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceLabel: TextView = view.findViewById(R.id.deviceLabel)

        init {
            view.setOnClickListener {
                onDeviceClick(devices[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.deviceLabel.text = devices[position].label
    }

    override fun getItemCount(): Int = devices.size
}
