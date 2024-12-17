package com.example.chatterplay.data_class

import com.google.firebase.Timestamp

data class ChatMessage(
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val image: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
