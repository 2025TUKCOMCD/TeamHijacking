package kr.io.seemore.transportation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.io.seemore.databinding.ActivityTransRealTimeLocationBinding

class TransRealtimeLocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransRealTimeLocationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransRealTimeLocationBinding.inflate(layoutInflater)
        setContentView(binding.main)


    }
}