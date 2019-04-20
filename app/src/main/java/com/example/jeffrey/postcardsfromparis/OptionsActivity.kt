package com.example.jeffrey.postcardsfromparis

import android.annotation.SuppressLint
import android.content.Intent
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
import kotlinx.android.synthetic.main.activity_options.*
import kotlinx.android.synthetic.main.dialog_change_image.*
import kotlinx.android.synthetic.main.dialog_change_name.*
import java.util.*

class OptionsActivity : AppCompatActivity() {

    companion object {
        private val TAG = OptionsActivity::class.java.simpleName
    }

    private var uri: Uri? = null
    private lateinit var nameDialog: AlertDialog
    private lateinit var imageDialog: AlertDialog

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        supportActionBar?.title = "User Options"

        activity_options_btn_change_name.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.dialog_change_name, null)
            nameDialog = AlertDialog.Builder(this).setView(view).create()
            nameDialog.show()

            nameDialog.dialog_change_name_btn_save.setOnClickListener {
                val newName = nameDialog.dialog_change_name_et_new_name.text.toString()
                if(newName.isEmpty()) {
                    Toast.makeText(this@OptionsActivity, "Please enter a name", Toast.LENGTH_SHORT).show()
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
                                }
                                .addOnFailureListener {
                                    Log.e(TAG, "Failed to update user name: ${it.message}")
                                    Toast.makeText(this@OptionsActivity, "Error: ${it.message}",
                                            Toast.LENGTH_SHORT).show()
                                }
                        }

                        override fun onCancelled(p0: DatabaseError) {}
                    })
                    nameDialog.dismiss()
                }
            }

            nameDialog.dialog_change_name_btn_cancel.setOnClickListener {
                nameDialog.dismiss()
            }
        }

        activity_options_btn_change_picture.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.dialog_change_image, null)
            imageDialog = AlertDialog.Builder(this).setView(view).create()
            imageDialog.show()

            imageDialog.dialog_change_image_img_profile_picture.setOnClickListener {
                startActivityForResult(Intent(Intent.ACTION_PICK).setType("image/*"), 0)
            }

            imageDialog.dialog_change_image_btn_save.setOnClickListener {
                if(uri == null) {
                    Toast.makeText(this, "Please add a photo", Toast.LENGTH_SHORT).show()
                } else {
                    val image = UUID.randomUUID().toString()
                    val ref = FirebaseStorage.getInstance().getReference("images/$image")
                    ref.putFile(uri!!)
                        .addOnSuccessListener {
                            Log.i(TAG, "Successfully saved new user profile image to storage: " +
                                    "${it.metadata?.path}")
                            Toast.makeText(this, "Updating profile picture...", Toast.LENGTH_SHORT).show()

                            ref.downloadUrl.addOnSuccessListener { uri ->
                                Log.i(TAG, "Image file location: $uri")

                                val uid = FirebaseAuth.getInstance().uid
                                val uRef = FirebaseDatabase.getInstance().getReference("/users/$uid")
                                uRef.addListenerForSingleValueEvent(object: ValueEventListener {
                                    override fun onDataChange(p0: DataSnapshot) {
                                        val user = p0.getValue(User::class.java)
                                        user?.imgUrl = uri.toString()
                                        uRef.setValue(user)
                                            .addOnSuccessListener {
                                                Log.i(TAG, "Successfully updated database with new user profile " +
                                                        "image")
                                                Toast.makeText(this@OptionsActivity, "Successfully " +
                                                        "updated profile picture", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {

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

                    imageDialog.dismiss()
                }
            }

            imageDialog.dialog_change_image_btn_cancel.setOnClickListener {
                imageDialog.dismiss()
            }
        }

        activity_options_btn_log_out.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthUserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == RESULT_OK && data != null) {
            uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

            if(this::imageDialog.isInitialized) {
                imageDialog.dialog_change_image_img_profile_picture.setImageBitmap(bitmap)
                imageDialog.dialog_change_image_txt_select_photo.text = "Change Photo"
            }
        }
    }
}