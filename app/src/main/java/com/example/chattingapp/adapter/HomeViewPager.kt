package com.example.chattingapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.chattingapp.CallHistory
import com.example.chattingapp.HomePage
import com.example.chattingapp.Status

class HomeViewPager(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private  var fragmentList1: ArrayList<Fragment> = ArrayList()
    private  var fragmentTitleList1: ArrayList<String> = ArrayList()
    override fun getCount(): Int {
        return fragmentList1.size
    }

    override fun getItem(position: Int): Fragment {
       /* return when (position) {
            0 -> HomePage()
            1 -> CallHistory()
            2 -> Status()
            else -> HomePage()
        }*/
        return fragmentList1[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
       /* return when (position) {
            0 -> "Chats"
            1 -> "Calls"
            2 -> "Status"
            else -> "Chats"
        }*/
        return fragmentTitleList1[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragmentList1.add(fragment)
        fragmentTitleList1.add(title)
    }

}