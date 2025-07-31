package kr.io.seemore.iot

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import kr.io.seemore.R
import kr.io.seemore.databinding.IotDeviceLittleViewBinding

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