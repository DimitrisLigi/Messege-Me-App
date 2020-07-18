package com.example.msgme

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid: String?, val username: String, val profilePictureUri: String): Parcelable{
    constructor():this("","","")

}