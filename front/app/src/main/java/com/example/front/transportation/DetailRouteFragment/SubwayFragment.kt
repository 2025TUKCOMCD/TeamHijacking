package com.example.front.transportation.DetailRouteFragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import com.example.front.R
import com.example.front.transportation.FragmentNavigation

// TODO: Rename parameter arguments, choose names that match
class SubwayFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_subway, container, false)

        val subwayLine = arguments?.getString("subwayLine") ?: ""
        val subwayStation = arguments?.getString("subwayStation") ?: ""

        view.findViewById<TextView>(R.id.subwayLineTextView).text = "지하철 노선: $subwayLine"
        view.findViewById<TextView>(R.id.subwayStationTextView).text = "지하철 역: $subwayStation"

        val nextButton = view.findViewById<AppCompatImageButton>(R.id.nextButton) // AppCompatImageButton  캐스팅
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