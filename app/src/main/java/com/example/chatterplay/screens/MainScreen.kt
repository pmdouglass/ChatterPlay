package com.example.chatterplay.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chatterplay.seperate_composables.BottomInputBar
import com.example.chatterplay.seperate_composables.ChatBubbleMock
import com.example.chatterplay.seperate_composables.MainTopAppBar
import com.example.chatterplay.seperate_composables.PrivateDrawerRoomList
import com.example.chatterplay.seperate_composables.RightSideModalDrawer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatterplay.navigation.CRNavHost
import com.example.chatterplay.seperate_composables.AllMembersRow
import com.example.chatterplay.seperate_composables.rememberCRProfile
import com.example.chatterplay.seperate_composables.rememberProfileState
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.ui.theme.customPurple
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun MainScreen(navController: NavController, viewModel: ChatViewModel = viewModel()) {


    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val chatRoomMembers by viewModel.chatRoomMembers.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val (personalProfile, alternateProfile) = rememberProfileState(viewModel = viewModel, userId = userId)

    RightSideModalDrawer(
        drawerState  = drawerState,
        drawerContent = {
                        PrivateDrawerRoomList(
                            onTap = { coroutineScope.launch { drawerState.close() } },
                            onLongPress = { /*TODO*/ },
                            navController = navController
                        )
        },
        content = {
            Scaffold(
                topBar = {
                    MainTopAppBar(
                        title = "ChatRise",
                        action = true,
                        actionIcon = Icons.Default.Menu,
                        onAction = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        navController = navController
                        )
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
                        AllMembersRow(
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
                        ) {
                            ChatBubbleMock(true, false)
                            Spacer(modifier = Modifier.height(25.dp))
                            ChatBubbleMock(true, true)
                            Spacer(modifier = Modifier.height(25.dp))
                        }
                    }
                }
            )
        }
    )




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
                onTap = { coroutineScope.launch { drawerState.close() } },
                onLongPress = { /*TODO*/ },
                navController = navController
            )
        },
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


