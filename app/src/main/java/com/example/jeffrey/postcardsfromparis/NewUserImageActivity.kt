package com.example.jeffrey.postcardsfromparis

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.jeffrey.postcardsfromparis.model.User
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.CLEAR_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.NEW_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.loadImage
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.longToast
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.startActivity
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.startActivityToPickImage
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_new_user_image.*
import java.util.*

/**
 * This activity lets new users choose a profile picture right after creating an account
 */
class NewUserImageActivity : AppCompatActivity() {

    companion object {
        private val TAG = NewUserImageActivity::class.java.simpleName

        // Request code
        private const val PICK_IMAGE = 0
    }

    // Used to store the URI of the profile picture selected by the user
    private var uri: Uri? = null

    /**
     * Sets button click listeners
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_user_image)

        supportActionBar?.title = "Add a photo"

        // For the user to choose a profile picture
        activity_new_user_image_img_profile_picture.setOnClickListener {
            startActivityToPickImage(PICK_IMAGE)
        }

        // Button to save selected photo
        activity_new_user_image_btn_use_photo.setOnClickListener {
            activity_new_user_image_btn_use_photo.isClickable = false
            activity_new_user_image_txt_skip.isClickable = false

            saveImage()
        }

        // If the user decides to skip choosing a photo for now
        activity_new_user_image_txt_skip.setOnClickListener {
            activity_new_user_image_btn_use_photo.isClickable = false
            activity_new_user_image_txt_skip.isClickable = false

            startActivity<MailboxActivity>(CLEAR_TASK or NEW_TASK)
        }
    }

    /**
     * Handles the results received from the user selecting a profile picture
     *
     * @param requestCode the code used to identify the request
     * @param resultCode the result of the request
     * @param data the intent which contains [data] related to the result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // If the user selected a picture
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            uri = data.data
            uri?.let {
                loadImage(it, activity_new_user_image_img_profile_picture)
            }

            activity_new_user_image_txt_select_photo.text = getString(R.string.change_image)
        }
    }

    /**
     * Saves the user's image
     */
    private fun saveImage() {
        if(uri == null) {
            toast("Please add a photo first")

            activity_new_user_image_btn_use_photo.isClickable = true
            activity_new_user_image_txt_skip.isClickable = true

            return
        }

        longToast("Saving image...")

        // Store the profile picture
        val image = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("images/$image")
        ref.putFile(uri!!)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully saved user profile image to storage: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener { dUrl ->
                    Log.i(TAG, "Image file location: $dUrl")

                    val uid = FirebaseAuth.getInstance().uid
                    val uRef = FirebaseDatabase.getInstance().getReference("users/$uid")
                    uRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            // Update the user's image on Firebase
                            val user = p0.getValue(User::class.java)
                            user?.imgUrl = dUrl.toString()
                            uRef.setValue(user)
                                .addOnSuccessListener {
                                    Log.i(TAG, "Successfully updated database with new user profile image")
                                    toast("Successfully saved image")

                                    startActivity<MailboxActivity>(CLEAR_TASK or NEW_TASK)
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Failed to update database with new user profile image: " +
                                            "${e.message}")
                                    toast("Error: ${e.message}")

                                    activity_new_user_image_btn_use_photo.isClickable = true
                                    activity_new_user_image_txt_skip.isClickable = true
                                }
                        }

                        override fun onCancelled(p0: DatabaseError) {}
                    })
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to save user profile image to storage: ${it.message}")
                toast("Failed to save image: ${it.message}")

                activity_new_user_image_btn_use_photo.isClickable = true
                activity_new_user_image_txt_skip.isClickable = true
            }
    }
}
