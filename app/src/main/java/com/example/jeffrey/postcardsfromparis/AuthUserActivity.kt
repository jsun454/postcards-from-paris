package com.example.jeffrey.postcardsfromparis

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_auth_user.*

class AuthUserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_user)

        activity_auth_user_btn_register.setOnClickListener {
            activity_auth_user_btn_register.isClickable = false
            activity_auth_user_btn_login.isClickable = false

            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        activity_auth_user_btn_login.setOnClickListener {
            activity_auth_user_btn_register.isClickable = false
            activity_auth_user_btn_login.isClickable = false

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        activity_auth_user_btn_register.isClickable = true
        activity_auth_user_btn_login.isClickable = true
    }
}
