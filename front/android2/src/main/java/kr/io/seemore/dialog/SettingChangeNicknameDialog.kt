package kr.io.seemore.dialog

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kr.io.seemore.databinding.SettingChangenickDialogBinding
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import kr.io.seemore.login.data.UserRequest
import kr.io.seemore.login.processor.RetrofitClient
import retrofit2.Call
import retrofit2.Response

class SettingChangeNicknameDialog: DialogFragment() {

    private lateinit var binding: SettingChangenickDialogBinding
    private lateinit var sharedPrefs: SharedPreferences

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onCreateView (
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
        val loginId = arguments?.getString("loginId")  //전달받은 loginId 이곳에 삽입

        Log.d("Retrofit", "닉네임 변경 요청 URL = https://seemore.io.kr/users/$loginId/nickname")


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
            val loginId = sharedPrefs.getString("loginId", null)

            if (newNick.isEmpty()) {
                Toast.makeText(requireContext(), "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (loginId == null) {
                Toast.makeText(requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updateMap = mapOf("name" to newNick)

            RetrofitClient.userService.updateNickname(loginId, updateMap)
                .enqueue(object : retrofit2.Callback<UserRequest> {
                    override fun onResponse(
                        call: Call<UserRequest>,
                        response: Response<UserRequest>
                    ) {
                        if (!isAdded) return

                        if (response.isSuccessful) {
                            sharedPrefs.edit {
                                putString("name", newNick)
                            }
                            Toast.makeText(requireContext(), "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                            Log.d("dialog", "닉네임 변경 완료: $newNick")
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), "서버 오류로 닉네임을 변경하지 못했습니다.", Toast.LENGTH_SHORT).show()
                            Log.d("dialog", "요청 보냄: loginId=$loginId, newNick=$newNick")
                            Log.e("dialog", "닉네임 변경 실패: ${response.code()}")

                        }
                    }
                    override fun onFailure(call: Call<UserRequest>, t: Throwable) {
                        if (!isAdded) return
                        Toast.makeText(requireContext(), "닉네임 변경 실패(네트워크 오류)", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
