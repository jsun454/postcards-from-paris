package com.example.jeffrey.postcardsfromparis

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.jeffrey.postcardsfromparis.adapter.MailboxPagerAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_mailbox.*

class MailboxActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mailbox)

        if(FirebaseAuth.getInstance().uid == null) {
            val intent = Intent(this, AuthUserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        val fragmentAdapter = MailboxPagerAdapter(supportFragmentManager)
        activity_mailbox_vp_mail_list.adapter = fragmentAdapter
        activity_mailbox_tab_mail_mode.setupWithViewPager(activity_mailbox_vp_mail_list)

        activity_mailbox_fab_new_postcard.setOnClickListener {
            val intent = Intent(this, NewPostcardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.menu_nav_options) {
            val intent = Intent(this, OptionsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }
}
