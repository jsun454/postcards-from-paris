package com.example.jeffrey.postcardsfromparis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.example.jeffrey.postcardsfromparis.model.MailDelivery
import com.example.jeffrey.postcardsfromparis.model.MailDelivery.distributePostcard
import com.example.jeffrey.postcardsfromparis.model.Postcard
import com.example.jeffrey.postcardsfromparis.model.User
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.CLEAR_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.NEW_TASK
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.NO_ANIMATION
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.hideKeyboard
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
import kotlinx.android.synthetic.main.activity_new_postcard.*
import java.util.*

/**
 * This activity lets the user create a new postcard and send it to other users
 */
class NewPostcardActivity : AppCompatActivity() {

    companion object {
        private val TAG = NewPostcardActivity::class.java.simpleName

        // Request codes
        private const val PICK_IMAGE = 0
        private const val REQUEST_LOCATION = 1

        // Character limit for postcard message
        private const val MAX_MESSAGE_LENGTH = 300

        // Extra to pass back to MailboxActivity
        const val RETURN_TO_SENT_TAB = "sent"
    }

    // Used to store the URI of the postcard picture selected by the user
    private var uri: Uri? = null

    /**
     * Display the user's information and set click listeners
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_postcard)

        // If there is no user logged in
        if(FirebaseAuth.getInstance().uid == null) {
            startActivity<AuthUserActivity>(CLEAR_TASK or NEW_TASK or NO_ANIMATION)
        }

        supportActionBar?.title = "New Postcard"

        displayUserInfo()

        // For the user to choose a postcard picture
        activity_new_postcard_img_postcard_picture.setOnClickListener {
            activity_new_postcard_et_postcard_message.clearFocus()

            startActivityToPickImage(PICK_IMAGE)
        }

        // Handles background taps
        activity_new_postcard_et_postcard_message.setOnFocusChangeListener { view, b ->
            if(!b) {
                view.hideKeyboard()
            }
        }

        // Send button
        activity_new_postcard_btn_send.setOnClickListener {
            activity_new_postcard_btn_send.isClickable = false
            activity_new_postcard_et_postcard_message.isFocusable = false
            activity_new_postcard_img_postcard_picture.isClickable = false

            sendMessage()
        }
    }

    /**
     * Handles the results received from the user selecting a postcard picture
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
                loadImage(it, activity_new_postcard_img_postcard_picture)
            }

            activity_new_postcard_txt_select_photo.text = getString(R.string.change_image)
        }
    }

    /**
     * Handles the results of requesting the user's location
     *
     * @param requestCode the code used to identify the request
     * @param permissions unused parameter in this implementation
     * @param grantResults unused parameter in this implementation
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_LOCATION) {
            activity_new_postcard_txt_location.text = getLocation()
        }
    }

    /**
     * Displays the user's information on the postcard template
     */
    private fun displayUserInfo() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)

                activity_new_postcard_txt_name.text = user?.name

                // Display location if permission has been granted, request location otherwise
                val locationAccess = ContextCompat.checkSelfPermission(this@NewPostcardActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                if(locationAccess == PackageManager.PERMISSION_GRANTED) {
                    activity_new_postcard_txt_location.text = getLocation()
                } else {
                    val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
                    ActivityCompat.requestPermissions(this@NewPostcardActivity, permissions, REQUEST_LOCATION)
                }

                if(user?.imgUrl?.isNotEmpty() == true) {
                    val imgUri = Uri.parse(user.imgUrl)
                    loadImage(imgUri, activity_new_postcard_img_profile_picture)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })

        // Handles the case where the user updated his/her profile picture immediately before coming to this
        // activity, and the picture just finished updating
        ref.addValueEventListener(object: ValueEventListener {
            // Used to detect if the user's profile picture has updated.
            var changes = 0

            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)
                if(user?.imgUrl?.isNotEmpty() == true) {
                    val imgUri = Uri.parse(user.imgUrl)
                    loadImage(imgUri, activity_new_postcard_img_profile_picture)
                }

                // Once the new picture is being displayed, remove this listener
                if(++changes == 2) {
                    ref.removeEventListener(this)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    /**
     * Tries to obtain the user's location
     *
     * @return the user's location (city) if location permissions have been granted
     */
    private fun getLocation(): String {
        val locationAccess = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if(locationAccess == PackageManager.PERMISSION_GRANTED) {
            val lm = getSystemService(LOCATION_SERVICE) as LocationManager
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

    /**
     * Attempts to send the user's postcard, displays an explanation message if the message cannot be sent
     */
    private fun sendMessage() {
        val message = activity_new_postcard_et_postcard_message.text.toString()

        if(message.isEmpty()) {
            toast("Message cannot be empty")

            activity_new_postcard_btn_send.isClickable = true
            activity_new_postcard_et_postcard_message.isFocusable = true
            activity_new_postcard_img_postcard_picture.isClickable = true

            return
        }

        if(message.length > MAX_MESSAGE_LENGTH) {
            toast("Message cannot be longer than $MAX_MESSAGE_LENGTH characters")

            activity_new_postcard_btn_send.isClickable = true
            activity_new_postcard_et_postcard_message.isFocusable = true
            activity_new_postcard_img_postcard_picture.isClickable = true

            return
        }

        if(uri == null) {
            toast("Please add a picture")

            activity_new_postcard_btn_send.isClickable = true
            activity_new_postcard_et_postcard_message.isFocusable = true
            activity_new_postcard_img_postcard_picture.isClickable = true

            return
        }

        longToast("Sending...")

        // Store the postcard picture
        val image = UUID.randomUUID().toString()
        val iRef = FirebaseStorage.getInstance().getReference("images/$image")
        iRef.putFile(uri!!)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully saved postcard image to storage: ${it.metadata?.path}")

                iRef.downloadUrl.addOnSuccessListener { dUrl ->
                    Log.i(TAG, "Image file location: $dUrl")

                    val uid = FirebaseAuth.getInstance().uid
                    val uRef = FirebaseDatabase.getInstance().getReference("users/$uid")
                    uRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val user = p0.getValue(User::class.java) ?: return
                            val location = activity_new_postcard_txt_location.text.toString()
                            val time = System.currentTimeMillis()

                            uRef.setValue(user)

                            // Creates the postcard object
                            val postcard = Postcard(dUrl.toString(), user, location, message, time)

                            // Saves the postcard to Firebase
                            val cardPath = UUID.randomUUID().toString()
                            val ref = FirebaseDatabase.getInstance().getReference("postcards/$uid/$cardPath")
                            ref.setValue(postcard)
                                .addOnSuccessListener {
                                    Log.i(TAG, "Successfully uploaded postcard to database")

                                    // Sends the postcard to other users
                                    distributePostcard(postcard, object: MailDelivery.FirebaseCallback {
                                        override fun onCallback(success: Boolean) {
                                            if(success) {
                                                toast("Postcard sent!")
                                            } else {
                                                toast("Unable to send postcard. Please try again later.")
                                            }

                                            val intent = Intent()
                                            intent.putExtra(RETURN_TO_SENT_TAB, true)
                                            setResult(RESULT_OK, intent)
                                            finish()
                                        }

                                        override fun onCallback() {}
                                    })
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Failed to upload postcard to database")
                                    toast("Error: ${e.message}")

                                    activity_new_postcard_btn_send.isClickable = true
                                    activity_new_postcard_et_postcard_message.isFocusable = true
                                    activity_new_postcard_img_postcard_picture.isClickable = true
                                }
                        }

                        override fun onCancelled(p0: DatabaseError) {}
                    })
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to save postcard image to storage: ${it.message}")
                toast("Failed to send postcard: ${it.message}")

                activity_new_postcard_btn_send.isClickable = true
                activity_new_postcard_et_postcard_message.isFocusable = true
                activity_new_postcard_img_postcard_picture.isClickable = true
            }
    }
}