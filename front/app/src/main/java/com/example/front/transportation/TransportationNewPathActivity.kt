package com.example.front.transportation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.front.R
import com.example.front.databinding.ActivityTransportationNewPathBinding
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class TransportationNewPathActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportationNewPathBinding

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100
        private const val GEOCODING_API_KEY = "AIzaSyDXdv60obb-54dlxvbSO0QgJM8WYpRyjqs" // 실제 API 키로 변경
        private const val TAG = "TransportationNewPathActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportationNewPathBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val addressTextView: TextView = binding.addressTextView
        val latitudeTextView: TextView = binding.latitudeTextView
        val longitudeTextView: TextView = binding.longitudeTextView

        val address = "서울특별시 강남구 강남대로 323"
        addressTextView.text = "Address: $address"

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocationFromAddress(address, latitudeTextView, longitudeTextView)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    private fun getLocationFromAddress(address: String, latitudeTextView: TextView, longitudeTextView: TextView) {
        Log.d(TAG, "getLocationFromAddress: Starting geocoding for address $address")

        val client = OkHttpClient()
        val url = "https://maps.googleapis.com/maps/api/geocode/json?address=${address}&key=$GEOCODING_API_KEY"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e(TAG, "Geocoding API call failed", e)
                runOnUiThread {
                    latitudeTextView.text = "Latitude: Error"
                    longitudeTextView.text = "Longitude: Error"
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "Geocoding API response: $responseBody")

                    val json = JsonParser.parseString(responseBody) as JsonObject
                    val results = json["results"].asJsonArray

                    if (results.size() > 0) {
                        val location = results[0].asJsonObject["geometry"].asJsonObject["location"].asJsonObject
                        val latitude = location["lat"].asDouble
                        val longitude = location["lng"].asDouble

                        runOnUiThread {
                            latitudeTextView.text = "Latitude: $latitude"
                            longitudeTextView.text = "Longitude: $longitude"
                        }
                    } else {
                        Log.e(TAG, "No results found for the specified address.")
                        runOnUiThread {
                            latitudeTextView.text = "Latitude: Not found"
                            longitudeTextView.text = "Longitude: Not found"
                        }
                    }
                } else {
                    Log.e(TAG, "Geocoding API response was not successful")
                    runOnUiThread {
                        latitudeTextView.text = "Latitude: Error"
                        longitudeTextView.text = "Longitude: Error"
                    }
                }
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val address = "서울특별시 강남구 강남대로 323"
            val latitudeTextView: TextView = binding.latitudeTextView
            val longitudeTextView: TextView = binding.longitudeTextView
            getLocationFromAddress(address, latitudeTextView, longitudeTextView)
        }
    }
}
