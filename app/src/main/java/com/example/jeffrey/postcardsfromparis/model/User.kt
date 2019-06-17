package com.example.jeffrey.postcardsfromparis.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(
    val uid: String,
    var name: String,
    var imgUrl: String,
    var lastSent: Long,
    var lastReceived: Long
): Parcelable {
    constructor(): this("", "", "", 0L, 0L)
}