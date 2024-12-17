package com.example.chatterplay.data_class

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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
    val createdAt: Timestamp = Timestamp.now()
)
data class AnonChatRoom(
    val members: List<String> = listOf(),
    val messages: List<ChatMessage> = emptyList(),
    val unreadMessageCounts: Map<String, Int> = mapOf(),
    val createdAt: Timestamp = Timestamp.now()
)
data class PlayerRanking(
    val memberId: String = "",
    val votes: Map<String, Map<String, Any>> = emptyMap(),
    val bonusPoints: Int = 0,
    val totalPoints: Int = 0
)

fun formatTheTimestamp(timestamp: Timestamp?): String {
    return if (timestamp != null) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        //sdf.timeZone = TimeZone.getTimeZone("UTC-4")
        sdf.format(timestamp.toDate())
    } else {
        "N/A"
    }
}
fun chatRoomDay(timestamp: Timestamp): String {
    val currentTime = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { time = timestamp.toDate() }

    val oneWeekAgo = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -7)
    }

    return if (messageTime.after(oneWeekAgo)) {
        // Within the last week
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        dayFormat.format(timestamp.toDate())
    } else {
        // More than a week ago
        val dateFormat = SimpleDateFormat("MMMM, EEE", Locale.getDefault())
        dateFormat.format(timestamp.toDate())
    }
}

fun formatCountdownTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}

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

fun getDay(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEE", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
fun getCurrentDayOfWeek(): String {
    val sdf = SimpleDateFormat("EEE", Locale.getDefault())
    return sdf.format(Date())
}


fun getTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun getDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
