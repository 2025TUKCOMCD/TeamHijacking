package com.example.front.transportation

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
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
        private const val GEOCODING_API_KEY = BuildConfig.Geolocation_APIKEY // 실제 API 키로 변경
        private const val TAG = "TransportationNewPathActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportationNewPathBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //사용할 객체들 선언
        val addressStartEditText: TextView = binding.addressStartTextView
        val addressEndEditText: TextView = binding.addressEndTextView

        // 권한이 있는지 확인 (여기서는 위치 권한 확인)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val address = "서울특별시 강남구 강남대로 323"
            addressStartEditText.text=address
            getLocationFromAddress(address)
        } else { // 만약 권한이 없으면 권한 요청
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
        }

        // 오디오 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_RECORD_AUDIO)
        }
        //시작지점 STT함수
        addressStartEditText.setOnClickListener {
            startSTT(REQUEST_CODE_SPEECH_INPUT_START)
        }
        //도착지점 STT함수
        addressEndEditText.setOnClickListener {
            startSTT(REQUEST_CODE_SPEECH_INPUT_END)
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

    //STT이후 오게되는 함수 만약 REQUESTCODE == REQUET_CODE_INPUT_START라면 STARTADRESS를 바꿔주고 아니면 END ADDRESS를 바꿔준다
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_CODE_SPEECH_INPUT_START || requestCode == REQUEST_CODE_SPEECH_INPUT_END) && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (result != null && result.isNotEmpty()) {
                val address = result[0]
                if (requestCode == REQUEST_CODE_SPEECH_INPUT_START) {
                    binding.addressStartTextView.text=address
                    getLocationFromAddress(address)
                } else {
                    binding.addressEndTextView.text=address
                    getLocationFromAddress(address)
                }
            }
        }
    }


    private fun getLocationFromAddress(address: String/*, latitudeTextView: TextView, longitudeTextView: TextView*/) {
        Log.d(TAG, "getLocationFromAddress: Starting geocoding for address $address")
        // HTTP 요청 형식: API 키와 주소를 입력하면 위도 경도로 바꿔줌
        val client = OkHttpClient()
        val url = "https://maps.googleapis.com/maps/api/geocode/json?address=${address}&key=$GEOCODING_API_KEY"

        val request = Request.Builder()
            .url(url)
            .build()


        client.newCall(request).enqueue(object : okhttp3.Callback {
            //실패시 출력
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e(TAG, "Geocoding API call failed", e)
            }

            //성공시 함수
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "Geocoding API response: $responseBody")

                    val json = JsonParser.parseString(responseBody) as JsonObject
                    val results = json["results"].asJsonArray
                    //대충 JSON추출 하는 코드라는 뜻
                    if (results.size() > 0) {
                        val location = results[0].asJsonObject["geometry"].asJsonObject["location"].asJsonObject
                        val latitude = location["lat"].asDouble
                        val longitude = location["lng"].asDouble
                        //텍스트를 넣어줌
                        Log.d("location","Latitude: $latitude")
                        Log.d("location","Longitude: $longitude")
                    } else {
                        Log.e(TAG, "No results found for the specified address.")
                        Log.d("location","Latitude: Not found")
                        Log.d("location","Longitude: Not found")
                    }
                } else {
                    Log.e(TAG, "Geocoding API response was not successful")
                    Log.d("location", "Latitude: Error")
                    Log.d("location","Longitude: Error")
                }
            }
        })
    }
    //Default함수 일단 실행 먼저 됨 API와의 통신을 위함
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val address = "서울특별시 강남구 강남대로 323"
            getLocationFromAddress(address)
        }
    }
}
