package com.example.front.transportation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.front.R
import com.example.front.databinding.ActivityTransportationSavedPathBinding

    /* onCreate()시, 저장된 경로를 데이터베이스로부터 받아오는 whatSavedPath() 함수를 이용해 버튼 생성 필요 */
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
        val imsiBtt4: Button = binding.imsiBtt4


        imsiBtt4.setOnClickListener{
            /* 임시 버튼, 서버가 완성되면 서버의 데이터베이스를 순회하며 추가하도록 수정 예정*/
            openView()
        }

        someRootThing.setOnClickListener{
            //일단 한 번 읽어주고 세부 정보로 넘어감
            // 새로운 경로 탐색 버튼 클릭 시 실행할 로직
            /* TODO::
               1. intent 전달 시에 매개변수로 여러가지 전달하여, 그에 맞게 TransportNewPathSearchActivity가
               수정되도록 코드 바꾸어야 함.
            * */
            val intent = Intent(this, TransportNewPathSearchActivity::class.java)
            startActivity(intent)
        }

    }

    private fun openView(
            addressNicknameText: String = "새로운 경로",
            departureText: String = "임시 출발지",
            destinationText: String = "임시 목적지",
            favouritePathStarValue: Boolean = false) {
        /*추후 TransSavedPathActivity가 열릴 시 작동할 function,
        * onCreate시 작동하여, database로부터 경로 목록 받아와 그 갯수만큼 버튼 생성.
        * 버튼 생성 function은 하단의 createPathBtt 이용, 순회하며 받아옴 */

        val inflater = layoutInflater
        val savedPathRootLayout: LinearLayout = findViewById(R.id.transSavedPathLayout)

        // trans_saved_button.xml을 inflate해 새로운 LinearLaytout 추가
        val newLayout:LinearLayout = inflater.inflate(R.layout.trans_saved_path_button, savedPathRootLayout, false) as LinearLayout

        // 동적으로 추가된 뷰 설정
        val addressNicknameTextView: TextView = newLayout.findViewById(R.id.addressNicknameTextview)
        val departureTextView: TextView = newLayout.findViewById(R.id.departureTextView)
        val destinationTextView: TextView = newLayout.findViewById(R.id.destinationTextView)
        val favouritePathStarBtt: ImageView = newLayout.findViewById(R.id.favouritePathStarBtt)

        //각 변수에 text 설정
        addressNicknameTextView.text = addressNicknameText
        departureTextView.text = "출발지: "+departureText
        destinationTextView.text = "목적지: "+destinationText

        // 즐겨찾기 버튼 클릭 이벤트 설정
        favouritePathStarBtt.setOnClickListener {
            starBtnClickListener(favouritePathStarBtt)
        }

        // savedPathRootLayout에 추가
        savedPathRootLayout.addView(newLayout)

        //레이아웃 갱신
        savedPathRootLayout.requestLayout()
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

}