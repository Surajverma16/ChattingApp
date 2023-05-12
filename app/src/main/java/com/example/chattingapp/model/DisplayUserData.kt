package com.example.chattingapp.model

data class DisplayUserData(
    var key: String = "",
    val name: String = "",
    val number: String = "",
    val profileImgName: String = "",
    val token: String = "",
    val onlineStatus : Boolean = false,
    val lastTime : String = "",
    val lastDate : String = ""
)
