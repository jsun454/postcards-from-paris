package com.example.jeffrey.postcardsfromparis.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Postcard(private val imgUrl: String, private val author: User, private val location: String,
        private val message: String): Parcelable {
    constructor(): this("", User(), "", "")
}