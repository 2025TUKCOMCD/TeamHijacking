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
        override fun doInBackground(vararg params: Void?): String { //응답 받은 데이터를 백그라운드에 저장 시키는 함수

            // 대중교통API를 사용하기 위한 입력 데이터
            val apiKey = "I8OXxpKUrXPtFVPtouJCLoWPsBv1Nk0c/57vVfV9pBM" // 여기에 실제 API 키를 입력하세요.
            val sx = "126.926493082645"
            val sy = "37.6134436427887"
            val ex = "127.126936754911"
            val ey = "37.5004198786564"
            val opt = "0"
            val lang = "0" // 결과 언어를 한국어로 설정
            val output = "json" // 출력 포맷을 JSON으로 설정



            //먼저 StringBuilder를 사용해 URL을 생성
            val urlBuilder = StringBuilder("https://api.odsay.com/v1/api/searchPubTransPathT")
            //그 이후에 넣어줘야 할 데이터를 ?apiKey형태로 넣어주기
            urlBuilder.append("?apiKey=").append(apiKey)
            urlBuilder.append("&lang=").append(lang)
            urlBuilder.append("&output=").append(output)

            //이때 소수점이나 특수 문자가 포함되는 값은 URL로 전달되는 도중 값이 바뀔 수 있으므로 인코딩이 필요함
            //그래서 인코딩 진행
            urlBuilder.append("&SX=").append(URLEncoder.encode(sx, "UTF-8"))
            urlBuilder.append("&SY=").append(URLEncoder.encode(sy, "UTF-8"))
            urlBuilder.append("&EX=").append(URLEncoder.encode(ex, "UTF-8"))
            urlBuilder.append("&EY=").append(URLEncoder.encode(ey, "UTF-8"))
            urlBuilder.append("&OPT=").append(opt)


            //이걸 토대로 URL객체 생성
            val url = URL(urlBuilder.toString())

            //그 url을 HttpURLConnection을 열어 연결
            val conn = url.openConnection() as HttpURLConnection
            //이 방식에서 GET 요청방식을 사용하겠다는 의미
            conn.requestMethod = "GET"
            //요청헤더에 Content-type, application/json으로설정
            conn.setRequestProperty("Content-type", "application/json")

            return try {
                //응답코드 확인
                val responseCode = conn.responseCode

                //200~299사이면(성공이면)
                if (responseCode in 200..299) {
                    //inputStream에서 데이터를 읽어옴
                    val rd = BufferedReader(InputStreamReader(conn.inputStream))
                    val sb = StringBuilder()
                    var line: String?
                    //대충 rd에서 한줄씩 가져오므로 한줄씩 버퍼에 옮긴다는 내용
                    while (rd.readLine().also { line = it } != null) {
                        sb.append(line)
                    }
                    //다옮기면 BufferdReader는 종료 때리고 sb는 한줄로 String형으로 변환
                    rd.close()
                    sb.toString()
                } else { //오류가 났을때
                    //errorStream을 읽어옴
                    val rd = BufferedReader(InputStreamReader(conn.errorStream))
                    val sb = StringBuilder()
                    var line: String?
                    //에러 구문을 받아옴
                    while (rd.readLine().also { line = it } != null) {
                        sb.append(line)
                    }
                    rd.close()
                    //String으로 넣음
                    sb.toString()
                }
            } finally { //그리고 종료
                conn.disconnect()
            }
        }

        override fun onPostExecute(result: String) {  //AsyncTask 이후에 실행 되는 함수 : FetchTask함수가 AsyncTask함수임
            super.onPostExecute(result)
            Log.d("API Full Response", result) // 전체 응답 로그 출력
            try {
                val resultJson = JSONObject(result)  //JSON 형태로 바꿈
                if (resultJson.has("result")) { //Json이 result를 가지고 있다면
                    val resultData = resultJson.getJSONObject("result") //resultJson안에 result를 resultData로 분류
                    val pathArray = resultData.getJSONArray("path")  //resultData안에 있는 path를 PathArray로 분류
                    val pathCount = pathArray.length()  //pathArray의 길이를 토대로 전체 경로의 갯수를 파악
                    val sb = StringBuilder()  //sb로 화면에 출력할 데이터를 만듦

                    sb.append("총 경로 개수: $pathCount\n")
                    for (i in 0 until minOf(pathCount, 3)) {  //원래는 전체를 다 구하지만 2개만 보는걸로 타협 너무 많음
                        val path = pathArray.getJSONObject(i) //pathArray중 i 번째를 path에 넣음
                        val info = path.getJSONObject("info") //path내부 데이터중 info를 추출
                        val totalTime = info.getInt("totalTime") //info중 totalTim,busTransitCount(버스 환승 횟수), subwayTransitCount( 지하철 환승횟수), 총 걸은 시간 등등 추출
                        val busTransitCount = info.getInt("busTransitCount")
                        val subwayTransitCount = info.getInt("subwayTransitCount")
                        val totalWalk = info.getInt("totalWalk")


                        //1차 가공 결과를 텍스트넣는 sb에 넣음
                        sb.append("Route_${i + 1} 총 소요 시간: ${totalTime}분, 환승 횟수: 지하철(${subwayTransitCount}) + 버스(${busTransitCount}), 도보 거리: ${totalWalk}m\n")

                        // 경로 상세 정보
                        val subPathArray = path.getJSONArray("subPath")  //path중 상세 경로인 subPath를 subPathArray로 넣음
                        sb.append("****상세경로****\n")
                        for (j in 0 until subPathArray.length()) {
                            val subPath = subPathArray.getJSONObject(j)  //j번째 subPath경로를 추출
                            when (subPath.getInt("trafficType")) {  //1이면 지하철 2이면 버스 3이면 도보임
                                1 -> { // 지하철이라면 아래와 같은 데이터를 추출
                                    if (!subPath.isNull("lane")) { //널세이프
                                        val laneArray = subPath.getJSONArray("lane") //lane을 추출(이 부분이 특색있는정보를 가지고 있음 지하철, 버스등
                                        for (k in 0 until laneArray.length()) { //lane의 갯수만큼 반복 추출 대부분 1개임
                                            Log.d("현빈",laneArray.toString())
                                            val lane = laneArray.getJSONObject(k) //k번째 lane을 추출
                                            val sectionTime = subPath.getString("sectionTime") //걸리는 시간
                                            val subwayName = lane.getString("name")  // 몇호선인지 추출
                                            val stationStart = subPath.getString("startName") //시작역 추출
                                            val stationEnd = subPath.getString("endName")  //도착역 추출
                                            sb.append("지하철: $subwayName ( $stationStart"+"역"+"-> $stationEnd"+"역 ) $sectionTime"+"분\n")
                                        }
                                    } else {
                                        Log.d("현빈", "lane이 null입니다.")
                                    }
                                }
                                2 -> { // 버스
                                    if (!subPath.isNull("lane")) {  //널세이프
                                        val laneArray = subPath.getJSONArray("lane")  //lane을 추출(이 부분이 특색있는정보를 가지고 있음 지하철, 버스등
                                        val stationStart = subPath.getString("startName")// 시작역
                                        val stationEnd = subPath.getString("endName")// 종착역
                                        val sectionTime = subPath.getString("sectionTime") //걸리는 시간
                                        sb.append("버스: $stationStart 정류장에서 \n=========================\n")
                                        for (k in 0 until laneArray.length()) {
                                            val lane = laneArray.getJSONObject(k)  //k번째 lane을 추출
                                            val busNo = lane.getString("busNo")  //버스 번호 추출  11이면 간선을 뜻함
                                            val busType = lane.getString("type") //버스 타입 추출
                                            sb.append("$busNo($busType),")
                                        }
                                        sb.append("\n=========================\n$stationEnd 정류장까지 $sectionTime"+"분\n")
                                    } else {
                                        Log.d("현빈", "lane이 null입니다.")
                                    }
                                }
                                3 -> { // 도보
                                    if(subPath.optInt("distance")!=0){  //만약 0분이면 ex)지하철 환승할때 0분으로 찍힘 이러면 그냥 스킵
                                        val distance = subPath.optInt("distance", 0)  //혹시나 하니 디폴트값 넣어주고 걸은 거리 출력
                                        val duration = subPath.optInt("sectionTime", 0) //및 걸리는 시간 출력
                                        sb.append("도보: 거리 $distance m, 소요 시간 ${duration}분\n") //해서 텍스트에 넣어줌
                                    }
                                }
                            }
                        }
                        sb.append("-------------------------------------------------------------\n") //경계선 줄
                    }

                    val displayText = sb.toString()  //를 한번에 모아서 변수에 저장

                    // 결과를 로그로 출력
                    Log.d("Parsed Result", displayText)

                    // TextView 업데이트
                    resultTextView.text = displayText  //그리고 text뷰 업데이트

                    // 결과를 스낵바로 보여줌 결과를 하단에 쓰윽 나왔다 들어가게 하는 거
                    Snackbar.make(binding.root, displayText, Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab).show()  //잘 추출 되씅면 disPlayText를 반환해주는데
                } else {  //오류 나면 이렇게 나옴
                    throw Exception("JSON 응답에 'result' 키가 없습니다")
                }
            } catch (e: Exception) {  //대충 ㅈㄴ 오류나면 이렇게 나온다는 뜻
                e.printStackTrace()
                Snackbar.make(binding.root, "JSON 파싱 오류: ${e.message}", Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.fab).show()
            }
        }
    }
}

