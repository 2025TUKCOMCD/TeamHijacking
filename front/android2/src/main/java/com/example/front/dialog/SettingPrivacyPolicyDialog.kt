package com.example.front.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.front.databinding.SettingPrivacyPolicyDialogBinding

class SettingPrivacyPolicyDialog: DialogFragment() {
    private lateinit var binding: SettingPrivacyPolicyDialogBinding

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
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
        binding = SettingPrivacyPolicyDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingPrivacyPolicyDismissBtn.setOnClickListener {
            Log.d("dialog", "개인 정보 정책 다이얼로그 삭제")
            dismiss()
        }
    }
}