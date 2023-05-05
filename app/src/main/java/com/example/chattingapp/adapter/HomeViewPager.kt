package com.example.chattingapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.chattingapp.CallHistory
import com.example.chattingapp.HomePage
import com.example.chattingapp.Status

class HomeViewPager(fm: FragmentManager, val tabCount: Int) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return tabCount
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> HomePage()
            1 -> Status()
            2 -> CallHistory()
            else -> HomePage()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Chats"
            1 -> "Status"
            2 -> "Calls"
            else -> "Chats"
        }
    }

}