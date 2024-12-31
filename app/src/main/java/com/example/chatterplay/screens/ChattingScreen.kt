package com.example.chatterplay.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatterplay.seperate_composables.AllMembersRow
import com.example.chatterplay.seperate_composables.ChatInput
import com.example.chatterplay.seperate_composables.ChatLazyColumn
import com.example.chatterplay.seperate_composables.MainTopAppBar
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChattingScreen(
    CRRoomId: String,
    roomId: String,
    game: Boolean,
    viewModel: ChatViewModel = viewModel(),
    navController: NavController
) {
    val chatRoom by viewModel.roomInfo.collectAsState()
    val chatRoomMembers by viewModel.chatRoomMembers.collectAsState()
    val membersCount by viewModel.chatRoomMembersCount.collectAsState()

    LaunchedEffect(CRRoomId, roomId) {
        viewModel.fetchChatMessages(roomId)
        viewModel.fetchChatRoomMembers(roomId = roomId, game = false)
        viewModel.fetchSingleChatRoomMemberCount(roomId)
        viewModel.getRoomInfo(CRRoomId = CRRoomId, roomId = roomId)
        Log.d("examp", "Chat room members: $chatRoomMembers")

    }



    //val currentRoom = chatRoom.find { it.roomId == roomId }
    /*currentRoom?.let { room ->
    }*/
    Scaffold (
        topBar = {
            if (game){
                MainTopAppBar(
                    title = "Private Room Name",
                    action = true,
                    actionIcon = Icons.Default.Menu,
                    onAction = { /*TODO*/ },
                    navController = navController
                )
            } else {
                CenterAlignedTopAppBar(
                    title = {
                        if (membersCount == 2){
                            chatRoomMembers?.firstOrNull()?.let {  member ->
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
            ChatInput(viewModel = viewModel, roomId = roomId )
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
                chatRoomMembers = chatRoomMembers,
                game = game,
                self = false,
                navController = navController
            )
            Divider()

            Column (
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                ChatLazyColumn(
                    viewModel = viewModel
                )

            }

        }
    }
}

