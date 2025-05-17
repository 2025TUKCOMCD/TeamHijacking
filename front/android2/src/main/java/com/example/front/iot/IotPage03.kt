package com.example.front.iot

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.front.databinding.FragmentIotPage03Binding
import com.kakao.sdk.user.UserApiClient
import androidx.core.content.edit

class IotPage03 : Fragment() {

    //binding
    private var _binding: FragmentIotPage03Binding? = null
    private  val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIotPage03Binding.inflate(inflater, container, false)

        val logoutBtn: Button = binding.logoutBtn
        logoutBtn.setOnClickListener {
            //1. 카카오 logout
            UserApiClient.instance.logout { error ->
                if (error != null ) {
                    //logout 실패
                    Log.e("Logout", "뭐라고 하더라 이거")
                } else {
                    //2. SharedPreferences 초기화
                    requireActivity().getSharedPreferences("userPrefs", MODE_PRIVATE).edit() {
                        clear()
                    }

                    //3. 로그인 화면 으로 이동
                    val intent = Intent(requireActivity(), com.example.front.login.LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                    requireActivity().finish()
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}