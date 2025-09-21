package kr.io.seemore.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kr.io.seemore.databinding.SettingContactToDevDialogBinding

class SettingContactToDevDialog: DialogFragment() {
    private lateinit var binding: SettingContactToDevDialogBinding

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 다이얼로그의 너비를 전체 화면에 맞게 조정
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT // 높이는 내용물에 맞춤
        dialog?.window?.setLayout(width, height)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingContactToDevDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingContactToDevDismissBtn.setOnClickListener {
            Log.d("dialog", "개발자 연락 다이얼로그 삭제")
            dismiss()
        }
    }
}