package com.example.jeffrey.postcardsfromparis

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.MotionEvent
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.CLEAR_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.NEW_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.hideKeyboard
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.startActivity
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.toast
import com.example.jeffrey.postcardsfromparis.util.SingleTapGestureListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

/**
 * This activity handles the user login process
 */
class LoginActivity : AppCompatActivity(), SingleTapGestureListener {

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
    }

    private lateinit var detector: GestureDetectorCompat

    /**
     * Sets title and click listeners
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.title = "Login"

        // Sets listeners for background taps
        detector = GestureDetectorCompat(this, this)
        detector.setOnDoubleTapListener(this)

        activity_login_sv_background.setOnTouchListener { _, event ->
            detector.onTouchEvent(event)
        }

        // Login button
        activity_login_btn_login.setOnClickListener {
            activity_login_btn_login.isClickable = false

            verifyUser()
        }
    }

    /**
     * Authenticates user and displays explanation message if authentication fails
     */
    private fun verifyUser() {
        val email = activity_login_et_email.text.toString()
        val password = activity_login_et_password.text.toString()

        // If fields are left blank
        if(email.isEmpty() || password.isEmpty()) {
            toast("Please enter email/password")

            activity_login_btn_login.isClickable = true

            return
        }

        // Attempt to authenticate user
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

    /**
     * Handles background taps
     *
     * @param e unused parameter in this implementation
     * @return that the motion event was consumed
     */
    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        activity_login_et_email.clearFocus()
        activity_login_et_password.clearFocus()

        window.decorView.rootView.hideKeyboard()

        return true
    }

    /**
     * Consumes the motion event if either input field has focus
     *
     * @param e unused parameter in this implementation
     * @returns whether the motion event was consumed
     */
    override fun onDown(e: MotionEvent?): Boolean {
        return activity_login_et_email.hasFocus() || activity_login_et_password.hasFocus()
    }
}