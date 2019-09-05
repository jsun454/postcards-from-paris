package com.example.jeffrey.postcardsfromparis.util

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import com.example.jeffrey.postcardsfromparis.R
import com.squareup.picasso.Picasso

/**
 * This object provides various functions that can be reused frequently across this project
 */
object SharedUtil {

    // Activity flags
    const val CLEAR_TASK = Intent.FLAG_ACTIVITY_CLEAR_TASK
    const val NEW_TASK = Intent.FLAG_ACTIVITY_NEW_TASK
    const val NO_ANIMATION = Intent.FLAG_ACTIVITY_NO_ANIMATION

    /**
     * Creates a popup dialog
     *
     * @param resource the ID of the layout resource to inflate
     * @param context the context associated with this dialog
     * @return the dialog that was created
     */
    fun Activity.createDialog(resource: Int, context: Context): AlertDialog {
        val view = layoutInflater.inflate(resource, null)
        return AlertDialog.Builder(context).setView(view).create()
    }

    /**
     * Lets the user select an image
     *
     * @param requestCode a request code to identify this request
     */
    fun Activity.startActivityToPickImage(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, requestCode)
    }

    /**
     * Start an activity to obtain a result
     *
     * @param T the activity to start
     * @param requestCode a request code to identify this request
     */
    inline fun <reified T: Any> Activity.startActivityForResult(requestCode: Int) {
        val intent = Intent(this, T::class.java)
        startActivityForResult(intent, requestCode)
    }

    /**
     * Start an activity with a set of [flags]
     *
     * @param T the activity to start
     * @param flags the flags to start the activity with
     */
    inline fun <reified T: Any> Context.startActivity(flags: Int) {
        val intent = Intent(this, T::class.java)
        intent.flags = flags
        startActivity(intent)
    }

    /**
     * Makes a short toast [message]
     *
     * @param message the message to show
     */
    fun Context.toast(message: CharSequence) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        val view = toast.view
        view.background.setColorFilter(ContextCompat.getColor(this, R.color.colorToast),
            PorterDuff.Mode.SRC_IN)
        toast.show()
    }

    /**
     * Makes a long toast [message]
     *
     * @param message the message to show
     */
    fun Context.longToast(message: CharSequence) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        val view = toast.view
        view.background.setColorFilter(ContextCompat.getColor(this, R.color.colorToast),
            PorterDuff.Mode.SRC_IN)
        toast.show()
    }

    /**
     * Opens the soft keyboard
     */
    fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * Hides the soft keyboard
     */
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(applicationWindowToken, 0)
    }

    /**
     * Displays an image using Picasso
     *
     * @param uri the URI of the image
     * @param view the view to display the image in
     */
    fun loadImage(uri: Uri, view: ImageView) {
        Picasso.get().load(uri).centerCrop().fit().into(view)
    }
}