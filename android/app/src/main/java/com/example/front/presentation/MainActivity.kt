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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)


        resultTextView = findViewById(R.id.resultTextView)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }

        // Fetch data when the floating action button is clicked
        binding.fab.setOnClickListener {
            FetchDataTask().execute()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
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
                    for (i in 0 until minOf(pathCount, 5)) {
                        val path = pathArray.getJSONObject(i)
                        val info = path.getJSONObject("info")
                        val totalTime = info.getInt("totalTime")
                        val busTransitCount = info.getInt("busTransitCount")
                        val subwayTransitCount = info.getInt("subwayTransitCount")
                        val totalWalk = info.getInt("totalWalk")

                        sb.append("Route_${i + 1} 총 소요 시간: ${totalTime}분, 환승 횟수: 지하철(${subwayTransitCount}) + 버스(${busTransitCount}), 도보 거리: ${totalWalk}m\n")

                        // 경로 상세 정보
                        val subPathArray = path.getJSONArray("subPath")
                        Log.d("현빈",subPathArray.length().toString())
//                        for (j in 0 until subPathArray.length()) {
//                            val subPath = subPathArray.getJSONObject(j)
//                            when (subPath.getInt("trafficType")) {
//                                1 -> { // 지하철
//                                    val laneArray = subPath.getJSONArray("lane")
//                                    for (k in 0 until laneArray.length()) {
//                                        val lane = laneArray.getJSONObject(k)
//                                        val subwayName = lane.getString("name")
//                                        val stationStart = subPath.getJSONObject("start").getString("name")
//                                        val stationEnd = subPath.getJSONObject("end").getString("name")
//                                        sb.append("지하철: $stationStart 역에서 $subwayName 타고 $stationEnd 역까지\n")
//                                    }
//                                }
//                                2 -> { // 버스
//                                    val laneArray = subPath.getJSONArray("lane")
//                                    for (k in 0 until laneArray.length()) {
//                                        val lane = laneArray.getJSONObject(k)
//                                        val busNo = lane.getString("busNo")
//                                        val busType = lane.getString("type")
//                                        val stationStart = subPath.getJSONObject("start").getString("name")
//                                        val stationEnd = subPath.getJSONObject("end").getString("name")
//                                        sb.append("버스: $stationStart 정류장에서 $busType $busNo 번 타고 $stationEnd 정류장까지\n")
//                                    }
//                                }
//                            }
//                        }
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

