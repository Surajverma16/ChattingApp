package com.example.chattingapp.model

data class DeleteMessageData(
    var key: String = "",
    val senderId: String = "",
    val message: String = "",
    val messageTime : String ="",
    val imageUri : String ="",
    val fileUrl : String="",
     val fileName : String =""
)
