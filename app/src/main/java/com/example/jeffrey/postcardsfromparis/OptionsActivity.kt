package com.example.jeffrey.postcardsfromparis

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.example.jeffrey.postcardsfromparis.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_options.*
import kotlinx.android.synthetic.main.dialog_change_image.*
import kotlinx.android.synthetic.main.dialog_change_name.*
import java.util.*

@SuppressLint("InflateParams, SetTextI18n")
class OptionsActivity : AppCompatActivity() {

    companion object {
        private val TAG = OptionsActivity::class.java.simpleName

        private const val UPDATE_NAME = 1
        private const val UPDATE_IMAGE = 2
    }

    private var uri: Uri? = null

    private val nameDialog: AlertDialog by lazy {
        val view = layoutInflater.inflate(R.layout.dialog_change_name, null)
        return@lazy AlertDialog.Builder(this).setView(view).create()
    }

    private val imageDialog: AlertDialog by lazy {
        val view = layoutInflater.inflate(R.layout.dialog_change_image, null)
        return@lazy AlertDialog.Builder(this).setView(view).create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        supportActionBar?.title = "User Options"

        displayUserInfo()

        setChangeNameListener()
        setChangeProfilePictureListener()
        setLogOutListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == RESULT_OK && data != null) {
            uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

            imageDialog.dialog_change_image_img_profile_picture.setImageBitmap(bitmap)
            imageDialog.dialog_change_image_txt_select_photo.text = "Change Photo"
        }
    }

    private fun displayUserInfo(mode: Int = 0) {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)

                if(mode != UPDATE_IMAGE) {
                    activity_options_txt_name.text = user?.name
                }

                if(mode != UPDATE_NAME) {
                    if(user?.imgUrl!!.isNotEmpty()) {
                        val imgUri = Uri.parse(user.imgUrl)
                        Picasso.get().load(imgUri).centerCrop().fit().into(activity_options_img_profile_picture)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun setChangeNameListener() {
        activity_options_btn_change_name.setOnClickListener {
            nameDialog.show()

            nameDialog.dialog_change_name_btn_save.setOnClickListener {
                val newName = nameDialog.dialog_change_name_et_new_name.text.toString()
                if(newName.isEmpty()) {
                    Toast.makeText(this@OptionsActivity, "Please enter a valid name", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val uid = FirebaseAuth.getInstance().uid
                    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
                    ref.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val user = p0.getValue(User::class.java)
                            user?.name = newName
                            ref.setValue(user)
                                .addOnSuccessListener {
                                    Log.i(TAG, "Successfully updated user name: ${user?.name}")
                                    Toast.makeText(this@OptionsActivity, "Name successfully updated",
                                        Toast.LENGTH_SHORT).show()

                                    nameDialog.dialog_change_name_et_new_name.text.clear()
                                    nameDialog.dismiss()

                                    displayUserInfo(UPDATE_NAME)
                                }
                                .addOnFailureListener {
                                    Log.e(TAG, "Failed to update user name: ${it.message}")
                                    Toast.makeText(this@OptionsActivity, "Error: ${it.message}",
                                        Toast.LENGTH_SHORT).show()
                                }
                        }

                        override fun onCancelled(p0: DatabaseError) {}
                    })
                }
            }

            nameDialog.dialog_change_name_btn_cancel.setOnClickListener {
                nameDialog.dialog_change_name_et_new_name.text.clear()
                nameDialog.dismiss()
            }
        }
    }

    private fun setChangeProfilePictureListener() {
        activity_options_btn_change_picture.setOnClickListener {
            imageDialog.show()

            imageDialog.dialog_change_image_img_profile_picture.setOnClickListener {
                startActivityForResult(Intent(Intent.ACTION_PICK).setType("image/*"), 0)
            }

            imageDialog.dialog_change_image_btn_save.setOnClickListener {
                // TODO: prevent multiple clicks in rapid succession
                if(uri == null) {
                    Toast.makeText(this, "Please add a photo", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Saving image...", Toast.LENGTH_SHORT).show()

                    val image = UUID.randomUUID().toString()
                    val ref = FirebaseStorage.getInstance().getReference("images/$image")
                    ref.putFile(uri!!)
                        .addOnSuccessListener {
                            Log.i(TAG, "Successfully saved new user profile image to storage: " +
                                    "${it.metadata?.path}")
                            Toast.makeText(this, "Updating profile picture...", Toast.LENGTH_SHORT).show()

                            ref.downloadUrl.addOnSuccessListener { iUri ->
                                Log.i(TAG, "Image file location: $iUri")

                                val uid = FirebaseAuth.getInstance().uid
                                val uRef = FirebaseDatabase.getInstance().getReference("/users/$uid")
                                uRef.addListenerForSingleValueEvent(object: ValueEventListener {
                                    @SuppressLint("SetTextI18n")
                                    override fun onDataChange(p0: DataSnapshot) {
                                        val user = p0.getValue(User::class.java)
                                        user?.imgUrl = iUri.toString()
                                        uRef.setValue(user)
                                            .addOnSuccessListener {
                                                Log.i(TAG, "Successfully updated database with new user profile " +
                                                        "image")
                                                Toast.makeText(this@OptionsActivity, "Successfully " +
                                                        "updated profile picture", Toast.LENGTH_SHORT).show()

                                                uri = null
                                                imageDialog.dialog_change_image_img_profile_picture.setImageDrawable(
                                                        ColorDrawable(Color.parseColor("#7C7C7C")))
                                                imageDialog.dialog_change_image_txt_select_photo.text = "Select Photo"
                                                imageDialog.dismiss()

                                                displayUserInfo(UPDATE_IMAGE)
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(TAG, "Failed to update database with new user profile " +
                                                        "image: ${e.message}")
                                                Toast.makeText(this@OptionsActivity, "Failed to update " +
                                                        "profile picture: {$e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }

                                    override fun onCancelled(p0: DatabaseError) {}
                                })
                            }
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "Failed to update user profile image: ${it.message}")
                            Toast.makeText(this, "Failed to update profile picture: ${it.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                }
            }

            imageDialog.dialog_change_image_btn_cancel.setOnClickListener {
                uri = null
                imageDialog.dialog_change_image_img_profile_picture.setImageDrawable(ColorDrawable(
                    Color.parseColor("#7C7C7C")))
                imageDialog.dialog_change_image_txt_select_photo.text = "Select Photo"
                imageDialog.dismiss()
            }
        }
    }

    private fun setLogOutListener() {
        activity_options_btn_log_out.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthUserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
}