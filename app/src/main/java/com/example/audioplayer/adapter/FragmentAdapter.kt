package com.example.audioplayer.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class FragmentAdapter(private val fragmentList:MutableList<Fragment>,private val fragmentManager:FragmentManager):FragmentPagerAdapter(fragmentManager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment =
        fragmentList[position]

    override fun getCount(): Int =
        fragmentList.size
}