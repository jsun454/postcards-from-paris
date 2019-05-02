package com.example.jeffrey.postcardsfromparis

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.jeffrey.postcardsfromparis.adapter.MailboxPagerAdapter
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.CLEAR_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.NEW_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.startActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_mailbox.*

class MailboxActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mailbox)

        if(FirebaseAuth.getInstance().uid == null) {
            startActivity<AuthUserActivity>(CLEAR_TASK or NEW_TASK)
        }

        val fragmentAdapter = MailboxPagerAdapter(supportFragmentManager)
        activity_mailbox_vp_mail_list.adapter = fragmentAdapter
        activity_mailbox_tab_mail_mode.setupWithViewPager(activity_mailbox_vp_mail_list)

        activity_mailbox_fab_new_postcard.setOnClickListener {
            startActivity<NewPostcardActivity>(NEW_TASK)
        }
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
}
