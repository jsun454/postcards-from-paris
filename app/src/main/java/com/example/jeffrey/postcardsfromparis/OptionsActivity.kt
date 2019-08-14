package com.example.jeffrey.postcardsfromparis

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import com.example.jeffrey.postcardsfromparis.model.User
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.CLEAR_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.NEW_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.NO_ANIMATION
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.createDialog
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.hideKeyboard
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.loadImage
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.longToast
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.showKeyboard
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.startActivity
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.startActivityToPickImage
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_options.*
import kotlinx.android.synthetic.main.dialog_change_image.*
import kotlinx.android.synthetic.main.dialog_change_name.*
import java.util.*

/**
 * This activity handles the options menu
 */
@SuppressLint("InflateParams")
class OptionsActivity : AppCompatActivity() {

    companion object {
        private val TAG = OptionsActivity::class.java.simpleName

        // Request code
        private const val PICK_IMAGE = 0

        private const val UPDATE_NAME = 1
        private const val UPDATE_IMAGE = 2
    }

    // Used to store the URI of the postcard picture selected by the user
    private var uri: Uri? = null

    // Dialogs for changing user's name/picture
    private var nameDialog: AlertDialog? = null
    private var imageDialog: AlertDialog? = null

    /**
     * Display the user's current information and set click listeners
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        // If there is no user logged in
        if(FirebaseAuth.getInstance().uid == null) {
            startActivity<AuthUserActivity>(CLEAR_TASK or NEW_TASK or NO_ANIMATION)
        }

        supportActionBar?.title = "User Options"

        displayUserInfo()

        // Open name change dialog
        activity_options_btn_change_name.setOnClickListener {
            if(nameDialog == null) {
                nameDialog = createDialog(R.layout.dialog_change_name, this)
                setNameDialogListeners()
            }
            nameDialog?.show()
        }

        // Open profile picture change dialog
        activity_options_btn_change_picture.setOnClickListener {
            if(imageDialog == null) {
                imageDialog = createDialog(R.layout.dialog_change_image, this)
                setImageDialogListeners()
            }
            imageDialog?.show()
        }

        // Log out button
        activity_options_btn_log_out.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity<AuthUserActivity>(CLEAR_TASK or NEW_TASK)
        }
    }

    /**
     * Handles the results received from the user selecting a profile picture
     *
     * @param requestCode the code used to identify the request
     * @param resultCode the result of the request
     * @param data the intent which contains [data] related to the request
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // If the user selected a picture
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            uri = data.data
            uri?.let {
                loadImage(it, imageDialog!!.dialog_change_image_img_profile_picture)
            }
            imageDialog!!.dialog_change_image_txt_select_photo.text = getString(R.string.change_image)
        }
    }

    /**
     * Updates the user's information being displayed based on what the user just changed
     *
     * @param mode indicates what information needs to be updated
     */
    private fun displayUserInfo(mode: Int = 0) {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)

                // Update user's picture (if the mode is neither [UPDATE_IMAGE] nor [UPDATE_NAME], then update both)
                if(mode != UPDATE_IMAGE) {
                    activity_options_txt_name.text = user?.name
                }

                // Update user's name
                if(mode != UPDATE_NAME) {
                    if(user?.imgUrl?.isNotEmpty() == true) {
                        val imgUri = Uri.parse(user.imgUrl)
                        loadImage(imgUri, activity_options_img_profile_picture)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    /**
     * Set click listeners for the buttons in the name change dialog
     */
    private fun setNameDialogListeners() {
        nameDialog?.apply {
            // When the dialog appears
            setOnShowListener {
                // Handles soft keyboard
                dialog_change_name_et_new_name.setOnFocusChangeListener { view, b ->
                    if(b) {
                        view.showKeyboard()
                    } else {
                        view.hideKeyboard()
                    }
                }

                // Save button
                dialog_change_name_btn_save.setOnClickListener {
                    val newName = dialog_change_name_et_new_name.text.toString()
                    if(newName.isEmpty()) {
                        toast("Please enter a valid name")
                    } else {
                        val uid = FirebaseAuth.getInstance().uid
                        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(p0: DataSnapshot) {
                                // Update the user's name on Firebase
                                val user = p0.getValue(User::class.java)
                                user?.name = newName
                                ref.setValue(user)
                                    .addOnSuccessListener {
                                        Log.i(TAG, "Successfully updated user name: ${user?.name}")
                                        toast("Name successfully updated")

                                        dismiss()

                                        displayUserInfo(UPDATE_NAME)
                                    }
                                    .addOnFailureListener {
                                        Log.e(TAG, "Failed to update user name: ${it.message}")
                                        toast("Error: ${it.message}")
                                    }
                            }

                            override fun onCancelled(p0: DatabaseError) {}
                        })
                    }
                }

                // Cancel button
                dialog_change_name_btn_cancel.setOnClickListener {
                    dismiss()
                }

                dialog_change_name_et_new_name.requestFocus()
            }

            // When dialog is cancelled
            setOnCancelListener {
                dialog_change_name_et_new_name.text.clear()
                dialog_change_name_et_new_name.clearFocus()

                dialog_change_name_et_new_name.onFocusChangeListener = null
                dialog_change_name_btn_save.setOnClickListener(null)
                dialog_change_name_btn_cancel.setOnClickListener(null)
            }

            // When dialog is dismissed
            setOnDismissListener {
                dialog_change_name_et_new_name.text.clear()
                dialog_change_name_et_new_name.clearFocus()

                dialog_change_name_et_new_name.onFocusChangeListener = null
                dialog_change_name_btn_save.setOnClickListener(null)
                dialog_change_name_btn_cancel.setOnClickListener(null)
            }
        }
    }

    /**
     * Set click listeners for the buttons in the profile picture change dialog
     */
    private fun setImageDialogListeners() {
        imageDialog?.apply {
            // When the dialog appears
            setOnShowListener {
                // For the user to choose a profile picture
                dialog_change_image_img_profile_picture.setOnClickListener {
                    startActivityToPickImage(PICK_IMAGE)
                }

                // Save button
                dialog_change_image_btn_save.setOnClickListener {
                    dialog_change_image_btn_save.isClickable = false
                    dialog_change_image_btn_cancel.isClickable = false

                    if (uri == null) {
                        toast("Please add a photo")

                        dialog_change_image_btn_save.isClickable = true
                        dialog_change_image_btn_cancel.isClickable = true
                    } else {
                        longToast("Updating image...")

                        // Store the profile picture
                        val image = UUID.randomUUID().toString()
                        val ref = FirebaseStorage.getInstance().getReference("images/$image")
                        ref.putFile(uri!!)
                            .addOnSuccessListener {
                                Log.i(TAG, "Successfully saved new user profile image to storage: ${it.metadata?.path}")

                                ref.downloadUrl.addOnSuccessListener { iUri ->
                                    Log.i(TAG, "Image file location: $iUri")

                                    val uid = FirebaseAuth.getInstance().uid
                                    val uRef = FirebaseDatabase.getInstance().getReference("users/$uid")
                                    uRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(p0: DataSnapshot) {
                                            // Update the user's image on Firebase
                                            val user = p0.getValue(User::class.java)
                                            user?.imgUrl = iUri.toString()
                                            uRef.setValue(user)
                                                .addOnSuccessListener {
                                                    Log.i(TAG, "Successfully updated database with new user profile " +
                                                            "image")
                                                    toast("Successfully updated profile picture")

                                                    // Reset the dialog to its original state
                                                    uri = null

                                                    val color = ContextCompat.getColor(baseContext,
                                                        R.color.colorDefault)
                                                    val cd = ColorDrawable(color)
                                                    dialog_change_image_img_profile_picture.setImageDrawable(cd)

                                                    dialog_change_image_txt_select_photo.text =
                                                        getString(R.string.pick_image)
                                                    dismiss()

                                                    displayUserInfo(UPDATE_IMAGE)
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(TAG, "Failed to update database with new user profile " +
                                                            "image: ${e.message}")
                                                    toast("Failed to update profile picture: {$e.message}")

                                                    dialog_change_image_btn_save.isClickable = true
                                                    dialog_change_image_btn_cancel.isClickable = true
                                                }
                                        }

                                        override fun onCancelled(p0: DatabaseError) {}
                                    })
                                }
                            }
                            .addOnFailureListener {
                                Log.e(TAG, "Failed to update user profile image: ${it.message}")
                                toast("Failed to update profile picture: ${it.message}")

                                dialog_change_image_btn_save.isClickable = true
                                dialog_change_image_btn_cancel.isClickable = true
                            }
                    }
                }

                // Cancel button
                dialog_change_image_btn_cancel.setOnClickListener {
                    // Reset the dialog to its original state
                    uri = null

                    val color = ContextCompat.getColor(baseContext, R.color.colorDefault)
                    val cd = ColorDrawable(color)
                    dialog_change_image_img_profile_picture.setImageDrawable(cd)

                    dialog_change_image_txt_select_photo.text = getString(R.string.pick_image)
                    dismiss()
                }
            }

            // When dialog is cancelled
            setOnCancelListener {
                uri = null

                val color = ContextCompat.getColor(baseContext, R.color.colorDefault)
                val cd = ColorDrawable(color)
                dialog_change_image_img_profile_picture.setImageDrawable(cd)

                dialog_change_image_txt_select_photo.text = getString(R.string.pick_image)

                dialog_change_image_img_profile_picture.setOnClickListener(null)
                dialog_change_image_btn_save.setOnClickListener(null)
                dialog_change_image_btn_cancel.setOnClickListener(null)
            }

            // When dialog is dismissed
            setOnDismissListener {
                uri = null

                val color = ContextCompat.getColor(baseContext, R.color.colorDefault)
                val cd = ColorDrawable(color)
                dialog_change_image_img_profile_picture.setImageDrawable(cd)

                dialog_change_image_txt_select_photo.text = getString(R.string.pick_image)

                dialog_change_image_img_profile_picture.setOnClickListener(null)
                dialog_change_image_btn_save.setOnClickListener(null)
                dialog_change_image_btn_cancel.setOnClickListener(null)
            }
        }
    }
}