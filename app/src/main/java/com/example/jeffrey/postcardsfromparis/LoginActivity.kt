package com.example.jeffrey.postcardsfromparis

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.CLEAR_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.NEW_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.startActivity
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
    }

    private lateinit var detector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.title = "Login"

        detector = GestureDetectorCompat(this, this)
        detector.setOnDoubleTapListener(this)

        activity_login_sv_background.setOnTouchListener { _, motionEvent ->
            detector.onTouchEvent(motionEvent)
        }

        activity_login_et_email.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        activity_login_btn_login.setOnClickListener {
            activity_login_btn_login.isClickable = false

            verifyUser()
        }
    }

    private fun verifyUser() {
        val email = activity_login_et_email.text.toString()
        val password = activity_login_et_password.text.toString()

        if(email.isEmpty() || password.isEmpty()) {
            toast("Please enter email/password")

            activity_login_btn_login.isClickable = true

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

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        activity_login_et_email.clearFocus()
        activity_login_et_password.clearFocus()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)

        return true
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return activity_login_et_email.hasFocus() || activity_login_et_password.hasFocus()
    }

    override fun onShowPress(e: MotionEvent?) {}
    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean = true
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean = true
    override fun onLongPress(e: MotionEvent?) {}
    override fun onDoubleTap(e: MotionEvent?): Boolean = true
    override fun onDoubleTapEvent(e: MotionEvent?): Boolean = true
    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean = true
}