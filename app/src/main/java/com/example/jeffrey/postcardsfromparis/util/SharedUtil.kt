package com.example.jeffrey.postcardsfromparis.util

import android.content.Context
import android.content.Intent
import android.widget.Toast

object SharedUtil {

    const val CLEAR_TASK = Intent.FLAG_ACTIVITY_CLEAR_TASK
    const val NEW_TASK = Intent.FLAG_ACTIVITY_NEW_TASK
    const val NO_ANIMATION = Intent.FLAG_ACTIVITY_NO_ANIMATION

    inline fun <reified T: Any> Context.startActivity(flags: Int) {
        val intent = Intent(this, T::class.java)
        intent.flags = flags
        startActivity(intent)
    }

    fun Context.toast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    fun Context.longToast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    // TODO: add extension functions to show/hide keyboard
}