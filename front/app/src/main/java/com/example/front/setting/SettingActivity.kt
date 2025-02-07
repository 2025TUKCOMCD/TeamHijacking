package com.example.front.setting

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivitySettingBinding
import org.w3c.dom.Text

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //view 바인딩
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //각각의 객체 바인딩
        val settingProfile: TextView = binding.settingProfile
        val settingNewestVersion: TextView = binding.settingNewestVersion
        val settingFAQ: TextView = binding.settingFAQ
        val askFormBtt: TextView = binding.askFormBtt

        //클릭 시 이동하도록...
        settingProfile.setOnClickListener{

        }
        settingNewestVersion.setOnClickListener{

        }
        settingFAQ.setOnClickListener {

        }
        askFormBtt.setOnClickListener {

        }
    }


}