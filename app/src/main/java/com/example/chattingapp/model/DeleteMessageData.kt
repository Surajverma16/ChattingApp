package com.example.chattingapp.model

data class DeleteMessageData(
    var key: String = "",
    val senderId: String = "",
    val message: String = "",
    val messageTime: String = "",
    val messageDate: String = "",
    val imageUri: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val messageRemark: Boolean = false
)
