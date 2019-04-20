package com.example.jeffrey.postcardsfromparis.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.jeffrey.postcardsfromparis.ReceivedMailFragment
import com.example.jeffrey.postcardsfromparis.SentMailFragment

class MailboxPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {

    companion object {
        const val TABS = 2
    }

    override fun getItem(p0: Int): Fragment {
        return when(p0) {
            0 -> ReceivedMailFragment()
            else -> SentMailFragment()
        }
    }

    override fun getCount(): Int {
        return TABS
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position) {
            0 -> "Received"
            else -> "Sent"
        }
    }
}