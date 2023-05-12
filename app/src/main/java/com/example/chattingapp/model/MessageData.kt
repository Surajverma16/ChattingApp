package com.example.chattingapp.model

data class MessageData(
    val senderId: String = "",
    val message: String = "",
    val messageTime: String = "",
    val messageDate: String = "",
    val imageUri: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val messageRemark: Boolean = false
)
