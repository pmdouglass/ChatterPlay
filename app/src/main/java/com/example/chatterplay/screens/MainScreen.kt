package com.example.chatterplay.screens

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chatterplay.MainActivity
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.analytics.ScreenPresenceLogger
import com.example.chatterplay.data_class.AlertType
import com.example.chatterplay.data_class.Title
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.seperate_composables.AlertingScreen
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
import com.example.chatterplay.view_model.ChatRiseViewModelFactory
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    crRoomId: String,
    navController: NavController,
    viewModel: ChatViewModel = viewModel(),
) {


    // Create SharedPreferences
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Initialize ChatRiseViewModel with the factory
    val crViewModel: ChatRiseViewModel = viewModel(
        factory = ChatRiseViewModelFactory(sharedPreferences)
    )


    val usersAlertType by crViewModel.usersAlertType.collectAsState()
    val systemsAlertType by crViewModel.systemAlertType.collectAsState()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var showTopBarInfo by remember { mutableStateOf(false)}
    var showMemberProfile by remember { mutableStateOf(false)}
    var selectedMemberProfile by remember { mutableStateOf<UserProfile?>(null)}
    var selectedGame by remember { mutableStateOf<Title?>(null)}
    val gameInfo by crViewModel.gameInfo.collectAsState() // gets gameInfo 'Title' from UserProfile
    val showAlert by crViewModel.showAlert.collectAsState()
    val profile = rememberCRProfile(crRoomId = crRoomId)
    val allRisers by viewModel.allRisers.collectAsState()
    val allChatRoomMembers by viewModel.allChatRoomMembers.collectAsState()
    val chatRoomMembers = allChatRoomMembers.filter { it.userId != currentUser?.uid }
    val memberCount by viewModel.chatRoomMembersCount.collectAsState()

    val RisersAll = allRisers
        .toMutableList()
        .apply {
            add(profile)
        }

    // Navigation tab Icons 'Description to Icon'
    val tabs = listOf(
        "null" to Icons.Default.Home,
        "null" to Icons.Default.Person,
        "null" to Icons.Default.Menu,
        "null" to Icons.Default.ImageAspectRatio
    )

    var invite by remember { mutableStateOf(false)}

    LaunchedEffect(crRoomId, showAlert){
        viewModel.fetchAllRisers(crRoomId)
        viewModel.fetchChatRoomMembers(crRoomId = crRoomId, roomId = crRoomId, game = true, mainChat = true)
        viewModel.fetchChatRoomMemberCount(crRoomId, "", true, false)
        crViewModel.fetchGameInfo(crRoomId) // initialize 'gameInfo
        crViewModel.loadUserLocalAlertType(currentUser?.uid ?: "")
        crViewModel.fetchSystemAlertType(crRoomId)
        crViewModel.checkforUserAlert(crRoomId)

    }
    Log.d("MainScreen", "All members $allChatRoomMembers")
    val allMembersHasAnswered by crViewModel.allMembersHasAnswered // sees if current user done with answers
    val userHasAnswered by crViewModel.userDoneAnswering

    Log.d("MainScreen", "isDoneAnswering: $allMembersHasAnswered")

    LaunchedEffect(systemsAlertType){
        crViewModel.checkforUserAlert(crRoomId)
    }
    LaunchedEffect(gameInfo){
        Log.d("MainScreen", "gameInfo: $gameInfo")
        if (gameInfo != null)
            gameInfo?.let { game ->
                crViewModel.checkUsersHasAnswered(crRoomId = crRoomId, title = game.title, context = context) // initialize userHasAnswered
                crViewModel.areAllMembersAnswered(crRoomId, game.title, context)
            }
    }

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    LaunchedEffect(Unit){
        // Log the event in Firebase Analytics
        val params = Bundle().apply {
            putString("screen_name", "ChatRiseScreen")
            putString("user_id", userId)
            putString("timestamp", System.currentTimeMillis().toString())
        }
        AnalyticsManager.getInstance(context).logEvent("screen_view", params)
    }
    ScreenPresenceLogger(screenName = "ChatRiseScreen", userId = userId)
    (context as? MainActivity)?.setCurrentScreen(("ChatRiseScreen"))


    val startIndex by remember {
        derivedStateOf {
            when {
                userHasAnswered == false -> 2
                else -> 0
            }
        }
    }

    Log.d("MainScreen", "startIndex: $startIndex")
    var selectedTabindex by remember { mutableIntStateOf(0) }


    LaunchedEffect(startIndex){
        selectedTabindex = startIndex
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
                                memberCount = memberCount,
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
                                val userIds: List<String> = RisersAll.map { it.userId }
                                crViewModel.generateRandomGameInfo(crRoomId) { randomGame ->
                                    if (randomGame != null){
                                        selectedGame = randomGame
                                        Log.d("MainChat", "selectedGame successfully set: $selectedGame")

                                        selectedGame?.let {game ->
                                            Log.d("MainChat", "Attempting to add game: ${game.title} for users: $userIds")
                                            crViewModel.saveGame(
                                                crRoomId = crRoomId,
                                                userIds = userIds,
                                                gameInfo = game,
                                                allMembers = RisersAll,
                                                context = context
                                            )

                                            coroutineScope.launch {
                                                // Log the event in Firebase Analytics
                                                val params = Bundle().apply {
                                                    putString("cr_room_id", crRoomId)
                                                    putString("game_name", game.title)
                                                    putString("game_mode", game.mode)
                                                }
                                                AnalyticsManager.getInstance(context).logEvent("game_started", params)

                                            }
                                        }
                                    } else {
                                        Log.d("MainChat", "No game was returned for generateRandomGameInfo, skipping addGame")
                                    }
                                }
                                Log.d("MainChat", "Button to Generate clicked")
                            }){
                                Text("Add Game")
                            }

                            Button(onClick = {
                                val userIds: List<String> = RisersAll.map { it.userId }
                                if (gameInfo != null){
                                    gameInfo?.let { game->
                                        crViewModel.resetGames(crRoomId, userIds, game.title, context = context)
                                    }
                                } else {
                                    Log.e("MainScreen", "gameInfo is null update")
                                }

                            }){
                                Text("update allDone")
                            }
                            Button(onClick = {
                                Log.d("MainScreen", "Alert to game button clicked")
                                crViewModel.updateSystemAlertType(
                                    crRoomId = crRoomId,
                                    alertType = AlertType.game,
                                    allMembers = RisersAll,
                                    context = context,
                                    userId = userId
                                )

                            }){
                                Text("alert to game")
                            }

                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier
                                .fillMaxWidth()
                        ){
                            Button(onClick = {
                                Log.d("MainScreen", "Alert to game button clicked")
                                crViewModel.updateSystemAlertType(
                                    crRoomId = crRoomId,
                                    alertType = AlertType.ranking,
                                    allMembers = RisersAll,
                                    context = context,
                                    userId = userId
                                )


                            }){
                                Text("alert to rank")
                            }
                            Button(onClick = {
                                Log.d("MainScreen", "Alert to rank button clicked")
                                crViewModel.updateSystemAlertType(
                                    crRoomId = crRoomId,
                                    alertType = AlertType.none,
                                    allMembers = RisersAll,
                                    context = context,
                                    userId = userId
                                )
                            }){
                                Text("alert to none")
                            }

                            Button(onClick = {
                                crViewModel.checkforUserAlert(crRoomId)
                            }){
                                if (usersAlertType != null){
                                    Text("$usersAlertType")
                                }else {
                                    Text("null")
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier
                                .fillMaxWidth()
                        ){
                            Button(onClick = {
                                Log.d("MainScreen", "Alert to game button clicked")
                                crViewModel.updateSystemAlertType(
                                    crRoomId = crRoomId,
                                    alertType = AlertType.game_results,
                                    allMembers = RisersAll,
                                    context = context,
                                    userId = userId
                                )

                            }){
                                Text("alert to gameResults")
                            }
                            Button(onClick = {
                                Log.d("MainScreen", "Alert to rankResults button clicked")
                                crViewModel.updateSystemAlertType(
                                    crRoomId = crRoomId,
                                    alertType = AlertType.rank_results,
                                    allMembers = RisersAll,
                                    context = context,
                                    userId = userId
                                )
                            }){
                                Text("Alert to RankResults")
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier
                                .fillMaxWidth()
                        ){
                            Button(onClick = {
                                crViewModel.updateSystemAlertType(
                                    crRoomId = crRoomId,
                                    alertType = AlertType.new_player,
                                    allMembers = RisersAll,
                                    context = context,
                                    userId = userId
                                )
                                coroutineScope.launch {
                                    viewModel.fetchAllRisers(crRoomId)
                                }
                            }){
                                Text("New Player")
                            }
                            Button(onClick = {
                                crViewModel.updateSystemAlertType(
                                    crRoomId = crRoomId,
                                    alertType = AlertType.blocking,
                                    allMembers = RisersAll,
                                    context = context,
                                    userId = "xlqEYiw505cY0wElaKzepCTzrVq2"
                                )
                            }){
                                Text("Block Player")
                            }
                        }



                        if (showAlert == true){
                            AlertingScreen(
                                crRoomId = crRoomId,
                                onDone = {
                                    when (systemsAlertType){
                                        AlertType.none.string -> {selectedTabindex = 0}
                                        AlertType.new_player.string -> {selectedTabindex = 0}
                                        AlertType.game.string -> {selectedTabindex = 2}
                                        AlertType.game_results.string -> {selectedTabindex = 2}
                                        AlertType.ranking.string -> {selectedTabindex = 3}
                                        AlertType.rank_results.string -> {selectedTabindex = 3}
                                        AlertType.blocking.string -> {selectedTabindex = 0}
                                        else -> {selectedTabindex = 0}
                                    }
                                    crViewModel.updateShowAlert(crRoomId, false)
                                    //crViewModel.updateAlertChangeToFalse()
                                }
                            )
                            /*
                            gameInfo?.let { game ->
                                AlertDialogSplash(
                                    crRoomId = crRoomId,
                                    game = true,
                                    gameInfo = game,
                                    onDone = {
                                        selectedTabindex = 2
                                    }
                                )
                            }

                             */

                        }


                        NavigationRow(
                            tabs = tabs,
                            selectedTabIndex = selectedTabindex,
                            onTabSelected = {index ->
                                selectedTabindex = index
                            },
                            disabledTabIndices =
                            when {
                                // emptylist() == none disabled
                                // listOf(?) == tab disabled

                                gameInfo == null -> listOf(2)
                                userHasAnswered == false -> emptyList()
                                userHasAnswered == true -> emptyList()
                                allMembersHasAnswered == false -> emptyList()
                                allMembersHasAnswered == true -> listOf(0)
                                else -> emptyList()
                            }
                        )

                        when (selectedTabindex){
                            0 -> {
                                RiseMainChat(
                                    selectedMember = { member ->
                                        selectedMemberProfile = member
                                        showMemberProfile = true
                                    },
                                    chatRoomMembers = allRisers,
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
                                if (gameInfo != null){
                                    gameInfo?.let { game ->
                                        ChoiceGameScreen(
                                            crRoomId = crRoomId,
                                            allChatRoomMembers = allChatRoomMembers
                                        )
                                    }

                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(CRAppTheme.colorScheme.onGameBackground)
                                    ){
                                        Text(
                                            "No Games to Play",
                                            style = CRAppTheme.typography.H2,
                                            color = Color.White
                                        )
                                    }
                                    Log.d("MainScreen", "selected game is null")
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
fun RiseMainChat(
    selectedMember: (UserProfile) -> Unit,
    chatRoomMembers: List<UserProfile>,
    crRoomId: String,
    profile: UserProfile,
    navController: NavController
){

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current
    LaunchedEffect(Unit){
        // Log the event in Firebase Analytics
        val params = Bundle().apply {
            putString("screen_name", "ChatRiseMainChat")
            putString("user_id", userId)
            putString("timestamp", System.currentTimeMillis().toString())
        }
        AnalyticsManager.getInstance(context).logEvent("screen_view", params)
    }

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
                mainChat = true,
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


