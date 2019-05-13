package com.example.jeffrey.postcardsfromparis

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.inputmethod.InputMethodManager
import com.example.jeffrey.postcardsfromparis.model.User
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_new_postcard.*

class NewPostcardActivity : AppCompatActivity() {

    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_postcard)

        supportActionBar?.title = "New Postcard"

        displayUserInfo()

        activity_new_postcard_img_postcard_picture.setOnClickListener {
            activity_new_postcard_et_postcard_message.clearFocus()

            startActivityForResult(Intent(Intent.ACTION_PICK).setType("image/*"), 0)
        }

        activity_new_postcard_et_postcard_message.setOnFocusChangeListener { view, b ->
            if(!b) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        activity_new_postcard_btn_send.setOnClickListener {
            sendMessage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == RESULT_OK && data != null) {
            uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

            activity_new_postcard_img_postcard_picture.setImageBitmap(bitmap)
            activity_new_postcard_txt_select_photo.text = getString(R.string.change_image)
        }
    }

    private fun displayUserInfo() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)

                activity_new_postcard_txt_name.text = user?.name

                // TODO: display user's location
                activity_new_postcard_txt_location.text = ""

                if(user?.imgUrl!!.isNotEmpty()) {
                    val imgUri = Uri.parse(user.imgUrl)
                    Picasso.get().load(imgUri).centerCrop().fit().into(activity_new_postcard_img_profile_picture)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun sendMessage() {
        val message = activity_new_postcard_et_postcard_message.text.toString()
        if(message.isEmpty()) {
            toast("Message cannot be empty")
            return
        }

        if(uri == null) {
            toast("Please add a picture")
            return
        }

        // TODO: save postcard if all fields are valid
    }
}