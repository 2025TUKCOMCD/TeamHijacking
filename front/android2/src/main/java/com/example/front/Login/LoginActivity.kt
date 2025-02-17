package com.example.front.Login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.front.databinding.ActivityLoginBinding
import com.example.front.MainActivity
import java.io.File
import java.io.FileInputStream
import java.util.Properties

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loginButton: ImageButton = binding.loginButton

        loginButton.setOnClickListener {

            //TODO:: 로그인이 끝난 후 실행될 수 있도록 조정할 것
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    //추후 구현할, 로그인 function
    fun kakaoLogin(){

    }

    //TODO:: 로그인 여부를 자동 확인하는 func 구현 필요
    fun isUserLogin(){

    }

    /*fun getApiKeyFromLocalProperties(context: Context): String {
        val properties = Properties()
        val localPropertiesFile = File(context.applicationInfo.dataDir, "../local.properties")

        if (localPropertiesFile.exists()) {
            properties.load(FileInputStream(localPropertiesFile))
        }

        return properties.getProperty("KAKAO_NATIVE_API_KEY", "")
    }*/
}