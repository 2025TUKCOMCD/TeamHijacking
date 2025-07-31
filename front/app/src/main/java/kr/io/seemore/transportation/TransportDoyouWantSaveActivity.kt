package kr.io.seemore.transportation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.io.seemore.databinding.ActivityTransportDoyouWantSaveBinding

//모달을 띄우기 위한 임시.,
class TransportDoyouWantSaveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportDoyouWantSaveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransportDoyouWantSaveBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}