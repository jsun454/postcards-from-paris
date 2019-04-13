package com.example.jeffrey.postcardsfromparis

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.jeffrey.postcardsfromparis.model.User
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
            registerUser()
        }
    }

    private fun registerUser() {
        synchronized(this) {
            val name = activity_register_et_name.text.toString()
            val email = activity_register_et_email.text.toString()
            val password = activity_register_et_password.text.toString()

            if(name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter name/email/password", Toast.LENGTH_SHORT).show()
                return
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Log.i(TAG, "Successfully created user with ID: ${it.user.uid}")
                    Toast.makeText(this, "Creating user...", Toast.LENGTH_SHORT).show()

                    val user = User(it.user.uid, name, "")
                    val ref = FirebaseDatabase.getInstance().getReference("/users/${it.user.uid}")
                    ref.setValue(user)
                        .addOnSuccessListener {
                            Log.i(TAG, "Successfully saved user to database")

                            val intent = Intent(this, MailboxActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "Failed to save user to database: ${it.message}")
                        }
                }
                .addOnFailureListener {
                    Log.w(TAG, "Failed to create user: ${it.message}")
                    Toast.makeText(this, "Registration failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
