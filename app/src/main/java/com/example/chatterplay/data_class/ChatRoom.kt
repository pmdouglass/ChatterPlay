package com.example.chatterplay.data_class

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

data class ChatRoom(
    val roomId: String = "",
    val roomName: String = "",
    val members: List<String> = listOf(),
    val messages: List<ChatMessage> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp = Timestamp.now(),
    val lastSeenTimestamps: Map<String, Timestamp> = emptyMap(),
    val lastProfile: String = "",
    val hiddenFor: List<String> = listOf(),
    val hiddenTimestamp: Map<String, Timestamp> = emptyMap(),
    val unreadMessageCounts: Map<String, Int> = mapOf(),
    val createdAt: Timestamp = Timestamp.now(),
    val AlertType: String = "none"
)

fun formattedDayTimestamp(timestamp: Timestamp): String{
    val now = Timestamp.now()
    val difference = now.toDate().time - timestamp.toDate().time
    val oneDay = 24 * 60 * 60 * 1000

    return if (difference < oneDay){
        // last 24 hours
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        timeFormat.format(timestamp.toDate())
    }else{
        // more than 24 hours
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        dateFormat.format(timestamp.toDate())
    }
}
