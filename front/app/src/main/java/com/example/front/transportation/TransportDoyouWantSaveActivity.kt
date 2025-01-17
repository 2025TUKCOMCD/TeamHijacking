package com.example.front.transportation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.front.R
import com.example.front.databinding.ActivityTransportDoyouWantSaveBinding
import com.example.front.databinding.ActivityTransportationSavedPathBinding

//모달을 띄우기 위한 임시.,
class TransportDoyouWantSaveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportDoyouWantSaveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransportDoyouWantSaveBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}