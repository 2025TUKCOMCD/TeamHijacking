package com.example.front


import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.front.databinding.ActivityMainBinding
import com.example.front.Iot.Iot_page01
import com.example.front.Iot.Iot_page02
import com.example.front.Iot.Iot_page03
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

// FragmentStateAdapter
//Fragment 화면 이동 하게 해주는 코드
class MyPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Iot_page01()
            1 -> Iot_page02()
            2 -> Iot_page03()
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
        val tabLayout: TabLayout = binding.tabLayout //
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.setIcon(R.drawable.ic_home) // 홈 아이콘 설정
                    tab.text = "홈" // 텍스트 (선택 사항)
                }
                1 -> {
                    tab.setIcon(R.drawable.ic_add) // + 아이콘 설정
                    tab.text = "추가" // 텍스트 (선택 사항)
                }
                2 -> {
                    tab.setIcon(R.drawable.ic_settings) // 제어 아이콘 설정
                    tab.text = "제어" // 텍스트 (선택 사항)
                }
            }
        }.attach()
    }
}