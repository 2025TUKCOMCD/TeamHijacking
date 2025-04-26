package com.example.front.transportation.DetailRouteFragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import com.example.front.R
import com.example.front.transportation.FragmentNavigation


class WalkFragment : Fragment() {

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
        val view = inflater.inflate(R.layout.fragment_walk, container, false)

        val walkDistance = arguments?.getString("walkDistance") ?: ""
        val walkTime = arguments?.getString("walkTime") ?: ""

        view.findViewById<TextView>(R.id.walkDistanceTextView).text = "거리: $walkDistance"
        view.findViewById<TextView>(R.id.walkTimeTextView).text = "소요 시간: $walkTime"

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