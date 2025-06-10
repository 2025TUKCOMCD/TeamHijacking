package com.example.front.iot

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.front.R
import com.example.front.databinding.IotDeviceLittleViewBinding

class IotDeviceLittleView : LinearLayout {
    private val binding: IotDeviceLittleViewBinding by lazy {
        IotDeviceLittleViewBinding.bind(
            LayoutInflater.from(context).inflate(R.layout.iot_device_little_view, this, false)
        )
    }

    constructor(context: Context) : super(context) {
        initView()
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()   //이 경우 속성 지정도 해준다캄
    }

    private fun initView() {
        addView(binding.root)
    }


}