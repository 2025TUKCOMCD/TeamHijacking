package com.example.front.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.front.databinding.SettingTermOfUseDialogBinding

class SettingTermOfUseDialog : DialogFragment() {
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
