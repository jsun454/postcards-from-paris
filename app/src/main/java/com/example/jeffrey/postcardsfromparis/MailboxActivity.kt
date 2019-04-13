package com.example.jeffrey.postcardsfromparis

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu

class MailboxActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mailbox)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_nav, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
