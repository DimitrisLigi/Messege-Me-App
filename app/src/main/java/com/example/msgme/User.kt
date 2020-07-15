package com.example.msgme

class User(val uid: String?, val username: String, val profilePictureUri: String){
    constructor():this("","","")
}