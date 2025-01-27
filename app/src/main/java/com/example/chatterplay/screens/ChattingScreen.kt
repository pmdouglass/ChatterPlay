package com.example.chatterplay.screens

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chatterplay.MainActivity
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.analytics.ScreenPresenceLogger
import com.example.chatterplay.seperate_composables.AllMembersRow
import com.example.chatterplay.seperate_composables.ChatInput
import com.example.chatterplay.seperate_composables.ChatLazyColumn
import com.example.chatterplay.seperate_composables.rememberProfileState
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChattingScreen(
    crRoomId: String,
    roomId: String,
    game: Boolean,
    mainChat: Boolean,
    viewModel: ChatViewModel = viewModel(),
    navController: NavController
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val chatRoom by viewModel.roomInfo.collectAsState()
    val allChatRoomMembers by viewModel.allChatRoomMembers.collectAsState()
    val RoomMembers = allChatRoomMembers.filter { it.userId != currentUserId }
    val membersCount by viewModel.chatRoomMembersCount.collectAsState()
    val (personalProfile, alternateProfile) = rememberProfileState(userId = currentUserId, viewModel)

    LaunchedEffect(crRoomId, roomId) {
        viewModel.fetchChatRoomMembers(crRoomId = crRoomId, roomId = roomId, game = game, mainChat = mainChat)
        viewModel.fetchChatRoomMemberCount(crRoomId, roomId, game, mainChat)
        viewModel.getRoomInfo(crRoomId = crRoomId, roomId = roomId)
        Log.d("examp", "Chat room members: $allChatRoomMembers")

    }

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current
    LaunchedEffect(Unit){
        // Log the event in Firebase Analytics
        val params = Bundle().apply {
            putString("screen_name", "ChattingScreen")
            putString("user_id", userId)
        }
        AnalyticsManager.getInstance(context).logEvent("screen_view", params)
    }
    ScreenPresenceLogger(screenName = "ChattingScreen", userId = userId)
    (context as? MainActivity)?.setCurrentScreen(("ChattingScreen"))


    Scaffold (
        topBar = {
            if (game){
                chatRoom?.let { room ->
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                room.roomName,
                                style = CRAppTheme.typography.headingLarge,
                                color = Color.White
                            )},
                        navigationIcon = {
                            IconButton(onClick = {navController.popBackStack()}) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    Modifier
                                        .size(35.dp),
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = CRAppTheme.colorScheme.gameBackground
                        )

                    )
                }
            } else {
                CenterAlignedTopAppBar(
                    title = {
                        if (membersCount == 2){
                            RoomMembers.firstOrNull()?.let { member ->
                                Text(
                                    member.fname,
                                    style = CRAppTheme.typography.headingLarge
                                )
                            }
                        } else {
                            chatRoom?.let { room ->
                                Text(
                                    room.roomName,
                                    style = CRAppTheme.typography.headingLarge
                                )
                            }
                        }
                            },
                    navigationIcon = {
                        IconButton(onClick = {navController.popBackStack()}) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                Modifier
                                    .size(35.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CRAppTheme.colorScheme.onBackground
                    )

                )
            }
        },
        bottomBar = {
            ChatInput(
                crRoomId = crRoomId,
                roomId = roomId,
                game = game,
                mainChat = false,
                memberCount = membersCount
            )
        }
    ){paddingValues ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (game) CRAppTheme.colorScheme.gameBackground else CRAppTheme.colorScheme.background
                )
                .padding(paddingValues)
        ){
            AllMembersRow(
                chatRoomMembers = RoomMembers,
                game = game,
                self = false,
                navController = navController
            )
            HorizontalDivider()

            Column (
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                ChatLazyColumn(
                    crRoomId = crRoomId,
                    roomId = roomId,
                    profile = personalProfile,
                    game = game,
                    mainChat = false
                )

            }

        }
    }
}

