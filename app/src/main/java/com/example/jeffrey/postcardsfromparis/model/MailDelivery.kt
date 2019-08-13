package com.example.jeffrey.postcardsfromparis.model

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

object MailDelivery {

    private const val WELCOME_IMAGE = "welcome.jpg"
    private const val PFP_PROFILE_PICTURE = "20190104_022430.jpg" // TODO: update this for final location name
    private const val PFP_LOCATION = "Santa Barbara"

    private val TAG = MailDelivery::class.java.simpleName

    fun createWelcomeMessage(user: User, fbc: FirebaseCallback) {
        val frontRef = FirebaseStorage.getInstance().getReference("images/$WELCOME_IMAGE")
        frontRef.downloadUrl.addOnSuccessListener { fUrl ->
            val backRef = FirebaseStorage.getInstance().getReference("images/$PFP_PROFILE_PICTURE")
            backRef.downloadUrl.addOnSuccessListener { bUrl ->
                val pfp = User("", "Postcards From Paris Team", bUrl.toString(), -1)
                val message = "Welcome to Postcards of Paris!"
                val time = System.currentTimeMillis()
                val postcard = Postcard(fUrl.toString(), pfp, PFP_LOCATION, message, time)

                val cardPath = UUID.randomUUID().toString()
                val ref = FirebaseDatabase.getInstance().getReference("postcards/${user.uid}/$cardPath")
                ref.setValue(postcard)
                    .addOnSuccessListener {
                        Log.i(TAG, "Successfully created welcome postcard")

                        fbc.onCallback()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to create welcome postcard")

                        fbc.onCallback()
                    }
            }.addOnFailureListener {
                fbc.onCallback()
            }
        }.addOnFailureListener {
            fbc.onCallback()
        }
    }

    fun distributePostcard(postcard: Postcard, fbc: FirebaseCallback) {
        // TODO: send postcard to each user who hasn't received a postcard in X amount of time (6 hours)
        // TODO: update lastreceived on each user
        // TODO: call fbc.onCallback() at the end of each success/failure listener
        // TODO: this won't work on a large scale but use for now
    }

    interface FirebaseCallback {
        fun onCallback()
    }
}