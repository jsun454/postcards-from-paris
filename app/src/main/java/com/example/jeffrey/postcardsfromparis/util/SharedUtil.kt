package com.example.jeffrey.postcardsfromparis.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import com.squareup.picasso.Picasso

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

    fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(applicationWindowToken, 0)
    }

    fun loadImage(uri: Uri, view: ImageView) {
        Picasso.get().load(uri).centerCrop().fit().into(view)
    }
}