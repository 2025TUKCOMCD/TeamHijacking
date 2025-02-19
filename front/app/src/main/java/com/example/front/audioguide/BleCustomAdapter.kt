package com.example.front.audioguide

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.front.R
import android.bluetooth.BluetoothDevice

class BleCustomAdapter(context: Context, deviceList: List<BluetoothDevice>) : ArrayAdapter<BluetoothDevice>(context, 0, deviceList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        }

        val device = getItem(position)
        val deviceName = view?.findViewById<TextView>(R.id.ble_name)
        deviceName?.text = device?.name

        val deviceAddress = view?.findViewById<TextView>(R.id.ble_address)
        deviceAddress?.text = device?.address

        return view!!
    }
}