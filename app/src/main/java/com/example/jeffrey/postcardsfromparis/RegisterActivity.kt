package com.example.jeffrey.postcardsfromparis

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.jeffrey.postcardsfromparis.model.User
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.CLEAR_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.NEW_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.longToast
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.startActivity
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    companion object {
        private val TAG = RegisterActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.title = "Registration"

        activity_register_btn_register.setOnClickListener {
            // TODO: prevent multiple clicks in rapid succession
            registerUser()
        }
    }

    private fun registerUser() {
        val name = activity_register_et_name.text.toString()
        val email = activity_register_et_email.text.toString()
        val password = activity_register_et_password.text.toString()

        if(name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            toast("Please enter name/email/password")
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully created user with ID: ${it.user.uid}")
                longToast("Creating user...")

                val user = User(it.user.uid, name, "")
                val ref = FirebaseDatabase.getInstance().getReference("/users/${it.user.uid}")
                ref.setValue(user)
                    .addOnSuccessListener {
                        Log.i(TAG, "Successfully saved user to database")

                        startActivity<NewUserImageActivity>(CLEAR_TASK or NEW_TASK)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to save user to database: ${e.message}")
                        toast("Error: {$e.message}")
                    }
            }
            .addOnFailureListener {
                Log.w(TAG, "Failed to create user: ${it.message}")
                toast("Registration failed: ${it.message}")
            }
    }
}
