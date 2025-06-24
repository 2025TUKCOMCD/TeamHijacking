package com.example.front


import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.front.databinding.ActivityMainBinding
import com.example.front.iot.IotPage01
import com.example.front.iot.IotPage02
import com.example.front.iot.IotPage03
import com.example.front.login.processor.UserProcessor
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.core.net.toUri

// FragmentStateAdapter
//Fragment 화면 이동 하게 해주는 코드
class MyPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> IotPage01()
            1 -> IotPage02()
            2 -> IotPage03()
            else -> throw IllegalStateException("Invalid position")
        }
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager: ViewPager2 = binding.viewpager
        val pagerAdapter = MyPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        //이름을 받아올 수 있는지 테스트 위한 코드
        val name = intent.getStringExtra("name")
        Toast.makeText(this, "어서오세요 $name 님", Toast.LENGTH_SHORT).show()

        // 딥링크 처리 로직 추가
        val appLinkIntent: Intent = intent
        val appLinkData: Uri? = appLinkIntent.data

        appLinkData?.let { uri ->
            // 딥링크 URI 스킴과 호스트 확인 (seemore://main)
            if (uri.scheme == "seemore" && uri.host == "main") {
                val state = uri.getQueryParameter("state")

                state?.let {
                    sendData(this,"/kakao","login_id",it)

                    Log.d("현빈", "Deep link 'state' parameter received: $it")
                    Toast.makeText(this, "딥링크 'state' 값: $it", Toast.LENGTH_LONG).show()
                    // 'state' 값을 사용해 필요한 작업을 수행
                    // 예: 특정 UI 업데이트, 데이터 로드, 로그인 상태 확인 등
                    UserProcessor.getSmartThingsToken(it) { token ->
                        if (token != null) {
                            saveSmartThingsToken(token)
                            Log.d("SmartThings", "토큰 저장 완료: $token")
                        } else {
                            Log.e("SmartThings", "토큰 저장 실패")
                        }
                    }
                } ?: run {
                    Log.d("현빈", "Deep link received, but 'state' parameter is null.")
                }
            } else {
                Log.d("현빈", "Non-seemore://main deep link received: $uri")
            }
        } ?: run {
            Log.d("현빈", "No deep link data received on launch.")
        }

        //탭 레이아웃 으로 하단에 나오는 버튼 이름을 일단 설정
        val tabLayout: TabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.setIcon(R.drawable.ic_home) // 아이콘 설정
                    tab.text = "관리" // 텍스트
                }
                1 -> {
                    tab.setIcon(R.drawable.ic_add)
                    tab.text = "추가"
                }
                2 -> {
                    tab.setIcon(R.drawable.ic_settings)
                    tab.text = "설정"
                }
            }
        }.attach()

        //ViewPager 의 페이지 변경에 따라 툴바 텍스트 변경
        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val title = when (position) {
                    0 -> "IoT 기기 관리"
                    1 -> "IoT 기기 추가"
                    2 -> "설정"
                    else -> "IoT 기기 관리"
                }
                binding.toolBarText.text = title
                binding.backStepBtn.visibility = if (position == 0) android.view.View.GONE else android.view.View.VISIBLE
                binding.iotAddBtn.visibility = if (position == 2 ) android.view.View.GONE else android.view.View.VISIBLE
            }
        })

        //뒤로 가기 진행
        binding.backStepBtn.setOnClickListener {
            val currentPosition = binding.viewpager.currentItem
            Log.d("ViewPager", "현재 위치: $currentPosition")
            if ( currentPosition > 0 ) {
                val newPosition = currentPosition - 1
                binding.viewpager.setCurrentItem(newPosition, true)
                Log.d("ViewPager", "현재 위치: $currentPosition -> 이동할 위치: ${currentPosition - 1}")
            }
            /* 장치 추가 페이지 임시로 삭제, 페이지 두 개로 수정 */
        }

        //장치 추가할 수 있도록 진행
        binding.iotAddBtn.setOnClickListener {
            openSmartThingsApp()
        }
    }
    //android 로부터 watch 로 데이터 보내기 위한 테스트 코드
    fun sendData(context: Context, requestapi : String, key: String, value: String) {
        val dataClient = Wearable.getDataClient(context)
        val putDataReq = PutDataMapRequest.create(requestapi).run {
            dataMap.putString(key, value)
            asPutDataRequest()
        }

        val putDataTask: Task<DataItem> = dataClient.putDataItem(putDataReq)

        putDataTask.addOnSuccessListener {
            Log.d(TAG, "데이터 전송 성공: $key = $value")
            Toast.makeText(context, "데이터 전송 성공", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Log.e(TAG, "데이터 전송 실패: ${exception.message}", exception)
            Toast.makeText(context, "데이터 전송 실패: ${exception.message}", Toast.LENGTH_SHORT).show()

            if (exception is ApiException) {
                val apiException = exception
                Log.e(TAG, "API Exception Status Code: ${apiException.statusCode}")
                // 필요에 따라 추가적인 오류 처리 로직 구현 (예: 특정 상태 코드에 따른 다른 UI 표시)
            }
        }
    }

    private fun saveSmartThingsToken(token: String) {
        val sharedPref = getSharedPreferences("smartThingsPrefs", AppCompatActivity.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("smartThingsToken", token)
            apply()
        }
        Log.d("SmartThings", "토큰 저장 완료: $token")
    }

    //     🔗 SmartThings 앱 열기 (설치되지 않았으면 Play Store로 이동)
    private fun openSmartThingsApp() {
//        try {
//            val intent = packageManager.getLaunchIntentForPackage("com.samsung.android.oneconnect")
//            Log.d("iot", "intent: ${intent}")
//            if (intent != null) {
//                Log.d("iot", "intent가 null이 아닙니다. intent = ${intent}")
//                startActivity(intent)
//            } else {
//                Log.d("iot", "intent가 null입니다.")
//                val playStoreIntent = Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse("market://details?id=com.samsung.android.oneconnect")
//                )
//                startActivity(playStoreIntent)
//            }
//        } catch (e: ActivityNotFoundException) {
//            Log.e("iot", "smartThings 앱이 설치되어 있지 않습니다.")
//        }
        val uri = Uri.parse("https://app.smartthings.com/add/device") // 또는 다른 공식 URI
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.samsung.android.oneconnect") // SmartThings 앱에만 전달

        Log.d("iot", "Intent: $intent, Resolved Activity: ${intent.resolveActivity(packageManager)}")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // PlayStore fallback
            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.samsung.android.oneconnect"))
            startActivity(playStoreIntent)
        }
        //왜인지는 모르겠는데, 주석 처리된 코드는 intent를 null로 반환하여 smartThings app 페이지로 이동하고
        //아래의 코드도 동일하게 동작. SmartThings app으로 넘어가질 않음.
        //TODO:: 해결해야함..
    }
}