package com.ricardotejo.openpose

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class FragmentPageAdapter(fm: FragmentManager, val fragmentCount : Int) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment {

        return when(position){
            0 -> {
                HomeFragment()}
            1 -> LikeFragment()
            else -> HomeFragment()
        }
    }

    override fun getCount(): Int {
        return fragmentCount
    }



}