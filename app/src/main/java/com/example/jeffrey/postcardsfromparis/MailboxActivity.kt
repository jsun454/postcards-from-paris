package com.example.jeffrey.postcardsfromparis

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.jeffrey.postcardsfromparis.adapter.MailboxPagerAdapter
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.CLEAR_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.NEW_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.NO_ANIMATION
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.startActivity
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.startActivityForResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_mailbox.*

/**
 * This activity displays the user's sent and received postcards
 */
class MailboxActivity : AppCompatActivity() {

    companion object {
        private const val NEW_POSTCARD = 0
        private const val SENT_TAB_POS = 1
    }

    /**
     * Confirm that there is a user logged in, and set up the sent and received postcard tabs
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mailbox)

        // If there is no user logged in
        if(FirebaseAuth.getInstance().uid == null) {
            startActivity<AuthUserActivity>(CLEAR_TASK or NEW_TASK or NO_ANIMATION)
        }

        // Set up fragments for both postcard tabs
        val fragmentAdapter = MailboxPagerAdapter(supportFragmentManager)
        activity_mailbox_vp_mail_list.adapter = fragmentAdapter
        activity_mailbox_tab_mail_mode.setupWithViewPager(activity_mailbox_vp_mail_list)

        // Button for the user to create a new postcard
        activity_mailbox_fab_new_postcard.setOnClickListener {
            startActivityForResult<NewPostcardActivity>(NEW_POSTCARD)
        }
    }

    /**
     * Creates the options menu
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Handles options menu functionality
     *
     * @param item the menu item that was selected
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.menu_nav_options) {
            startActivity<OptionsActivity>(NEW_TASK)
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Handles the results received from the user creating a new postcard
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // User created a new postcard
        if(requestCode == NEW_POSTCARD && resultCode == RESULT_OK && data != null) {
            if(data.getBooleanExtra(NewPostcardActivity.RETURN_TO_SENT_TAB, false)) {
                // Switch to the sent tab if the user just sent a postcard
                val sentTab = activity_mailbox_tab_mail_mode.getTabAt(SENT_TAB_POS)
                sentTab?.select()
            }
        }
    }
}