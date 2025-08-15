package kr.io.seemore.iot

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import kr.io.seemore.R
import kr.io.seemore.databinding.FragmentIotPage02Binding
import android.net.Uri

class IotPage02 : Fragment() {

    private var _binding: FragmentIotPage02Binding? = null
    private val binding get() = _binding!!
    private val tag = "IoT_page02"
    private lateinit var telToSubwayBtn:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIotPage02Binding.inflate(inflater, container, false)
        telToSubwayBtn = binding.telToSubwayBtn
        telToSubwayBtn.setOnClickListener {
            callToSubway()
        }

        return binding.root
    }



    //------------custom func 전화번호 선택 함수
    fun callToSubway() {
        var phoneNumber: String = "00099990000"  //selectCallNum
        //위치 정보 받아왔다면 여기에서 체크::
        //혹은 위치에 따라 다른 func 하도록 구현

        //전화하기
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }

    fun selectCallNum() {

    }
}