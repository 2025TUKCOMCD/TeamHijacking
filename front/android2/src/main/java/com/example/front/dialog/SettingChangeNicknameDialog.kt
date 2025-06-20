package com.example.front.dialog

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.front.databinding.SettingChangenickDialogBinding
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit

class SettingChangeNicknameDialog: DialogFragment() {
    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    private lateinit var binding: SettingChangenickDialogBinding
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingChangenickDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefs = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)

        initNickname()
        setupListeners()
    }

    private fun initNickname() {
        val prevNickText = sharedPrefs.getString("name", null)
        binding.changeNicknameEditText.setText(prevNickText ?: "")
    }

    private fun setupListeners() {
        binding.settingChangeDismissBtn.setOnClickListener {
            Log.d("dialog", "닉네임 변경 취소")
            dismiss()
        }

        binding.settingChangeConfirmBtn.setOnClickListener {
            val newNick = binding.changeNicknameEditText.text.toString()

            if (newNick.isEmpty()) {
                Toast.makeText(requireContext(), "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sharedPrefs.edit {
                putString("name", newNick)
            }

            Log.d("dialog", "새 닉네임: $newNick")
            Toast.makeText(activity, "새 닉네임: $newNick",Toast.LENGTH_SHORT).show()

            //닉네임 변경을 여러번 할 수 없도록 제한을 두어도 괜찮겠음
            dismiss()
        }
    }
}
