package com.example.front.iot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.front.R
import com.example.front.databinding.FragmentIotPage03Binding

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
        return inflater.inflate(R.layout.fragment_iot_page03, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}