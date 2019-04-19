package com.example.jeffrey.postcardsfromparis

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_options.*

class OptionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        activity_options_btn_change_name.setOnClickListener {
            // TODO: open fragment to change name, add current name to res view
        }

        activity_options_btn_change_picture.setOnClickListener {
            // TODO: start activity for result (image), add current picture to res view
        }

        activity_options_btn_log_out.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthUserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
}
