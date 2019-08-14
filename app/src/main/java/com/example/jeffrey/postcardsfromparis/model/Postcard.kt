package com.example.jeffrey.postcardsfromparis.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * This class represents a postcard
 *
 * @property imgUrl the URL of the postcard picture
 * @property author the user who wrote the postcard
 * @property location where the postcard was written
 * @property message the postcard's message
 * @property time when the postcard was written
 * @constructor Creates a default postcard with a default user and no image, location, or message, written at time 0.
 */
@Parcelize
class Postcard(
    val imgUrl: String,
    val author: User,
    val location: String,
    val message: String,
    val time: Long
): Parcelable {
    constructor(): this("", User(), "", "", 0L)
}