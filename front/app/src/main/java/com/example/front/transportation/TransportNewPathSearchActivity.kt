package com.example.front.transportation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.front.R
/*"경로 클릭시" 출력될 화면, 새 경로를 찾는 화면과 구 경로를
* 재사용할 때 동일하게 사용된다. 피그마의 경로 클릭시 경우 참고.*/
class TransportNewPathSearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_transport_new_path_search)

    }
}