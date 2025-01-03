package com.example.front.presentation

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.front.presentation.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater) //바인딩
        setContentView(binding.root) //화면 표시

        setSupportActionBar(binding.toolbar)  //위상단에 android 글자표시

        resultTextView = findViewById(R.id.resultTextView)  //resultTextView를 객체로 받아옴

        binding.fab.setOnClickListener {    //메인 기믹 오른쪽 아래 버튼을 클릭하면 FetchDataTask가 실행됨
            FetchDataTask().execute()
        }
    }

    inner class FetchDataTask : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
            val apiKey = "I8OXxpKUrXPtFVPtouJCLoWPsBv1Nk0c/57vVfV9pBM" // 여기에 실제 API 키를 입력하세요.
            val sx = "126.926493082645"
            val sy = "37.6134436427887"
            val ex = "127.126936754911"
            val ey = "37.5004198786564"
            val opt = "0"
            val lang = "0" // 결과 언어를 한국어로 설정
            val output = "json" // 출력 포맷을 JSON으로 설정

            val urlBuilder = StringBuilder("https://api.odsay.com/v1/api/searchPubTransPathT")
            urlBuilder.append("?apiKey=").append(apiKey)
            urlBuilder.append("&lang=").append(lang)
            urlBuilder.append("&output=").append(output)
            urlBuilder.append("&SX=").append(URLEncoder.encode(sx, "UTF-8"))
            urlBuilder.append("&SY=").append(URLEncoder.encode(sy, "UTF-8"))
            urlBuilder.append("&EX=").append(URLEncoder.encode(ex, "UTF-8"))
            urlBuilder.append("&EY=").append(URLEncoder.encode(ey, "UTF-8"))
            urlBuilder.append("&OPT=").append(opt)

            val url = URL(urlBuilder.toString())
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Content-type", "application/json")

            return try {
                val responseCode = conn.responseCode
                if (responseCode in 200..299) {
                    val rd = BufferedReader(InputStreamReader(conn.inputStream))
                    val sb = StringBuilder()
                    var line: String?
                    while (rd.readLine().also { line = it } != null) {
                        sb.append(line)
                    }
                    rd.close()
                    sb.toString()
                } else {
                    val rd = BufferedReader(InputStreamReader(conn.errorStream))
                    val sb = StringBuilder()
                    var line: String?
                    while (rd.readLine().also { line = it } != null) {
                        sb.append(line)
                    }
                    rd.close()
                    sb.toString()
                }
            } finally {
                conn.disconnect()
            }
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            Log.d("API Full Response", result) // 전체 응답 로그 출력
            try {
                val resultJson = JSONObject(result)
                if (resultJson.has("result")) {
                    val resultData = resultJson.getJSONObject("result")
                    val pathArray = resultData.getJSONArray("path")
                    val pathCount = pathArray.length()
                    val sb = StringBuilder()

                    sb.append("총 경로 개수: $pathCount\n")
                    for (i in 0 until minOf(pathCount, 2)) {
                        val path = pathArray.getJSONObject(i)
                        val info = path.getJSONObject("info")
                        val totalTime = info.getInt("totalTime")
                        val busTransitCount = info.getInt("busTransitCount")
                        val subwayTransitCount = info.getInt("subwayTransitCount")
                        val totalWalk = info.getInt("totalWalk")

                        sb.append("Route_${i + 1} 총 소요 시간: ${totalTime}분, 환승 횟수: 지하철(${subwayTransitCount}) + 버스(${busTransitCount}), 도보 거리: ${totalWalk}m\n")

                        // 경로 상세 정보
                        val subPathArray = path.getJSONArray("subPath")
                        sb.append("****상세경로****\n")
                        for (j in 0 until subPathArray.length()) {
                            val subPath = subPathArray.getJSONObject(j)
                            Log.d("현빈", "버그다1")
                            when (subPath.getInt("trafficType")) {
                                1 -> { // 지하철
                                    if (!subPath.isNull("lane")) {
                                        Log.d("현빈", "버그다2")
                                        val laneArray = subPath.getJSONArray("lane")
                                        for (k in 0 until laneArray.length()) {
                                            Log.d("현빈",laneArray.toString())
                                            Log.d("현빈", "버그다3")
                                            val lane = laneArray.getJSONObject(k)
                                            Log.d("현빈", "버그다4")
                                            val subwayName = lane.getString("name")
                                            Log.d("현빈", "버그다5")
                                            val stationStart = subPath.getString("startName")

                                            Log.d("현빈", "버그다6")
                                            val stationEnd = subPath.getString("endName")

                                            Log.d("현빈", "버그다7")
                                            sb.append("지하철: $stationStart 역에서 $subwayName 타고 $stationEnd 역까지\n")
                                            Log.d("현빈", "버그다8")
                                        }
                                    } else {
                                        Log.d("현빈", "lane이 null입니다.")
                                    }
                                }
                                2 -> { // 버스
                                    if (!subPath.isNull("lane")) {
                                        Log.d("현빈", "버그다8")
                                        val laneArray = subPath.getJSONArray("lane")
                                        Log.d("현빈", "버그다9")
                                        for (k in 0 until laneArray.length()) {

                                            Log.d("현빈", "버그다10")
                                            val lane = laneArray.getJSONObject(k)
                                            Log.d("현빈", "버그다11")
                                            val busNo = lane.getString("busNo")
                                            Log.d("현빈", "버그다12")
                                            val busType = lane.getString("type")
                                            Log.d("현빈", "버그다13")
                                            val stationStart = subPath.getString("startName")
                                            Log.d("현빈", "버그다14")
                                            val stationEnd = subPath.getString("endName")
                                            Log.d("현빈", "버그다15")
                                            sb.append("버스: $stationStart 정류장에서 $busType $busNo 번 타고 $stationEnd 정류장까지\n")
                                            Log.d("현빈", "버그다16")
                                        }
                                    } else {
                                        Log.d("현빈", "lane이 null입니다.")
                                    }
                                }
                                3 -> { // 도보
                                    if(subPath.optInt("distance")!=0){
                                        val distance = subPath.optInt("distance", 0)
                                        val duration = subPath.optInt("sectionTime", 0)
                                        sb.append("도보: 거리 $distance m, 소요 시간 ${duration}분\n")
                                    }
                                }
                            }
                        }
                        sb.append("-------------------------------------------------------------\n")
                    }

                    val displayText = sb.toString()

                    // 결과를 로그로 출력
                    Log.d("Parsed Result", displayText)

                    // TextView 업데이트
                    resultTextView.text = displayText

                    // 결과를 스낵바로 보여줌
                    Snackbar.make(binding.root, displayText, Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab).show()
                } else {
                    throw Exception("JSON 응답에 'result' 키가 없습니다")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Snackbar.make(binding.root, "JSON 파싱 오류: ${e.message}", Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.fab).show()
            }
        }
    }
}

