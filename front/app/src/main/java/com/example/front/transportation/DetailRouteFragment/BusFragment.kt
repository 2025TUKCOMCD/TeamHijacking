package com.example.front.transportation.DetailRouteFragment

import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import com.example.front.R
import com.example.front.transportation.FragmentNavigation

class BusFragment : Fragment() {

    private var navigation: FragmentNavigation? = null
    private lateinit var locationManager: LocationManager

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
        val locationBtn =view.findViewById<AppCompatImageButton>(R.id.locationButton)
        view.findViewById<TextView>(R.id.busNumberTextView).text = "버스 번호: $busNumber"
        view.findViewById<TextView>(R.id.busStationTextView).text = "버스 정류장: $busStation"
        Log.d("현빈", "일단 테스트123")
        locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
        Log.d("현빈", "데이터 들어감")
        locationBtn.setOnClickListener {
            Log.d("현빈", "클릭함")
            getLocation()
        }

        val nextButton = view.findViewById<AppCompatImageButton>(R.id.nextButton)
        nextButton.setOnClickListener {
            navigation?.showNextFragment()
        }

        return view
    }

    override fun onDetach() {
        super.onDetach()
        navigation = null
    }

    private fun getLocation() {
        Log.d("현빈", "함수 입성")
        checkPermissions(requireActivity())
        val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        Log.d("현빈", "설정 완료")
        Log.d("현빈", location.toString())
        val latitude = location?.latitude
        val longitude = location?.longitude
        val accuracy = location?.accuracy
        val time = location?.time
        Log.d("현빈", latitude.toString())
        Log.d("현빈", longitude.toString())
        Log.d("현빈", accuracy.toString())
        Log.d("현빈", time.toString())
        location?.let {
            val latitude = it.latitude
            val longitude = it.longitude
            val accuracy = it.accuracy
            val time = it.time
            Log.d("현빈", latitude.toString())
            Log.d("현빈", longitude.toString())
            Log.d("현빈", accuracy.toString())
            Log.d("현빈", time.toString())
        }
    }
}