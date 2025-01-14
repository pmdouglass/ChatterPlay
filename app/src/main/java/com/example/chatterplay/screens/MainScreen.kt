package com.example.chatterplay.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ImageAspectRatio
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chatterplay.data_class.Title
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.seperate_composables.AllMembersRow
import com.example.chatterplay.seperate_composables.ChatInput
import com.example.chatterplay.seperate_composables.ChatLazyColumn
import com.example.chatterplay.seperate_composables.ChatRiseTopBar
import com.example.chatterplay.seperate_composables.NavigationRow
import com.example.chatterplay.seperate_composables.PrivateDrawerRoomList
import com.example.chatterplay.seperate_composables.RightSideModalDrawer
import com.example.chatterplay.seperate_composables.rememberCRProfile
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.ui.theme.customPurple
import com.example.chatterplay.view_model.ChatRiseViewModel
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    crRoomId: String,
    navController: NavController,
    viewModel: ChatViewModel = viewModel(),
    crViewModel: ChatRiseViewModel = viewModel()
) {


    val currentUser = FirebaseAuth.getInstance().currentUser
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var showTopBarInfo by remember { mutableStateOf(false)}
    var showMemberProfile by remember { mutableStateOf(false)}
    var selectedMemberProfile by remember { mutableStateOf<UserProfile?>(null)}
    var selectedGame by remember { mutableStateOf<Title?>(null)}
    val gameInfo by crViewModel.gameInfo.collectAsState()


    val profile = rememberCRProfile(crRoomId = crRoomId)
    val allChatRoomMembers by viewModel.allChatRoomMembers.collectAsState()
    val chatRoomMembers = allChatRoomMembers.filter { it.userId != currentUser?.uid }


    // Navigation tab Icons 'Description to Icon'
    val tabs = listOf(
        "null" to Icons.Default.Home,
        "null" to Icons.Default.Person,
        "null" to Icons.Default.Menu,
        "null" to Icons.Default.ImageAspectRatio
    )
    var selectedTabindex by remember { mutableIntStateOf(0) }
    var invite by remember { mutableStateOf(false)}

    LaunchedEffect(crRoomId){
        viewModel.fetchChatRoomMembers(crRoomId = crRoomId, roomId = crRoomId, game = true, mainChat = true)
        crViewModel.getGameInfo(crRoomId)
    }

    RightSideModalDrawer(
        drawerState  = drawerState,
        drawerContent = {
            when {
                invite -> {
                    InviteSelectScreen(
                        crRoomId = crRoomId,
                        game = true,
                        onBack = {invite = false},
                        onCreate = {
                            coroutineScope.launch { drawerState.close() }
                            invite = false
                                   },
                        viewModel = ChatViewModel(),
                        navController = navController
                    )
                }
                else -> {
                    PrivateDrawerRoomList(
                        crRoomId = crRoomId,
                        onInvite = {invite = true},
                        navController = navController
                    )
                }
            }

        },
        reset = {invite = false},
        content = {
            Scaffold(
                topBar = {
                    ChatRiseTopBar(
                        profile = profile,
                        onClick = {showTopBarInfo = !showTopBarInfo},
                        onAction = { coroutineScope.launch { drawerState.open() }},
                        showTopBarInfo = showTopBarInfo,
                        navController = navController
                    )
                },
                bottomBar = {
                    when(selectedTabindex){
                        0 -> {
                            ChatInput(
                                crRoomId = crRoomId,
                                roomId = crRoomId,
                                game = true,
                                mainChat = true
                            )
                        }
                        else -> {
                            
                        }
                    }
                },
                content = {paddingValues ->
                    Column (
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .clickable { showTopBarInfo = false }
                    ){
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier
                                .fillMaxWidth()
                        ){
                            Button(onClick = {
                                crViewModel.generateRandomGameInfo { randomGame ->
                                    selectedGame = randomGame
                                }
                                selectedGame?.let {game ->
                                    val userIds: List<String> = allChatRoomMembers.map { it.userId }
                                    crViewModel.addGame(crRoomId, userIds, game)
                                }
                            }){
                                Text("Add Game")
                            }
                        }
                        NavigationRow(
                            tabs = tabs,
                            selectedTabIndex = selectedTabindex,
                            onTabSelected = {index ->
                                selectedTabindex = index
                            }
                        )

                        when (selectedTabindex){
                            0 -> {
                                RiseMainChat(
                                    selectedMember = { member ->
                                        selectedMemberProfile = member
                                        showMemberProfile = true
                                    },
                                    chatRoomMembers = chatRoomMembers,
                                    crRoomId = crRoomId,
                                    profile = profile,
                                    navController = navController
                                )

                            }
                            1 -> {
                                ProfileScreen2(
                                    crRoomId = crRoomId,
                                    profile = profile,
                                    game = true,
                                    self = true,
                                    isEditable = false,
                                    navController = navController
                                )
                            }
                            2 -> {
                                if (gameInfo != null) {
                                    gameInfo?.let { game ->
                                        PairGameScreen(
                                            crRoomId = crRoomId,
                                            gameInfo = game,
                                            allChatRoomMembers = allChatRoomMembers
                                        )
                                    }
                                } else {
                                    Text("GameInfo is null")
                                }

                            }
                            3 -> {
                                RankingScreen(
                                    crRoomId = crRoomId,
                                    allChatRoomMembers = allChatRoomMembers
                                )
                            }
                            else -> {}
                        }



                    }
                    if (showMemberProfile && selectedMemberProfile != null){
                        ShowMembersProfile(
                            crRoomId = crRoomId,
                            profile = selectedMemberProfile!!,
                            onDismiss = {showMemberProfile = false},
                            navController = navController
                        )
                    }
                }
            )

        }

    )
}
@Composable
fun RiseMainChat(selectedMember: (UserProfile) -> Unit, chatRoomMembers: List<UserProfile>, crRoomId: String, profile: UserProfile, navController: NavController){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(customPurple)
    ){
        AllMembersRow(
            selectedMember = selectedMember,
            chatRoomMembers = chatRoomMembers,
            game = true,
            self = false,
            navController = navController
        )
        HorizontalDivider()
        Column (
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxSize()
        ){
            ChatLazyColumn(
                crRoomId = crRoomId,
                roomId = "",
                profile = profile,
                game = true,
                mainChat = true
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShowMembersProfile(
    crRoomId: String,
    profile: UserProfile,
    onDismiss: () -> Unit,
    navController: NavController
){
    Dialog(onDismissRequest = { onDismiss()}){
        Surface {
            Column(
                modifier = Modifier
                    .height(600.dp)
                    .background(CRAppTheme.colorScheme.onGameBackground)
            ) {
                ProfileScreen2(
                    crRoomId = crRoomId,
                    profile = profile,
                    game = true,
                    self = false,
                    isEditable = false,
                    navController = navController
                )
            }
        }
    }
}

