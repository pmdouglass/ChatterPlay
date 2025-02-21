package com.example.chatterplay.screens

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chatterplay.MainActivity
import com.example.chatterplay.R
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.analytics.ScreenPresenceLogger
import com.example.chatterplay.seperate_composables.ChatRiseThumbnail
import com.example.chatterplay.seperate_composables.RoomSelectionView
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRoomSelect(
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {


    val chatRooms by viewModel.allChatRooms.collectAsState()
    val allRooms = chatRooms.sortedBy { it.lastMessageTimestamp }
    //val userProfile by viewModel.userProfile.collectAsState()
    val unreadMessageCount by viewModel.unreadMessageCount.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var search by remember { mutableStateOf("")}

    val filteredRooms by remember(search, chatRooms) {
        derivedStateOf {
            chatRooms.filter { it.roomName.contains(search, ignoreCase = true) }
        }
    }


    val context = LocalContext.current
    LaunchedEffect(Unit){
        // Log the event in Firebase Analytics
        val params = Bundle().apply {
            putString("screen_name", "RoomSelectScreen")
            putString("user_id", userId)
            putString("timestamp", System.currentTimeMillis().toString())
        }
        AnalyticsManager.getInstance(context).logEvent("screen_view", params)
    }
    ScreenPresenceLogger(screenName = "RoomSelectScreen", userId = userId)
    (context as? MainActivity)?.setCurrentScreen(("RoomSelectionScreen"))


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 20.dp)
                ){
                    IconButton(onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                    IconButton(onClick = {navController.navigate("settingsScreen")}) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(20.dp))
                NavigationDrawerItem(
                    label = {
                        Text("Profile")
                            },
                    selected = false,
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        navController.navigate("profileScreen/false/true/$userId")
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Sign Out") },
                    selected = false,
                    icon = {
                           Icons.Default.AutoStories
                    },
                    onClick = {

                        coroutineScope.launch {
                            // Log the logout event
                            val params = Bundle().apply {
                                putString("user_id", userId)
                                putString("login_method", "email") // Change as needed for other methods
                            }
                            AnalyticsManager.getInstance(context).logEvent("user_logout", params)
                        }

                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("loginScreen") {
                            popUpTo(0)
                        }
                    }
                )

            }

        }
    ) {
        Scaffold (
            topBar = {
                TopAppBar(title = {  },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CRAppTheme.colorScheme.background
                    ),
                    navigationIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.cool_neon),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .clickable {
                                    coroutineScope.launch {
                                        drawerState.open()
                                    }
                                }
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            val crRoomId = "0"
                            navController.navigate("inviteScreen/$crRoomId/false")
                        }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                )
            },
            content = {paddingValues ->
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CRAppTheme.colorScheme.background)
                        .padding(paddingValues)
                ){
                    Spacer(modifier = Modifier.height(10.dp))
                    ChatRiseThumbnail(navController = navController)
                    HorizontalDivider()
                    Text(
                        "Conversations",
                        style = CRAppTheme.typography.headingMedium,
                        modifier = Modifier.padding(start = 8.dp, bottom = 10.dp)
                    )
                    OutlinedTextField(
                        value = search,
                        onValueChange = {search = it},
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CRAppTheme.colorScheme.textOnBackground)},
                        placeholder = {Text("Search", color = CRAppTheme.colorScheme.textOnBackground)},
                        modifier = Modifier
                            .fillMaxWidth()
                            //.height(25.dp)
                            .padding(start = 10.dp, end = 10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // possible sort by and filter
                    /*
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp)
                    ){
                        Text("Sort By")
                        Text("Filter")

                    }

                     */
                    Spacer(modifier = Modifier.height(20.dp))


                    LazyColumn (
                        modifier = Modifier
                            .fillMaxSize()
                    ){
                        items(filteredRooms){ room ->
                            val crRoomId = "0"
                            RoomSelectionView(
                                game = false,
                                room = room,
                                membersCount = room.members.size,
                                replyCount = unreadMessageCount[room.roomId] ?: 0,
                                onClick = {
                                    viewModel.updateLastSeenTimestamp(room.roomId)
                                    navController.navigate("chatScreen/${crRoomId}/${room.roomId}/false/false")
                                }
                            )
                        }
                    }


                }
            }
        )
    }

}
