package com.example.jeffrey.postcardsfromparis.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * This class represents a user
 *
 * @property uid the user's user ID
 * @property name the user's name
 * @property imgUrl the URL of the user's profile picture
 * @property lastReceived when the user last received a postcard
 * @constructor Creates a default user with no ID, name, or image, who last received a postcard at time 0.
 */
@Parcelize
class User(
    val uid: String,
    var name: String,
    var imgUrl: String,
    var lastReceived: Long
): Parcelable {
    constructor(): this("", "", "", 0L)
}