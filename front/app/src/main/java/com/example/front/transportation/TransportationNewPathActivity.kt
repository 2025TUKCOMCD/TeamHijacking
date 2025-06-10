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

        // 출발지, 도착지 문자열을 저장할 변수 추가
        var startAddressString: String = ""
        var endAddressString: String = ""

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
        val addressConfirmBtn: Button = binding.AddressConfirmBtn

        // 오디오 권한 요청 (onCreate에서 한 번만 호출)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_RECORD_AUDIO)
        }

        // 초기 주소 설정 및 위치 fetching
        setupInitialLocations()

        // 시작 지점 STT 함수
        addressStartEditText.setOnClickListener {
            startSTT(REQUEST_CODE_SPEECH_INPUT_START)
        }
        // 도착 지점 STT 함수
        addressEndEditText.setOnClickListener {
            startSTT(REQUEST_CODE_SPEECH_INPUT_END)
        }

        addressConfirmBtn.setOnClickListener {
            val intent = Intent(this, TransportNewPathSearchActivity::class.java)
            intent.putExtra("startLat", revs_latitude)
            intent.putExtra("startLng", revs_longitude)
            intent.putExtra("endLat", reve_latitude)
            intent.putExtra("endLng", reve_longitude)
            // 출발지/도착지 문자열도 Intent에 추가하여 넘깁니다.
            intent.putExtra("departureName", startAddressString)
            intent.putExtra("destinationName", endAddressString)
            startActivity(intent)
        }
    }

    // 초기 주소 설정 및 위치 정보를 가져오는 함수
    private fun setupInitialLocations() {
        val saddress = "장기역" // 초기 시작 주소
        val eaddress = "운양역" // 초기 도착 주소

        binding.addressStartTextView.text = saddress
        // 콜백에서 주소 문자열도 함께 받도록 수정
        fetchLocation(saddress) { address, latitude, longitude ->
            startAddressString = address // 문자열 저장
            revs_latitude = latitude
            revs_longitude = longitude
            Log.d(TAG, "Start Location - Address: $address, Latitude: $revs_latitude, Longitude: $revs_longitude")
        }

        binding.addressEndTextView.text = eaddress
        // 콜백에서 주소 문자열도 함께 받도록 수정
        fetchLocation(eaddress) { address, latitude, longitude ->
            endAddressString = address // 문자열 저장
            reve_latitude = latitude
            reve_longitude = longitude
            Log.d(TAG, "End Location - Address: $address, Latitude: $reve_latitude, Longitude: $reve_longitude")
        }
    }


    private fun startSTT(requestCode: Int) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREA)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, if (requestCode == REQUEST_CODE_SPEECH_INPUT_START) "시작 주소를 말해주세요..." else "도착 주소를 말해주세요...")
        try {
            startActivityForResult(intent, requestCode)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "STT Not Supported: " + e.message)
            // 사용자에게 STT를 지원하지 않는다는 메시지를 보여주는 것이 좋습니다.
        }
    }

    // 위도 경도 가져오기 (음성 인식 결과 처리)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_CODE_SPEECH_INPUT_START || requestCode == REQUEST_CODE_SPEECH_INPUT_END) && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (result != null && result.isNotEmpty()) {
                val address = result[0]
                if (requestCode == REQUEST_CODE_SPEECH_INPUT_START) {
                    binding.addressStartTextView.text = address
                    // 콜백에서 주소 문자열도 함께 받도록 수정
                    fetchLocation(address) { returnedAddress, latitude, longitude ->
                        startAddressString = returnedAddress // 문자열 저장
                        revs_latitude = latitude
                        revs_longitude = longitude
                        Log.d(TAG, "Start Location - Address: $returnedAddress, Latitude: $revs_latitude, Longitude: $revs_longitude")
                    }
                } else { // REQUEST_CODE_SPEECH_INPUT_END
                    binding.addressEndTextView.text = address
                    // 콜백에서 주소 문자열도 함께 받도록 수정
                    fetchLocation(address) { returnedAddress, latitude, longitude ->
                        endAddressString = returnedAddress // 문자열 저장
                        reve_latitude = latitude
                        reve_longitude = longitude
                        Log.d(TAG, "End Location - Address: $returnedAddress, Latitude: $reve_latitude, Longitude: $reve_longitude")
                    }
                }
            } else {
                Log.w(TAG, "STT result is empty.")
            }
        }
    }

    // fetchLocation 함수 콜백 시그니처 변경: 주소 문자열도 함께 전달하도록 수정
    private fun fetchLocation(address: String, callback: (String, Double, Double) -> Unit) {
        Thread {
            val location = getLocationFromAddress(address)
            if (location != null) {
                runOnUiThread {
                    // 콜백에 주소 문자열, 위도, 경도를 모두 전달
                    callback(address, location.first, location.second)
                }
            } else {
                Log.e(TAG, "Failed to get location for address: $address")
                // 사용자에게 위치 정보를 가져오지 못했다는 피드백을 줄 수 있습니다.
            }
        }.start()
    }

    private fun getLocationFromAddress(address: String): Pair<Double, Double>? {
        val client = OkHttpClient()
        val encodedAddress = java.net.URLEncoder.encode(address, "UTF-8")
        val url = "https://maps.googleapis.com/maps/api/geocode/json?address=${encodedAddress}&key=$GEOCODING_API_KEY"
        val request = Request.Builder().url(url).build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val json = JsonParser.parseString(responseBody) as JsonObject
                    val results = json["results"].asJsonArray
                    if (results != null && results.size() > 0) {
                        val location = results[0].asJsonObject["geometry"].asJsonObject["location"].asJsonObject
                        val latitude = location["lat"].asDouble
                        val longitude = location["lng"].asDouble
                        Pair(latitude, longitude)
                    } else {
                        Log.e(TAG, "No results found for the specified address: $address")
                        null
                    }
                } else {
                    Log.e(TAG, "Geocoding API response body is null.")
                    null
                }
            } else {
                Log.e(TAG, "Geocoding API response was not successful: ${response.code} - ${response.message}")
                null
            }
        } catch (e: IOException) {
            Log.e(TAG, "Geocoding API call failed (IOException) for address: $address", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Geocoding API call failed (General Exception) for address: $address", e)
            null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "ACCESS_FINE_LOCATION permission granted.")
                setupInitialLocations()
            } else {
                Log.w(TAG, "ACCESS_FINE_LOCATION permission denied.")
            }
        } else if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "RECORD_AUDIO permission granted.")
            } else {
                Log.w(TAG, "RECORD_AUDIO permission denied.")
            }
        }
    }
}