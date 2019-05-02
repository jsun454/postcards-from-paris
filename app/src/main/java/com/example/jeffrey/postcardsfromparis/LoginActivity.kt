package com.example.jeffrey.postcardsfromparis

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.CLEAR_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.NEW_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.startActivity
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.title = "Login"

        activity_login_btn_login.setOnClickListener {
            activity_login_btn_login.isClickable = false

            verifyUser()
        }
    }

    private fun verifyUser() {
        val email = activity_login_et_email.text.toString()
        val password = activity_login_et_password.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            toast("Please enter email/password")
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully logged in as user: ${it.user.uid}")

                startActivity<MailboxActivity>(CLEAR_TASK or NEW_TASK)
            }
            .addOnFailureListener {
                Log.w(TAG, "Failed to log in as user: ${it.message}")
                toast("Login failed: ${it.message}")

                activity_login_btn_login.isClickable = true
            }
    }
}
