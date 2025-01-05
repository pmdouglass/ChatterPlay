package com.example.chatterplay.screens

import android.util.Log
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ImageAspectRatio
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chatterplay.seperate_composables.BottomInputBar
import com.example.chatterplay.seperate_composables.ChatBubbleMock
import com.example.chatterplay.seperate_composables.MainTopAppBar
import com.example.chatterplay.seperate_composables.PrivateDrawerRoomList
import com.example.chatterplay.seperate_composables.RightSideModalDrawer
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.data_class.ChatMessage
import com.example.chatterplay.data_class.ChatRoom
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.navigation.CRNavHost
import com.example.chatterplay.seperate_composables.AllMembersRow
import com.example.chatterplay.seperate_composables.ChatBubble
import com.example.chatterplay.seperate_composables.ChatInput
import com.example.chatterplay.seperate_composables.ChatLazyColumn
import com.example.chatterplay.seperate_composables.ChatRiseTopBar
import com.example.chatterplay.seperate_composables.NavigationRow
import com.example.chatterplay.seperate_composables.rememberCRProfile
import com.example.chatterplay.seperate_composables.rememberProfileState
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.ui.theme.customPurple
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun MainScreen(CRRoomId: String, navController: NavController, viewModel: ChatViewModel = viewModel()) {


    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var showTopBarInfo by remember { mutableStateOf(false)}
    var showMemberProfile by remember { mutableStateOf(false)}
    var selectedMemberProfile by remember { mutableStateOf<UserProfile?>(null)}


    val profile = rememberCRProfile(CRRoomId = CRRoomId)
    val chatRoomMembers by viewModel.chatRoomMembers.collectAsState()

    val tabs = listOf(
        "null" to Icons.Default.Home,
        "null" to Icons.Default.Person,
        "null" to Icons.Default.Menu,
        "null" to Icons.Default.ImageAspectRatio
    )
    var selectedTabindex by remember { mutableStateOf(0) }
    var invite by remember { mutableStateOf(false)}

    LaunchedEffect(CRRoomId){
        viewModel.fetchChatRoomMembers(roomId = CRRoomId, game = true)
    }

    RightSideModalDrawer(
        drawerState  = drawerState,
        drawerContent = {
            when {
                invite -> {
                    InviteSelectScreen(
                        CRRoomId = CRRoomId,
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
                        CRRoomId = CRRoomId,
                        onInvite = {invite = true},
                        onTap = { coroutineScope.launch { drawerState.close() } },
                        onLongPress = { /*TODO*/ },
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
                    ChatInput(
                        roomId = CRRoomId,
                        game = true
                    )
                },
                content = {paddingValues ->
                    Column (
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(customPurple)
                            .padding(paddingValues)
                            .clickable { showTopBarInfo = false }
                    ){
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
                                    roomId = CRRoomId,
                                    profile = profile,
                                    navController = navController
                                )

                            }
                            1 -> {
                                ProfileScreen2(
                                    profile = profile,
                                    game = true,
                                    self = true,
                                    isEditable = false
                                )
                            }
                            2 -> {

                            }
                            3 -> {
                                                            }
                            else -> {}
                        }



                    }
                    if (showMemberProfile && selectedMemberProfile != null){
                        ShowMembersProfile(
                            profile = selectedMemberProfile!!,
                            onDismiss = {showMemberProfile = false},
                            chatRoomMembers = chatRoomMembers,
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
    roomId: String,
    profile: UserProfile,
    navController: NavController
){
    Column(
        modifier = Modifier.fillMaxSize()
    ){
        AllMembersRow(
            selectedMember = selectedMember,
            chatRoomMembers = chatRoomMembers,
            game = true,
            self = false,
            navController = navController
        )
        Divider()
        Column (
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxSize()
        ){
            ChatLazyColumn(roomId = roomId, profile = profile, game = true)
        }
    }
}

@Composable
fun ShowMembersProfile(
    profile: UserProfile,
    onDismiss: () -> Unit,
    chatRoomMembers: List<UserProfile>,
    navController: NavController
){
    Dialog(onDismissRequest = { onDismiss()}){
        Surface {
            Column(
                modifier = Modifier
                    .height(600.dp)
                    .background(CRAppTheme.colorScheme.onGameBackground)
            ) {
                /*AllMembersRow(
                    onImageClick = { },
                    chatRoomMembers = chatRoomMembers,
                    game = true,
                    self = false,
                    navController = navController
                )*/
                ProfileScreen2(
                    profile = profile,
                    game = true,
                    self = false,
                    isEditable = false
                )
            }
        }
    }
}


@Composable
fun LazyChatColumn(
    roomId: String,
    profile: UserProfile,
    game: Boolean,
    viewModel: ChatViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(roomId){
        viewModel.fetchChatMessages(roomId = roomId, true)
    }

    val ScrollToBottom = remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index != messages.size -1
        }
    }
    if (ScrollToBottom.value){
        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()){
                listState.animateScrollToItem(messages.size -1)
            }

        }
    }
    Log.d("Message", "Message size is ${messages.size}")

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxSize()
    ) {
        itemsIndexed(messages) { index, message ->
            val previousMessage = if(index >0) messages[index - 1] else null
            ChatBubble(
                image = message.image,
                message = message,
                isFromMe = message.senderId == currentUser?.uid,
                previousMessage = previousMessage
            )
        }
        item {
            Row (
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Text(
                    "Sending as",
                    modifier = Modifier.padding(end = 10.dp)
                )
                Image(
                    painter = rememberAsyncImagePainter(profile.imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                )
                Text(
                    profile.fname,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }

}









@Composable
fun ChatRiseScreen(CRRoomId: String, navController: NavController, viewModel: ChatViewModel = viewModel()) {


    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val contentNavController = rememberNavController()

    RightSideModalDrawer(
        drawerState  = drawerState,
        drawerContent = {
            PrivateDrawerRoomList(
                CRRoomId = CRRoomId,
                onInvite = {},
                onTap = { coroutineScope.launch { drawerState.close() } },
                onLongPress = { /*TODO*/ },
                navController = navController
            )
        },
        reset = {},
        content = {
            Scaffold(
                topBar = {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = {contentNavController.popBackStack()}){
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = {navController.popBackStack()}){
                            Icon(
                                Icons.Default.ArrowCircleDown,
                                contentDescription = null
                            )
                        }

                        Text("Motha Fucka", style = CRAppTheme.typography.H3)
                        IconButton(onClick = { contentNavController.navigate("CRHome/${CRRoomId}")}){
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { contentNavController.navigate("game/${CRRoomId}") }){
                            Icon(
                                Icons.Default.HideImage,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { contentNavController.navigate("profile") }){
                            Icon(
                                Icons.Default.Man,
                                contentDescription = null
                            )
                        }
                    }
                },
                bottomBar = {
                    BottomInputBar()
                },
                content = {paddingValues ->
                    Column (
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(customPurple)
                            .padding(paddingValues)
                    ){
                        CRNavHost(navController = contentNavController, CRRoomId = CRRoomId)
                    }
                }
            )
        }
    )




}
@Composable
fun CRMainChat(CRRoomId: String){
    val profile = rememberCRProfile(CRRoomId = CRRoomId)

    if (profile.fname.isNotEmpty()){
        Column {
            Text("HomeScreen")
            Text("First name is ${profile.fname} no last name")
        }
    } else {
        Text("Loading . . . ")
    }

}
@Composable
fun Game(CRRoomId: String, viewModel: ChatViewModel = viewModel(), contentNavController: NavController){
    val chatRoomMembers by viewModel.chatRoomMembers.collectAsState()

    LaunchedEffect(CRRoomId, chatRoomMembers){
        viewModel.fetchChatRoomMembers(roomId = CRRoomId, game = true)
        Log.d("examp", "Chat room members: $chatRoomMembers")
    }

    Column (
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .background(customPurple)
            .padding(10.dp)
    ){
       if (chatRoomMembers.isEmpty()){
           Text("Loading . . . ", style = CRAppTheme.typography.H2)
       } else {
           chatRoomMembers.forEach { member ->
               Text(
                   text = member.fname ?: "Unknown",
                   style = CRAppTheme.typography.H2)
           }
       }
        Spacer(modifier = Modifier.height(100.dp))
        AllMembersRow(
            chatRoomMembers = chatRoomMembers,
            game = true,
            self = false,
            navController = contentNavController
        )
        Divider()
        Column (
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxSize()
        ) {
            ChatBubbleMock(true, false)
            Spacer(modifier = Modifier.height(25.dp))
            ChatBubbleMock(true, true)
            Spacer(modifier = Modifier.height(25.dp))
        }
    }
}
@Composable
fun Profile(){
    Text("Profile Screen")
}


