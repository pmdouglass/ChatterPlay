package com.example.chatterplay.screens

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ImageAspectRatio
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.MainActivity
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.analytics.ScreenPresenceLogger
import com.example.chatterplay.data_class.AlertType
import com.example.chatterplay.data_class.ChatMessage
import com.example.chatterplay.data_class.Title
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.seperate_composables.AlertLastMessage
import com.example.chatterplay.seperate_composables.AlertingScreen
import com.example.chatterplay.seperate_composables.AllMembersRow
import com.example.chatterplay.seperate_composables.ChatBubble
import com.example.chatterplay.seperate_composables.ChatInput
import com.example.chatterplay.seperate_composables.ChatRiseTopBar
import com.example.chatterplay.seperate_composables.NavigationRow
import com.example.chatterplay.seperate_composables.PrivateDrawerRoomList
import com.example.chatterplay.seperate_composables.RightSideModalDrawer
import com.example.chatterplay.seperate_composables.UserInfoFooter
import com.example.chatterplay.seperate_composables.UserProfileIcon
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
        factory = ChatRiseViewModelFactory(sharedPreferences, viewModel)
    )



    val usersAlertType by crViewModel.usersAlertType.collectAsState()
    val systemsAlertType by crViewModel.systemAlertType.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

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
    var showButtons by remember { mutableStateOf(false)}
    val topPlayers by crViewModel.topTwoPlayers.collectAsState()
    val topPlayerRoomId by crViewModel.topPlayerRoomId.collectAsState()
    val blockedPlayerId by crViewModel.blockedPlayerId.collectAsState()
    val currentUserTradeStatus by crViewModel.currentUsersTradeStatus.collectAsState()
    var showBlockedPlayerAlert by remember { mutableStateOf(false) }

    val RisersAll = allRisers
        .toMutableList()
        .apply {
            add(profile)
        }
    val isTopPlayer by remember {
        derivedStateOf {
            topPlayers?.let { (rank1, rank2) ->
                val rank1 = rank1.first
                val rank2 = rank2.first

                userId == rank1 || userId == rank2
            } ?: false
        }
    }

    // Navigation tab Icons 'Description to Icon'
    val tabs by remember {
        derivedStateOf {
            when {
                systemsAlertType == AlertType.top_discuss.string && isTopPlayer -> {
                    listOf(
                        "null" to Icons.Default.Home,  // home 0
                        "null" to Icons.Default.Person,  // profile 1
                        "null" to Icons.Default.Menu,  // game 2
                        "null" to Icons.Default.ImageAspectRatio,  // rating 3
                        "null" to Icons.AutoMirrored.Default.PlaylistAdd,  // last message 4
                        "null" to Icons.Default.ArrowCircleDown  // leader discuss 5
                    )
                }
                else -> {
                    listOf(
                        "null" to Icons.Default.Home,  // home 0
                        "null" to Icons.Default.Person,  // profile 1
                        "null" to Icons.Default.Menu,  // game 2
                        "null" to Icons.Default.ImageAspectRatio,  // rating 3
                        "null" to Icons.AutoMirrored.Default.PlaylistAdd  // last message 4
                    )
                }
            }
        }
    }

    var invite by remember { mutableStateOf(false)}

    LaunchedEffect(crRoomId, showAlert){
        viewModel.fetchAllRisers(crRoomId)
        viewModel.fetchChatRoomMembers(crRoomId = crRoomId, roomId = crRoomId, game = true, mainChat = true)
        viewModel.fetchChatRoomMemberCount(crRoomId, "", true, false)
        crViewModel.fetchGameInfo(crRoomId) // initialize 'gameInfo
        crViewModel.loadUserLocalAlertType(currentUser?.uid ?: "")
        crViewModel.fetchSystemAlertType(crRoomId)
        crViewModel.checkforUserAlert(crRoomId)
        crViewModel.getTopTwoPlayers(crRoomId) // topPlayers
        crViewModel.getBlockedPlayer(crRoomId) // initialize blockedPlayerId
    }
    LaunchedEffect(topPlayers){
        if (systemsAlertType == AlertType.top_discuss.string && isTopPlayer){
            crViewModel.fetchTradeStatus(crRoomId, userId)
            crViewModel.fetchTopPlayerRoomId(crRoomId)
        }
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
                systemsAlertType == AlertType.game.string || systemsAlertType == AlertType.game_results.string && userHasAnswered == false -> 2
                systemsAlertType == AlertType.game.string || systemsAlertType == AlertType.game_results.string && allMembersHasAnswered == true -> 2
                systemsAlertType == AlertType.top_discuss.string && isTopPlayer -> 5
                //systemsAlertType != AlertType.top_discuss.string && isTopPlayer -> 0
                else -> 0
            }
        }
    }



    Log.d("MainScreen", "startIndex: $startIndex")
    var selectedTabindex by remember { mutableIntStateOf(0) }


    LaunchedEffect(startIndex){
        selectedTabindex = startIndex
    }
    val disabledTabIndices by remember {
        derivedStateOf {
            Log.d("MainScreen", "Evaluating disabledTabIndices")

            val disabledTabs = mutableListOf<Int>()

            if (gameInfo == null){
                disabledTabs.add(2)
            }
            if (userHasAnswered == false){
                disabledTabs.addAll(listOf(0,1,3,4))
            }
            if (isTopPlayer && systemsAlertType == AlertType.top_discuss.string){
                disabledTabs.addAll(listOf(0,1,2,4))
            }
            if (usersAlertType == AlertType.blocking.string){
                disabledTabs.add(4)
            }
            disabledTabs.distinct()
        }
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
                        crRoomId = crRoomId,
                        profile = profile,
                        onClick = {showTopBarInfo = !showTopBarInfo},
                        enabled =
                        when {
                            blockedPlayerId == userId -> {false}
                            isTopPlayer && currentUserTradeStatus != "Confirmed" -> {false}
                            else -> {true}
                        },
                        onAction = { coroutineScope.launch { drawerState.open() }},
                        showTopBarInfo = showTopBarInfo,
                        navController = navController
                    )
                },
                bottomBar = {
                    when(selectedTabindex){
                        0 -> {
                            if (blockedPlayerId == userId){

                            }else{
                                ChatInput(
                                    crRoomId = crRoomId,
                                    roomId = crRoomId,
                                    memberCount = memberCount,
                                    game = true,
                                    mainChat = true
                                )
                            }
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
                        Row{
                            Button(onClick = { showButtons = !showButtons}){Text("Show All Buttons")}
                            Text("System: $systemsAlertType")
                            Text("User: $usersAlertType")
                        }
                        if (showButtons){
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
                                    crViewModel.searchForNewPlayer(
                                        crRoomId = crRoomId,
                                        allMembers = RisersAll,
                                        context = context
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
                                Button(onClick = {
                                    viewModel.announceBlockedPlayer(crRoomId, profile, context)
                                }){
                                    Text("Announce")
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ){

                            }
                        }



                        if (showAlert == true){
                            val tabIndex = if (isTopPlayer) 5 else 0
                            if (systemsAlertType != AlertType.none.string){
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
                                            AlertType.top_discuss.string -> {selectedTabindex = tabIndex}
                                            AlertType.blocking.string -> {selectedTabindex = 0}
                                            AlertType.last_message.string -> {selectedTabindex = 0}
                                            else -> {selectedTabindex = 0}
                                        }
                                        crViewModel.updateShowAlert(crRoomId, false)
                                        //crViewModel.updateAlertChangeToFalse()
                                    }
                                )
                            }else {
                                crViewModel.updateShowAlert(crRoomId, false)
                            }


                        }
                        if (blockedPlayerId == userId){
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(CRAppTheme.colorScheme.onGameBackground)
                            ){
                                if (showBlockedPlayerAlert){
                                    AlertLastMessage(
                                        crRoomId = crRoomId,
                                        AllRisers = allRisers,
                                        navController = navController
                                    )
                                }else {
                                    Text(
                                        "BLOCKED!",
                                        style = CRAppTheme.typography.H6,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(100.dp))

                                    Button(onClick = {
                                        // alert blocked player to next step
                                        showBlockedPlayerAlert = true
                                    }){
                                        Text(
                                            "Next"
                                        )
                                    }
                                }
                            }
                        }



                        NavigationRow(
                            tabs = tabs,
                            selectedTabIndex = selectedTabindex,
                            onTabSelected = {index ->
                                selectedTabindex = index
                            },
                            disabledTabIndices = disabledTabIndices
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
                            4 -> {
                                PlayersLastWords(crRoomId = crRoomId)
                            }
                            5 -> {
                                if (isTopPlayer){
                                    Log.d("MainScreen", "You are a top player")
                                    topPlayerRoomId?.let { roomId ->
                                        LeaderChatScreen(
                                            crRoomId = crRoomId,
                                            roomId = roomId,
                                            currentUserId = userId,
                                            otherUserId = if (userId == topPlayers?.first?.first) topPlayers?.second?.first ?: "" else topPlayers?.first?.first ?: ""
                                        )
                                    } ?: Text("No roomId")
                                } else {
                                    Text("You are NOT a Top Player", color = Color.Gray)
                                }
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
    viewModel: ChatViewModel = viewModel(),
    navController: NavController
){


    // Create SharedPreferences
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Initialize ChatRiseViewModel with the factory
    val crViewModel: ChatRiseViewModel = viewModel(
        factory = ChatRiseViewModelFactory(sharedPreferences, viewModel)
    )


    LaunchedEffect(Unit){
        // Log the event in Firebase Analytics
        val params = Bundle().apply {
            putString("screen_name", "ChatRiseMainChat")
            putString("user_id", userId)
            putString("timestamp", System.currentTimeMillis().toString())
        }
        AnalyticsManager.getInstance(context).logEvent("screen_view", params)
    }

    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(crRoomId) {
        viewModel.fetchChatMessages(context = context, crRoomId = crRoomId, roomId = crRoomId, game = true, mainChat = true) //  messages
    }


    // Scroll to bottom when new messages arrive
    val scrollToBottom = remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index != messages.size - 1
        }
    }
    LaunchedEffect(messages.size, scrollToBottom) {
        if (messages.isNotEmpty() && scrollToBottom.value) {
            listState.animateScrollToItem(messages.size - 1)
        }
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
            // Chat List UI
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxSize()
            ) {
                // Messages
                itemsIndexed(messages) { index, message ->
                    val previousMessage = messages.getOrNull(index - 1)
                    when {
                        message.senderId == "System" -> {
                            SystemMessage(message)
                        }
                        else -> {
                            ChatBubble(
                                image = message.image,
                                message = message,
                                isFromMe = message.senderId == userId,
                                previousMessage = previousMessage,
                                game = true
                            )
                        }
                    }
                }

                // Footer
                item {
                    UserInfoFooter(profile, true)
                }
            }
        }
    }
}
@Composable
fun PlayersLastWords(
    crRoomId: String,
    viewModel: ChatViewModel = viewModel()
){
    // Create SharedPreferences
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Initialize ChatRiseViewModel with the factory
    val crViewModel: ChatRiseViewModel = viewModel(
        factory = ChatRiseViewModelFactory(sharedPreferences, viewModel)
    )

    val messages by crViewModel.blockedMessages.collectAsState()

    LaunchedEffect(crRoomId){
        crViewModel.fetchBlockedMessages(crRoomId)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CRAppTheme.colorScheme.gameBackground)
            .padding(30.dp)
    ){
        Text("Final Message",
            style = CRAppTheme.typography.H3,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(messages){message ->
                lastWordsView(crRoomId = crRoomId, message = message)
            }
        }

    }
}
@Composable
fun lastWordsView(
    crRoomId: String,
    message: ChatMessage,
    viewModel: ChatViewModel = viewModel()
){
    // Create SharedPreferences
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Initialize ChatRiseViewModel with the factory
    val crViewModel: ChatRiseViewModel = viewModel(
        factory = ChatRiseViewModelFactory(sharedPreferences, viewModel)
    )

    var showLastMessage by remember { mutableStateOf(false) }
    val gameProfile by crViewModel.otherUserProfile.collectAsState()
    val trueProfile by viewModel.userProfile.collectAsState()
    val senderProfile = remember { mutableStateOf<UserProfile?>(null)}
    val gameSenderProfile = remember { mutableStateOf<UserProfile?>(null)}

    LaunchedEffect(message.senderId){
        senderProfile.value = viewModel.getRealUserProfile(message.senderId) // trueProfile
        gameSenderProfile.value = crViewModel.getOthercrUserProfile(crRoomId, message.senderId) // gameProfile
    }

    if (!showLastMessage){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(10.dp)
                    .clickable { showLastMessage = true }
            ){
                Image(
                    painter = rememberAsyncImagePainter(message.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(175.dp)
                        .clip(CircleShape)
                )
                Text(
                    message.senderName,
                    fontSize = 50.sp,
                    color = Color.White
                )
            }
        }
    }else {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(CRAppTheme.colorScheme.primary.copy(alpha = .85f)),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ){
                    senderProfile.value?.let {
                        UserProfileIcon(
                            chatMember = it,
                            imgSize = 50,
                            txtSize = 20,
                            game = false,
                            self = true,
                            navController = rememberNavController()
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp)
                    )
                    gameSenderProfile.value?.let {
                        UserProfileIcon(
                            chatMember = it,
                            imgSize = 50,
                            txtSize = 20,
                            game = false,
                            self = true,
                            navController = rememberNavController()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(message.message)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
@Composable
fun SystemMessage(message: ChatMessage){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ){
        Text(
            message.message,
            color = Color.White,
            style = CRAppTheme.typography.H1,
            textAlign = TextAlign.Center
        )
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


