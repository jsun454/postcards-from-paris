package com.example.jeffrey.postcardsfromparis

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.MotionEvent
import com.example.jeffrey.postcardsfromparis.model.MailDelivery
import com.example.jeffrey.postcardsfromparis.model.MailDelivery.createWelcomeMessage
import com.example.jeffrey.postcardsfromparis.model.User
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.CLEAR_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.NEW_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.hideKeyboard
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.longToast
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.startActivity
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.toast
import com.example.jeffrey.postcardsfromparis.util.SingleTapGestureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

/**
 * This activity handles the user registration process
 */
class RegisterActivity : AppCompatActivity(), SingleTapGestureListener {

    companion object {
        private val TAG = RegisterActivity::class.java.simpleName

        // Character limit for user's name
        const val MAX_NAME_LENGTH = 16
    }

    private lateinit var detector: GestureDetectorCompat

    /**
     * Sets title and click listeners
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.title = "Register"

        // Sets listeners for background taps
        detector = GestureDetectorCompat(this, this)
        detector.setOnDoubleTapListener(this)

        activity_register_sv_background.setOnTouchListener { _, event ->
            detector.onTouchEvent(event)
        }

        // Register button
        activity_register_btn_register.setOnClickListener {
            activity_register_btn_register.isClickable = false

            registerUser()
        }
    }

    /**
     * Registers user and displays explanation message if registration fails
     */
    private fun registerUser() {
        val name = activity_register_et_name.text.toString()
        val email = activity_register_et_email.text.toString()
        val password = activity_register_et_password.text.toString()

        // If fields are left blank
        if(name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            toast("Please enter name/email/password")

            activity_register_btn_register.isClickable = true

            return
        }

        // Limit name length
        if(name.length > MAX_NAME_LENGTH) {
            toast("Name cannot be longer than $MAX_NAME_LENGTH characters")

            activity_register_btn_register.isClickable = true

            return
        }

        // Register user
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully created user with ID: ${it.user.uid}")
                longToast("Creating user...")

                // Save corresponding user object to database
                val user = User(it.user.uid, name, "", 0L)
                val ref = FirebaseDatabase.getInstance().getReference("users/${it.user.uid}")
                ref.setValue(user)
                    .addOnSuccessListener {
                        Log.i(TAG, "Successfully saved user to database")

                        // Send the new user a welcome postcard
                        createWelcomeMessage(user, object: MailDelivery.FirebaseCallback {
                            override fun onCallback() {
                                startActivity<NewUserImageActivity>(CLEAR_TASK or NEW_TASK)
                            }

                            override fun onCallback(success: Boolean) {}
                        })
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to save user to database: ${e.message}")
                        toast("Error: ${e.message}")
                    }
            }
            .addOnFailureListener {
                Log.w(TAG, "Failed to create user: ${it.message}")
                toast("Registration failed: ${it.message}")

                activity_register_btn_register.isClickable = true
            }
    }

    /**
     * Handles background taps
     *
     * @param e unused parameter in this implementation
     * @return that the motion event was consumed
     */
    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        activity_register_et_name.clearFocus()
        activity_register_et_email.clearFocus()
        activity_register_et_password.clearFocus()

        window.decorView.rootView.hideKeyboard()

        return true
    }

    /**
     * Consumes the motion event if either input field has focus
     *
     * @param e unused parameter in this implementation
     * @return whether the motion event was consumed
     */
    override fun onDown(e: MotionEvent?): Boolean {
        return activity_register_et_name.hasFocus() || activity_register_et_email.hasFocus() ||
                activity_register_et_password.hasFocus()
    }
}
