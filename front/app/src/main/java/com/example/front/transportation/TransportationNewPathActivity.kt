package com.example.front.transportation

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.front.BuildConfig
import com.example.front.databinding.ActivityTransportationNewPathBinding
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.Locale

class TransportationNewPathActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportationNewPathBinding

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100
        private const val PERMISSION_REQUEST_RECORD_AUDIO = 101
        private const val REQUEST_CODE_SPEECH_INPUT_START = 102
        private const val REQUEST_CODE_SPEECH_INPUT_END = 103
        var revs_latitude: Double = 0.0
        var revs_longitude: Double = 0.0
        var reve_latitude: Double = 0.0
        var reve_longitude: Double = 0.0
        private val GEOCODING_API_KEY = BuildConfig.Geolocation_APIKEY // 실제 API 키로 변경
        private const val TAG = "TransportationNewPathActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportationNewPathBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 사용할 객체들 선언
        val addressStartEditText: TextView = binding.addressStartTextView
        val addressEndEditText: TextView = binding.addressEndTextView
        val AddressConfirmBtn: Button = binding.AddressConfirmBtn
        // 오디오 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_RECORD_AUDIO)
        }
        // 권한이 있는지 확인 (여기서는 위치 권한 확인) //일단 이곳에서 먼저 주소를 넣어둠(나중에 삭제 예정)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val saddress = "서울특별시 강남구 강남대로 323"
            addressStartEditText.text = saddress
            fetchLocation(saddress) { latitude, longitude ->
                revs_latitude = latitude
                revs_longitude = longitude
                Log.d(TAG, "Start Location - Latitude: $revs_latitude, Longitude: $revs_longitude")
            }
            val eaddress = "아차산로 65길 85"
            addressEndEditText.text = eaddress
            fetchLocation(eaddress) { latitude, longitude ->
                reve_latitude = latitude
                reve_longitude = longitude
                Log.d(TAG, "End Location - Latitude: $reve_latitude, Longitude: $reve_longitude")
            }
        } else { // 만약 권한이 없으면 권한 요청
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
        }
        // 시작 지점 STT 함수
        addressStartEditText.setOnClickListener {
            startSTT(REQUEST_CODE_SPEECH_INPUT_START)
        }
        // 도착 지점 STT 함수
        addressEndEditText.setOnClickListener {
            startSTT(REQUEST_CODE_SPEECH_INPUT_END)
        }
        AddressConfirmBtn.setOnClickListener{
            val intent = Intent(this, TransportNewPathSearchActivity::class.java)
            intent.putExtra("startLat", revs_latitude)
            intent.putExtra("startLng", revs_longitude)
            intent.putExtra("EndLat", reve_latitude)
            intent.putExtra("EndLng", reve_longitude)
            startActivity(intent)
        }
    }

    private fun startSTT(requestCode: Int) {
        // 음성 인식을 위한 인텐트
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        // 음성 인식 모델 설정
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        // 음성 인식을 위한 기본 언어: 한국어
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREA)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, if (requestCode == REQUEST_CODE_SPEECH_INPUT_START) "Speak the start address..." else "Speak the end address...")
        try {
            startActivityForResult(intent, requestCode)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    // STT 이후 오게 되는 함수. 만약 REQUESTCODE == REQUEST_CODE_INPUT_START라면 STARTADRESS를 바꿔주고 아니면 END ADDRESS를 바꿔준다
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_CODE_SPEECH_INPUT_START || requestCode == REQUEST_CODE_SPEECH_INPUT_END) && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (result != null && result.isNotEmpty()) {
                val address = result[0]
                if (requestCode == REQUEST_CODE_SPEECH_INPUT_START) {
                    binding.addressStartTextView.text = address
                    fetchLocation(address) { latitude, longitude ->
                        revs_latitude = latitude
                        revs_longitude = longitude
                        Log.d(TAG, "Start Location - Latitude: $revs_latitude, Longitude: $revs_longitude")
                    }
                } else {
                    binding.addressEndTextView.text = address
                    fetchLocation(address) { latitude, longitude ->
                        reve_latitude = latitude
                        reve_longitude = longitude
                        Log.d(TAG, "End Location - Latitude: $reve_latitude, Longitude: $reve_longitude")
                    }
                }
            }
        }
    }

    private fun fetchLocation(address: String, callback: (Double, Double) -> Unit) {
        Thread {
            val location = getLocationFromAddress(address)
            if (location != null) {
                runOnUiThread {
                    callback(location.first, location.second)
                }
            } else {
                Log.e(TAG, "Failed to get location for address: $address")
            }
        }.start()
    }

    private fun getLocationFromAddress(address: String): Pair<Double, Double>? {
        val client = OkHttpClient()
        val url = "https://maps.googleapis.com/maps/api/geocode/json?address=${address}&key=$GEOCODING_API_KEY"
        val request = Request.Builder().url(url).build()

        return try {
            val response = client.newCall(request).execute() // 동기식 네트워크 요청
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val json = JsonParser.parseString(responseBody) as JsonObject
                val results = json["results"].asJsonArray
                if (results.size() > 0) {
                    val location = results[0].asJsonObject["geometry"].asJsonObject["location"].asJsonObject
                    val latitude = location["lat"].asDouble
                    val longitude = location["lng"].asDouble
                    Pair(latitude, longitude)
                } else {
                    Log.e(TAG, "No results found for the specified address.")
                    null
                }
            } else {
                Log.e(TAG, "Geocoding API response was not successful")
                null
            }
        } catch (e: IOException) {
            Log.e(TAG, "Geocoding API call failed", e)
            null
        }
    }

    // Default 함수. 일단 실행 먼저 됨. API와의 통신을 위함
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val address = "서울특별시 강남구 강남대로 323"
            fetchLocation(address) { latitude, longitude ->
                revs_latitude = latitude
                revs_longitude = longitude
                Log.d(TAG, "Permission Granted - Latitude: $revs_latitude, Longitude: $revs_longitude")
            }
        }
    }
}
