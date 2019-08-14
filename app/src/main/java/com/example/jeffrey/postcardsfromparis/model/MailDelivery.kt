package com.example.jeffrey.postcardsfromparis.model

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.*

object MailDelivery {

    private const val WELCOME_IMAGE = "welcome.jpg"
    private const val PFP_PROFILE_PICTURE = "20190104_022430.jpg" // TODO: update this for final location name
    private const val PFP_LOCATION = "Santa Barbara"
    private const val MAX_RECIPIENTS = 10

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
                    .addOnFailureListener {
                        Log.e(TAG, "Failed to create welcome postcard: ${it.message}")

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
        var numRecipients = 0
        var success = false
        var done = false

        val ref = FirebaseDatabase.getInstance().getReference("users").orderByChild("lastReceived")
            .limitToFirst(MAX_RECIPIENTS)
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                if(done) return

                ++numRecipients

                val user = p0.getValue(User::class.java) ?: return
                val uid = user.uid
                if(uid == postcard.author.uid) {
                    --numRecipients
                    return
                }

                val cardPath = UUID.randomUUID().toString()
                val cardRef = FirebaseDatabase.getInstance().getReference("postcards/$uid/$cardPath")
                cardRef.setValue(postcard)
                    .addOnSuccessListener {
                        Log.i(TAG, "Successfully delivered postcard to user")

                        --numRecipients
                        success = true

                        if(numRecipients == 0) {
                            fbc.onCallback(success)
                            ref.removeEventListener(this)
                        }
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Failed to deliver postcard to user")

                        --numRecipients

                        if(numRecipients == 0) {
                            fbc.onCallback(success)
                        }
                    }
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
        })

        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(numRecipients == 0) {
                    done = true
                    fbc.onCallback(success)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    interface FirebaseCallback {
        fun onCallback()
        fun onCallback(success: Boolean)
    }
}