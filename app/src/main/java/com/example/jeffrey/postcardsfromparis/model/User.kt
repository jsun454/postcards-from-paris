package com.example.jeffrey.postcardsfromparis.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid: String, val name: String, val imgUrl: String): Parcelable {
    constructor(): this("", "", "")
}