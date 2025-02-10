package com.example.chatterplay.screens

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatterplay.data_class.ChatMessage
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.Timestamp


/*
fun promptGoodbyeMessage(crRoomId: String, blockedPlayerId: String) {
    viewModelScope.launch {
        chatRepository.sendSystemMessage(
            crRoomId,
            ChatMessage(
                senderId = "System",
                senderName = "The Circle",
                message = "${blockedPlayerId} may now leave a final message before they exit the game.",
                timestamp = Timestamp.now(),
                isSystemMessage = true
            )
        )
    }
}

 */