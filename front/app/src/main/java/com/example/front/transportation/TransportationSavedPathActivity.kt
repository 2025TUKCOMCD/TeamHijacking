package com.example.front.transportation

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.front.R
import com.example.front.databinding.ActivityTransportationSavedPathBinding

    /* onCreate()시, 저장된 경로를 데이터베이스로부터 받아오는 whatSavedPath() 함수를 이용해
    *  버튼 생성 필요 */
class TransportationSavedPathActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportationSavedPathBinding
        //이 경로가 즐겨찾기로 설정한 변수인지, 아닌지 확인하기 위해 넣어둔 변수
        //추후 각 경로마다 하나씩 들어가도록 코드 수정하여야 함
    private var isFavouritePath:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransportationSavedPathBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //각 요소 바인딩, someRootThing은 추후 삭제 필요.
        val someRootThing: LinearLayout = binding.someRootThing
        val savedPathRootLayout: LinearLayout = binding.savedPathRootLayout
        val favouritePathStarBtt: ImageView = binding.favouritePathStarBtt
        val imsiBtt4: Button = binding.imsiBtt4


        //textView에 text 삽입
        //someRootThing.text="여기에서 텍스트 수정이 가능함"

        imsiBtt4.setOnClickListener{
            openView()
        }

        someRootThing.setOnClickListener{
            //일단 한 번 읽어주고
            //세부 정보로 넘어감
            // 새로운 경로 탐색 버튼 클릭 시 실행할 로직
            val intent = Intent(this, TransportNewPathSearchActivity::class.java)
            startActivity(intent)
        }

        favouritePathStarBtt.setOnClickListener{
            starBtnClickListener(favouritePathStarBtt)
        }

    }

    private fun openView() {
        /*추후 TransSavedPathActivity가 열릴 시 작동할 function,
        * onCreate시 작동하여, database로부터 경로 목록 받아와 그 갯수만큼 버튼 생성.
        * 버튼 생성 function은 하단의 createPathBtt 이용, 순회하며 받아옴 */


        val inflater = layoutInflater
        val savedPathRootLayout: LinearLayout = findViewById(R.id.transSavedPathLayout)

        // trans_saved_button.xml을 inflate해 새로운 LinearLaytou 추가
        val newLayout  = inflater.inflate(R.layout.trans_saved_path_button, savedPathRootLayout, false) as LinearLayout

        // 동적으로 추가된 뷰의 텍스트 설정
        val addressNicknameTextView: TextView = newLayout.findViewById(R.id.addressNicknameTextview)
        addressNicknameTextView.text = "새로운 경로"

        val departureTextView: TextView = newLayout.findViewById(R.id.departureTextView)
        departureTextView.text = "출발지: 예시출발지"

        val destinationTextView: TextView = newLayout.findViewById(R.id.destinationTextView)
        destinationTextView.text = "목적지: 예시목적지"

        // savedPathRootLayout에 추가
        savedPathRootLayout.addView(newLayout)

        //레이아웃 갱신
        savedPathRootLayout.requestLayout()
    }

    private fun createPathBtt(savedPathThing: LinearLayout) {
        //하... 각 path를 불러오고, text를 설정하는 기능
        //아예 다른 activity로 만들어서 불러오는게 나을지도 모르겠음..
        /* todo::
        *   1. path 지정 btt UIUX 디자인 (완료)
        *   2. UIUX 개발 (대략 완료? ㅠㅠ)
        *   3. 해당 버튼을 이 activity에 불러오도록 구현
        *   4. 그 요소의 text 변경하도록 대략적으로..  */

        val childAddressNickname: TextView
        //val favouritePathStarBtt: ImageView
        val departureTextView: TextView
        val destinationTextView: TextView
    }

    private fun starBtnClickListener(favouritePathStar: ImageView) {
        /*id가 savedPathRootLayout인, 일종의 저장 경로 띄워주기용 view의
        * favouriteStarBtt 클릭 시 버튼의 색이 노란색으로 바뀌며,
        * 다른 경로에 비해 우선도를 가지도록 조정하는 버튼
        * 의미없이 LinearLayout을 변수로 받아오도록 한 것은, 그를 통해 추후 각 요소에만 영향을
        * 미치도록 구현하기 위함임
        *  todo::
        *    1. click시 버튼의 색이 바뀌며, bool로 지정한 isFavouriteRoot의 값이 바뀌도록
        *       (단, 이는 데이터베이스의 생성이 끝난 뒤에야 가능할 성 싶음)
        *    2. 그러니 현재는 버튼의 색을 바꾸는 데에 집중할 예정.
        *    3. 클릭 시 저장된 경로의 순서를 바꿀 수 있도록, 정렬 메소드를 호출
        *       (이 역시 경로의 개발이 완료된 후 가능할 성 싶음)
        *    4. 이 곳에 필요한 것을 바탕으로, database에 들어가야 할 내역에 대한
        *       체계적 정리 및 이슈화 필요
        *    5. charGPT의 조언에 따르면, 각 savedPathRootLayout에 개별적으로
        *       즐겨찾기 상태를 관리하기 위해서는 isFavouritePath 대신 각 LinearLayout
        *       또는 경로마다 상태를 추적하는 별도의 데이터 구조가 필요하다고 함. 그 부분에 대한
        *       추가적인 코드 수정이 요구됨 (Map <LinearLayout, Boolean> 이나 data class를
        *       활용하는 등 */

        //자식 뷰 중 ImageView를 찾아 tint를 변경하기 위한, 임시 코드
        val favouriteStarBtt: ImageView = favouritePathStar

        if(!isFavouritePath) {
            favouriteStarBtt.setColorFilter(getColor(R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN)
            isFavouritePath = true
        } else {
            favouriteStarBtt.setColorFilter(getColor(R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN)
            isFavouritePath = false
        }
    }

    private fun whatSavedPath() {
        /* 동적으로 생성된 LinearLayout- 요소에, 출발지, 목적지, 닉네임 등을 setting하는 함수
        * 20250124 기준 사용하고 있는 변수는 다음과 같음::
        *   addressNicknameTextView: TextView = 저장된 경로의 닉네임 삽입
        *   favouritePathStarBtt: ImageView = 즐겨찾기 버튼,
        *   departureTextView: TextView = 출발지 Text를 set할 곳
        *   destinationTextView: TextView = 도착지 Text를 set할 곳
        * todo::
        *    1. 경로 정렬하도록 하는 function */
    }
}