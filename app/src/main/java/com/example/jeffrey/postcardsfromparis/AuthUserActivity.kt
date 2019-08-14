package com.example.jeffrey.postcardsfromparis

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.NEW_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.startActivity
import kotlinx.android.synthetic.main.activity_auth_user.*

/**
 * This activity lets the user log in or register a new account
 */
class AuthUserActivity : AppCompatActivity() {

    /**
     * Sets button click listeners
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_user)

        // If the user chooses to register a new account
        activity_auth_user_btn_register.setOnClickListener {
            activity_auth_user_btn_register.isClickable = false
            activity_auth_user_btn_login.isClickable = false

            startActivity<RegisterActivity>(NEW_TASK)
        }

        // If the user chooses to log in
        activity_auth_user_btn_login.setOnClickListener {
            activity_auth_user_btn_register.isClickable = false
            activity_auth_user_btn_login.isClickable = false

            startActivity<LoginActivity>(NEW_TASK)
        }
    }

    /**
     * Makes buttons clickable again
     */
    override fun onResume() {
        super.onResume()

        activity_auth_user_btn_register.isClickable = true
        activity_auth_user_btn_login.isClickable = true
    }
}
