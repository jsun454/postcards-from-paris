package com.example.jeffrey.postcardsfromparis.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Postcard(val imgUrl: String, val author: User, val location: String,
        val message: String): Parcelable {
    constructor(): this("", User(), "", "")
}