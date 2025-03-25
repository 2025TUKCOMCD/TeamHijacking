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


class WalkFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_walk, container, false)

        // 전달받은 데이터 표시
        val walkDistance = arguments?.getString("walkDistance") ?: ""
        val walkTime = arguments?.getString("walkTime") ?: ""

        view.findViewById<TextView>(R.id.walkDistanceTextView).text = "거리: $walkDistance"
        view.findViewById<TextView>(R.id.walkTimeTextView).text = "소요 시간: $walkTime"

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