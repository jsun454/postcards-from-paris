package com.example.jeffrey.postcardsfromparis.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.jeffrey.postcardsfromparis.ReceivedMailFragment
import com.example.jeffrey.postcardsfromparis.SentMailFragment

/**
 * This class is the adapter for the sent/received mail tabs in the user's mailbox
 *
 * @property fm the fragment manager for this adapter
 */
class MailboxPagerAdapter(private val fm: FragmentManager?) : FragmentPagerAdapter(fm) {

    companion object {
        const val TABS = 2
    }

    /**
     * Returns the received or sent mail tab's fragment depending on the current tab position
     *
     * @param position the current tab position
     * @return the fragment corresponding to the current tab position
     */
    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> ReceivedMailFragment()
            else -> SentMailFragment()
        }
    }

    /**
     * Returns the number of tabs
     *
     * @return the number of tabs
     */
    override fun getCount(): Int {
        return TABS
    }

    /**
     * Returns the page title corresponding to the current tab
     *
     * @param position the current tab position
     * @return the page title of the current tab
     */
    override fun getPageTitle(position: Int): CharSequence? {
        return when(position) {
            0 -> "Received"
            else -> "Sent"
        }
    }
}