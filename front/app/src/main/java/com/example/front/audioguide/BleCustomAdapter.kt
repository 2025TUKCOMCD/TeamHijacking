package com.example.front.audioguide

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.front.R // 프로젝트의 R 파일 경로에 맞게 수정

class BleCustomAdapter(context: Context, private val devices: ArrayList<BluetoothDevice>) :
    ArrayAdapter<BluetoothDevice>(context, 0, devices) {

    // 아이템 뷰 타입 정의 (옵션 1: getItemViewType을 사용하는 경우)
    private val TYPE_AGH = 0
    private val TYPE_BGH = 1

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // 현재 기기 정보 가져오기
        val device = getItem(position) // getItem(position)이 null을 반환하지 않음을 확신할 경우 !! 사용 가능
        // device.name이 null일 경우 "Unknown Device"로 대체하여 사용
        val deviceName = device?.name ?: "Unknown Device"

        // Log.d("현빈", deviceName) // 디버깅용 로그

        val viewType = if (deviceName.startsWith("G")) TYPE_AGH else TYPE_BGH
        var currentView = convertView

        // 현재 뷰 타입과 재활용될 뷰 타입이 다르면 새로 생성 (중요!)
        // getTag()를 사용하여 뷰 타입을 저장하고 비교하는 방식도 사용 가능
        if (currentView == null || (currentView.tag as? Int) != viewType) {
            currentView = if (viewType == TYPE_AGH) {
                LayoutInflater.from(context).inflate(R.layout.agh_list_item, parent, false)
            } else { // TYPE_BGH
                LayoutInflater.from(context).inflate(R.layout.bgh_list_item, parent, false)
            }
            // 새로 생성된 뷰에 뷰 타입을 태그로 저장 (재활용 시 비교를 위해)
            currentView.tag = viewType
        }


        // 뷰 컴포넌트 찾기
        // 주의: 각 레이아웃(agh_list_item, bgh_list_item)에 동일한 ID의 TextView가 있어야 합니다.
        val deviceNameTextView = currentView?.findViewById<TextView>(R.id.ble_name) // 예시: 주소 표시용 TextView

        // TextView에 텍스트 설정
        deviceNameTextView?.text = deviceName

        return currentView!!
    }

    override fun getItemViewType(position: Int): Int {
        val device = getItem(position)
        val deviceName = device?.name ?: "Unknown Device"
        return if (deviceName.startsWith("G")) TYPE_AGH else TYPE_BGH
    }

    override fun getViewTypeCount(): Int {
        return 2 // AGH 타입과 BGH 타입, 총 2가지 뷰 타입
    }
}