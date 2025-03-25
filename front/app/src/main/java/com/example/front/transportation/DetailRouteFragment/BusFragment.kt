package com.example.front.transportation.DetailRouteFragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import com.example.front.R
import com.example.front.transportation.FragmentNavigation

class BusFragment : Fragment() {

    private var navigation: FragmentNavigation? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentNavigation) {
            navigation = context
        } else {
            throw RuntimeException("$context must implement FragmentNavigation")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bus, container, false)

        val busNumber = arguments?.getString("busNumber") ?: ""
        val busStation = arguments?.getString("busStation") ?: ""

        view.findViewById<TextView>(R.id.busNumberTextView).text = "버스 번호: $busNumber"
        view.findViewById<TextView>(R.id.busStationTextView).text = "버스 정류장: $busStation"
        Log.d("현빈", "일단 테스트")

        val nextButton = view.findViewById<AppCompatImageButton>(R.id.nextButton) // AppCompatImageButton으로 캐스팅
        nextButton.setOnClickListener {
            navigation?.showNextFragment()
        }

        return view
    }

    override fun onDetach() {
        super.onDetach()
        navigation = null
    }
}