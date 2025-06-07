package com.example.front


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
//import com.example.front.iot.IotPage02
import com.example.front.iot.IotPage03
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

// FragmentStateAdapter
//Fragment 화면 이동 하게 해주는 코드
class MyPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> IotPage01()
//            1 -> IotPage02()
            1 -> IotPage03()
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

        //탭 레이아웃 으로 하단에 나오는 버튼 이름을 일단 설정
        val tabLayout: TabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.setIcon(R.drawable.ic_home) // 아이콘 설정
                    tab.text = "관리" // 텍스트
                }
//                1 -> {
//                    tab.setIcon(R.drawable.ic_add)
//                    tab.text = "추가"
//                }
                1 -> {
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
//                    1 -> "IoT 기기 추가"
                    1 -> "설정"
                    else -> "IoT 기기 관리"
                }
                binding.toolBarText.text = title
                binding.backStepBtn.visibility = if (position == 0) android.view.View.GONE else android.view.View.VISIBLE
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
    }
}