package kr.io.seemore.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kr.io.seemore.databinding.SettingTermOfUseDialogBinding

class SettingTermOfUseDialog : DialogFragment() {
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

    private lateinit var binding: SettingTermOfUseDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
       savedInstanceState: Bundle?
    ): View? {
        binding = SettingTermOfUseDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//
//        binding.tvSample.text = text
//
//        binding.btnSample.setOnClickListener {
//            Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
//        }
        binding.settingTermOfUseDismissBtn.setOnClickListener {
            Log.d("dialog", "이용 약관 다이얼로그 삭제")
            dismiss()
        }
    }
}
