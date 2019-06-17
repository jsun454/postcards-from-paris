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

class MailboxActivity : AppCompatActivity() {

    companion object {
        private const val NEW_POSTCARD = 0
        private const val SENT_TAB_POS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mailbox)

        if(FirebaseAuth.getInstance().uid == null) {
            startActivity<AuthUserActivity>(CLEAR_TASK or NEW_TASK or NO_ANIMATION)
        }

        val fragmentAdapter = MailboxPagerAdapter(supportFragmentManager)
        activity_mailbox_vp_mail_list.adapter = fragmentAdapter
        activity_mailbox_tab_mail_mode.setupWithViewPager(activity_mailbox_vp_mail_list)

        activity_mailbox_fab_new_postcard.setOnClickListener {
            startActivityForResult<NewPostcardActivity>(NEW_POSTCARD)
        }

        // TODO: if user is new (check for no received messages, or maybe pass in bool extra newUser=true to activity),
        //  send an automatic welcome message from "The PFP Team" or something as a one-time thing
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.menu_nav_options) {
            startActivity<OptionsActivity>(NEW_TASK)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == NEW_POSTCARD && resultCode == RESULT_OK && data != null) {
            if(data.getBooleanExtra(NewPostcardActivity.RETURN_TO_SENT_TAB, false)) {
                val sentTab = activity_mailbox_tab_mail_mode.getTabAt(SENT_TAB_POS)
                sentTab?.select()
            }
        }
    }
}