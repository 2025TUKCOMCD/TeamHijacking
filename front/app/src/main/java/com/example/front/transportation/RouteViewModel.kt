package com.example.front.transportation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.front.transportation.data.searchPath.Route
import com.example.front.transportation.processor.RouteProcessor

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RouteViewModel : ViewModel() {
    private val _routeData = MutableLiveData<List<Route>>()
    val routeData: LiveData<List<Route>> get() = _routeData

    fun fetchRoute(startLat: Double, startLng: Double,endLat: Double, endLng: Double ) {
        viewModelScope.launch {
            val result = RouteProcessor.fetchRoute(startLat, startLng, endLat, endLng )
            _routeData.value = result ?: emptyList()
        }
    }
}