package com.example.front.transportation.DetailRouteFragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.front.R

class SubwayFragment : Fragment() {
    private var navigation: BusFragment.FragmentNavigation? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BusFragment.FragmentNavigation) {
            navigation = context
        } else {
            throw RuntimeException("$context must implement FragmentNavigation")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_subway, container, false)

        // 전달받은 데이터 표시
        val subwayLine = arguments?.getString("subwayLine") ?: ""
        val subwayStation = arguments?.getString("subwayStation") ?: ""

        view.findViewById<TextView>(R.id.subwayLineTextView).text = "노선 번호: $subwayLine"
        view.findViewById<TextView>(R.id.subwayStationTextView).text = "역: $subwayStation"

        // 다음 Fragment로 이동하는 버튼 이벤트 처리
        view.findViewById<Button>(R.id.nextButton).setOnClickListener {
            navigation?.showNextFragment()
        }

        return view
    }

    override fun onDetach() {
        super.onDetach()
        navigation = null
    }
}