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
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.createDialog
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.hideKeyboard
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.loadImage
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.longToast
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.showKeyboard
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.startActivity
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

@SuppressLint("InflateParams")
class OptionsActivity : AppCompatActivity() {

    companion object {
        private val TAG = OptionsActivity::class.java.simpleName

        private const val UPDATE_NAME = 1
        private const val UPDATE_IMAGE = 2
    }

    private var uri: Uri? = null
    private var nameDialog: AlertDialog? = null
    private var imageDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        supportActionBar?.title = "User Options"

        displayUserInfo()

        activity_options_btn_change_name.setOnClickListener {
            if(nameDialog == null) {
                nameDialog = createDialog(R.layout.dialog_change_name, this)
                setNameDialogListeners()
            }
            nameDialog?.show()
        }

        activity_options_btn_change_picture.setOnClickListener {
            if(imageDialog == null) {
                imageDialog = createDialog(R.layout.dialog_change_image, this)
                setImageDialogListeners()
            }
            imageDialog?.show()
        }

        activity_options_btn_log_out.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity<AuthUserActivity>(CLEAR_TASK or NEW_TASK)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == RESULT_OK && data != null) {
            uri = data.data
            uri?.let {
                loadImage(it, imageDialog!!.dialog_change_image_img_profile_picture)
            }
            imageDialog!!.dialog_change_image_txt_select_photo.text = getString(R.string.change_image)
        }
    }

    private fun displayUserInfo(mode: Int = 0) {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)

                if(mode != UPDATE_IMAGE) {
                    activity_options_txt_name.text = user?.name
                }

                if(mode != UPDATE_NAME) {
                    if(user?.imgUrl!!.isNotEmpty()) {
                        val imgUri = Uri.parse(user.imgUrl)
                        loadImage(imgUri, activity_options_img_profile_picture)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun setNameDialogListeners() {
        nameDialog?.apply {
            setOnShowListener {
                dialog_change_name_et_new_name.setOnFocusChangeListener { view, b ->
                    // TODO: make keyboard show consistently when name dialog opens

                    if(b) {
                        view.showKeyboard()
                    } else {
                        view.hideKeyboard()
                    }
                }

                dialog_change_name_btn_save.setOnClickListener {
                    val newName = dialog_change_name_et_new_name.text.toString()
                    if(newName.isEmpty()) {
                        toast("Please enter a valid name")
                    } else {
                        val uid = FirebaseAuth.getInstance().uid
                        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(p0: DataSnapshot) {
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

                dialog_change_name_btn_cancel.setOnClickListener {
                    dismiss()
                }

                dialog_change_name_et_new_name.requestFocus()
            }

            setOnCancelListener {
                dialog_change_name_et_new_name.text.clear()
                dialog_change_name_et_new_name.clearFocus()

                dialog_change_name_et_new_name.onFocusChangeListener = null
                dialog_change_name_btn_save.setOnClickListener(null)
                dialog_change_name_btn_cancel.setOnClickListener(null)
            }

            setOnDismissListener {
                dialog_change_name_et_new_name.text.clear()
                dialog_change_name_et_new_name.clearFocus()

                dialog_change_name_et_new_name.onFocusChangeListener = null
                dialog_change_name_btn_save.setOnClickListener(null)
                dialog_change_name_btn_cancel.setOnClickListener(null)
            }
        }
    }

    private fun setImageDialogListeners() {
        imageDialog?.apply {
            setOnShowListener {
                dialog_change_image_img_profile_picture.setOnClickListener {
                    startActivityForResult(Intent(Intent.ACTION_PICK).setType("image/*"), 0)
                }

                dialog_change_image_btn_save.setOnClickListener {
                    dialog_change_image_btn_save.isClickable = false

                    // TODO: prevent multiple clicks in rapid succession

                    if (uri == null) {
                        toast("Please add a photo")

                        dialog_change_image_btn_save.isClickable = true
                    } else {
                        longToast("Updating image...")

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
                                            val user = p0.getValue(User::class.java)
                                            user?.imgUrl = iUri.toString()
                                            uRef.setValue(user)
                                                .addOnSuccessListener {
                                                    Log.i(TAG, "Successfully updated database with new user profile " +
                                                            "image")
                                                    toast("Successfully updated profile picture")

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
                            }
                    }
                }

                dialog_change_image_btn_cancel.setOnClickListener {
                    uri = null

                    val color = ContextCompat.getColor(baseContext, R.color.colorDefault)
                    val cd = ColorDrawable(color)
                    dialog_change_image_img_profile_picture.setImageDrawable(cd)

                    dialog_change_image_txt_select_photo.text = getString(R.string.pick_image)
                    dismiss()
                }
            }

            setOnCancelListener {
                dialog_change_image_img_profile_picture.setOnClickListener(null)
                dialog_change_image_btn_save.setOnClickListener(null)
                dialog_change_image_btn_cancel.setOnClickListener(null)
            }

            setOnDismissListener {
                dialog_change_image_img_profile_picture.setOnClickListener(null)
                dialog_change_image_btn_save.setOnClickListener(null)
                dialog_change_image_btn_cancel.setOnClickListener(null)
            }
        }
    }
}