package com.example.jeffrey.postcardsfromparis.model

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.*

/**
 * This object handles the distribution of postcards to other users and updates Firebase accordingly
 */
object MailDelivery {

    private val TAG = MailDelivery::class.java.simpleName

    // Constants for creating the welcome postcard that is sent to every new user
    private const val WELCOME_IMAGE = "welcome.jpg"
    private const val PFP_PROFILE_PICTURE = "20190104_022430.jpg" // TODO: update this for final location name
    private const val PFP_LOCATION = "Santa Barbara"

    // How many users a regular postcard can be sent to
    private const val MAX_RECIPIENTS = 10

    /**
     * Sends a welcome postcard to the [user]
     *
     * @param user the user who will receive the postcard
     * @param cb the object whose callback function this function will call once it finishes writing to Firebase
     */
    fun createWelcomeMessage(user: User, cb: FirebaseCallback) {
        // Gets a reference to the postcard picture
        val frontRef = FirebaseStorage.getInstance().getReference("images/$WELCOME_IMAGE")
        frontRef.downloadUrl.addOnSuccessListener { fUrl ->
            // Gets a reference to the stamp picture
            val backRef = FirebaseStorage.getInstance().getReference("images/$PFP_PROFILE_PICTURE")
            backRef.downloadUrl.addOnSuccessListener { bUrl ->
                // Create the welcome postcard
                val pfp = User("", "Postcards From Paris Team", bUrl.toString(), -1)
                val message = "Welcome to Postcards of Paris!"
                val time = System.currentTimeMillis()
                val postcard = Postcard(fUrl.toString(), pfp, PFP_LOCATION, message, time)

                // Send the postcard to the user
                val cardPath = UUID.randomUUID().toString()
                val ref = FirebaseDatabase.getInstance().getReference("postcards/${user.uid}/$cardPath")
                ref.setValue(postcard)
                    .addOnSuccessListener {
                        Log.i(TAG, "Successfully created welcome postcard")

                        // Signals that this function has completed
                        cb.onCallback()
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Failed to create welcome postcard: ${it.message}")

                        cb.onCallback()
                    }
            }.addOnFailureListener {
                cb.onCallback()
            }
        }.addOnFailureListener {
            cb.onCallback()
        }
    }

    /**
     * Distributes a [postcard] to various users
     *
     * @param postcard the postcard to be distributed
     * @param cb the object whose callback function this function will call once it finishes writing to Firebase
     */
    fun distributePostcard(postcard: Postcard, cb: FirebaseCallback) {
        // Tracks the number of remaining users who should receive the postcard. Should start and end at 0.
        var numRecipients = 0

        // Tracks whether at least one user has received the postcard
        var success = false

        // Tracks whether the distribution has been completed (doesn't matter if it was successful or not)
        var done = false

        // Gets a reference to the set of users who haven't received a postcard in the longest amount of time
        val ref = FirebaseDatabase.getInstance().getReference("users").orderByChild("lastReceived")
            .limitToFirst(MAX_RECIPIENTS)
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                // Prevents infinite distribution
                if(success) {
                    return
                }

                // Handles the weird case where the postcard is distributed much later when it isn't supposed to be
                if(done) {
                    ref.removeEventListener(this)

                    return
                }

                // Add a user to the recipient list. This line should be executed from each Firebase reference child
                // before any actual distribution occurs. Once distribution starts [numRecipients] will decrease back
                // down to 0.
                ++numRecipients

                val user = p0.getValue(User::class.java)
                val uid = user?.uid

                // Do not send the postcard if the recipient user is null or if the recipient user is the postcard's
                // author
                if(uid == null || uid == postcard.author.uid) {
                    --numRecipients

                    return
                }

                // Send the postcard to the recipient user
                val cardPath = UUID.randomUUID().toString()
                val cardRef = FirebaseDatabase.getInstance().getReference("postcards/$uid/$cardPath")
                cardRef.setValue(postcard)
                    .addOnSuccessListener {
                        // Update to show that the user just received a postcard
                        user.lastReceived = System.currentTimeMillis()

                        // Save the updated user information to Firebase
                        val uRef = FirebaseDatabase.getInstance().getReference("users/$uid")
                        uRef.setValue(user)
                            .addOnSuccessListener {
                                Log.i(TAG, "Successfully delivered postcard to user")

                                --numRecipients
                                success = true

                                if(numRecipients == 0) {
                                    cb.onCallback(success)

                                    ref.removeEventListener(this)
                                }
                            }
                            .addOnFailureListener {
                                Log.e(TAG, "Failed to deliver postcard to user: ${it.message}")

                                --numRecipients

                                if(numRecipients == 0) {
                                    cb.onCallback(success)

                                    ref.removeEventListener(this)
                                }
                            }
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Failed to deliver postcard to user: ${it.message}")

                        --numRecipients

                        if(numRecipients == 0) {
                            cb.onCallback(success)

                            ref.removeEventListener(this)
                        }
                    }
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
        })

        // Handle the case where there are no recipients. Firebase guarantees that this will execute after the child
        // listeners have had a chance to increment [numRecipients].
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(numRecipients == 0) {
                    // Signals that the distribution has completed
                    done = true

                    cb.onCallback(success)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    /**
     * This interface can be used to signal when a function has finished its task on Firebase
     */
    interface FirebaseCallback {
        /**
         * Used to signal completion
         */
        fun onCallback()

        /**
         * Used to signal completion, accompanied by a boolean flag
         *
         * @param success indicate whether some aspect was a success
         */
        fun onCallback(success: Boolean)
    }
}