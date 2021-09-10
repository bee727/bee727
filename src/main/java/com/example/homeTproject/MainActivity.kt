package com.example.homeTproject

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.homeTproject.databinding.ActivityMainBinding
import com.example.homeTproject.databinding.HomefragmentBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mContext: Context
    private lateinit var binding: ActivityMainBinding
    private lateinit var mbinding : HomefragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mbinding = HomefragmentBinding.inflate(layoutInflater)

        configureBottomNavigation()

//        mbinding.btnSquatStart.setOnClickListener {
//            Toast.makeText(this@MainActivity, "토스트 메세지 띄우기 입니다.", Toast.LENGTH_SHORT).show()
//            val nextIntent : Intent = Intent(this@MainActivity, MocapActivity::class.java)
//            startActivity(nextIntent)
//        }




    }


    private fun configureBottomNavigation() {
        binding.viewPagerFra.adapter = FragmentPageAdapter(supportFragmentManager, 2)
        binding.tabMenu.setupWithViewPager(binding.viewPagerFra)

        val bottomNaviLayout : View = this.layoutInflater.inflate(R.layout.bottom_navigation_tab, null, false)

        binding.tabMenu.getTabAt(0)!!.customView =  bottomNaviLayout.findViewById(R.id.btn_home)
        binding.tabMenu.getTabAt(1)!!.customView =  bottomNaviLayout.findViewById(R.id.btn_list)
    }

    fun setActionBarTitle(title: String?) {
        if (supportActionBar != null) {
            supportActionBar?.title = title
        }
    }

    public fun onFragmentChange(index : Int) {
        // HomeFragment에서 시작버튼 클릭시 횟수 정하는 SelectCountFragment로
        // 이동하게 하는 함수 (작성중)
        if(index == 0){
//            supportFragmentManager.beginTransaction().replace()
        }
    }



}