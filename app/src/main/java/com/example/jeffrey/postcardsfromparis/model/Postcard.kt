package com.example.jeffrey.postcardsfromparis.model

import android.location.Location
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Postcard(private val imgUrl: String, private val author: User, private val location: Location,
        private val message: String): Parcelable {
    constructor(): this("", User(), Location(""), "")
}