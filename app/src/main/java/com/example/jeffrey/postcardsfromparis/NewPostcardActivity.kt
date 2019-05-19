package com.example.jeffrey.postcardsfromparis

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.example.jeffrey.postcardsfromparis.model.User
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.hideKeyboard
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.loadImage
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_new_postcard.*
import java.util.*

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
                view.hideKeyboard()
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
            uri?.let {
                loadImage(it, activity_new_postcard_img_postcard_picture)
            }

            activity_new_postcard_txt_select_photo.text = getString(R.string.change_image)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 0) {
            activity_new_postcard_txt_location.text = getLocation()
        }
    }

    private fun displayUserInfo() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)

                activity_new_postcard_txt_name.text = user?.name

                if(ContextCompat.checkSelfPermission(this@NewPostcardActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    activity_new_postcard_txt_location.text = getLocation()
                } else {
                    ActivityCompat.requestPermissions(this@NewPostcardActivity,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 0)
                }

                if(user?.imgUrl!!.isNotEmpty()) {
                    val imgUri = Uri.parse(user.imgUrl)
                    loadImage(imgUri, activity_new_postcard_img_profile_picture)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun getLocation(): String {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val here = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val gcd = Geocoder(this, Locale.getDefault())
            val addresses = gcd.getFromLocation(here.latitude, here.longitude, 1)
            if(addresses != null && addresses.isNotEmpty() && addresses[0].locality != null) {
                return addresses[0].locality
            }
        }

        toast("Unable to access current location")

        return "Unknown"
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