package com.example.chatterplay.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.ui.theme.darkPurple
import com.example.chatterplay.view_model.ChatViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.R
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.seperate_composables.FriendInfoRow
import com.example.chatterplay.seperate_composables.PersonIcon
import com.example.chatterplay.seperate_composables.RowState
import com.example.chatterplay.seperate_composables.rememberProfileState
import com.google.firebase.auth.FirebaseAuth



@OptIn(ExperimentalMaterial3Api::class)
@Composable
    fun InviteScreen(
    CRRoomId: String = "",
    game: Boolean,
    viewModel: ChatViewModel = viewModel(),
    navController: NavController
) {

    val currentUser = FirebaseAuth.getInstance().currentUser
    var roomName by remember { mutableStateOf("")}
    var searchtxt by remember { mutableStateOf("")}
    val allUsers by viewModel.allUsers.collectAsState()
    var selectedUsers by remember { mutableStateOf<List<UserProfile>>(emptyList())}
    val isGroup by remember(selectedUsers) {
        derivedStateOf { selectedUsers.size > 1 }
    }
    val filteredUsers by remember(searchtxt, allUsers) {
        derivedStateOf {
            allUsers.filter { it.fname.contains(searchtxt, ignoreCase = true) }
        }
    }

    fun toggleUserSelection(user: UserProfile){
        selectedUsers = selectedUsers.toMutableList().apply {
            if (contains(user)) remove(user) else add(user)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Invite Member",
                        style = CRAppTheme.typography.headingLarge,
                        color =
                        if (game) Color.White else Color.Black,
                        modifier = Modifier
                            .padding(start = 10.dp)
                    ) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor =
                    if (game){
                        darkPurple
                    }else{
                        CRAppTheme.colorScheme.background
                    }
                ),
                navigationIcon = {
                    Icon(Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = if (game) Color.White else Color.Black,
                        modifier = Modifier
                            .size(35.dp)
                            .clickable {
                                navController.popBackStack()
                            }
                    )},
                actions = {
                    Text(
                        text = "CREATE",
                        style = CRAppTheme.typography.headingSmall,
                        color =
                        if (selectedUsers.size == 0){
                            Color.Gray
                        }else if (game) {
                            Color.White
                        } else {
                            Color.Black
                        },
                        modifier = Modifier
                            .clickable {
                                if (selectedUsers.size == 0){

                                }else if (currentUser != null && selectedUsers.isNotEmpty()){
                                    val theRoomName = if (roomName.isBlank()) {
                                        selectedUsers.joinToString(", ") { it.fname}
                                    } else {
                                        roomName
                                    }
                                    if (game){
                                        viewModel.createAndInviteToChatRoom(
                                            CRRoomId = CRRoomId,
                                            memberIds = selectedUsers.map { it.userId },
                                            roomName = theRoomName
                                        ){ roomId ->
                                            navController.navigate("chatScreen/$CRRoomId/$roomId/true"){
                                                popUpTo("inviteScreen/$CRRoomId/true") {inclusive = true}
                                            }
                                        }
                                    } else{
                                        viewModel.createAndInviteToChatRoom(
                                            CRRoomId = CRRoomId,
                                            memberIds = selectedUsers.map { it.userId },
                                            roomName = theRoomName
                                        ){ roomId ->
                                            navController.navigate("chatScreen/$CRRoomId/$roomId/false"){
                                                popUpTo("inviteScreen/$CRRoomId/false") {inclusive = true}
                                            }
                                        }
                                    }
                                }

                            }
                    )}
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (game) CRAppTheme.colorScheme.gameBackground else CRAppTheme.colorScheme.background
                    )
                    .padding(paddingValues)
            ) {
                Divider(modifier = Modifier.padding(bottom = 10.dp))
                if (isGroup){
                    TextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        placeholder = { Text("Group Name (Optional)",
                            style = CRAppTheme.typography.infoLarge,
                            color = if (game) CRAppTheme.colorScheme.textOnGameBackground else CRAppTheme.colorScheme.textOnBackground
                        )},
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                } else {
                    roomName = ""
                }
                TextField(
                    value = searchtxt,
                    onValueChange = {searchtxt = it},
                    placeholder = {Text("Search",
                        style = CRAppTheme.typography.infoLarge,
                        color = if (game) CRAppTheme.colorScheme.textOnGameBackground else CRAppTheme.colorScheme.textOnBackground
                        )},
                    leadingIcon = {Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = if (game) CRAppTheme.colorScheme.textOnGameBackground else CRAppTheme.colorScheme.textOnBackground

                    )},
                    trailingIcon = {
                                   IconButton(onClick = {
                                       searchtxt = ""
                                   }) {
                                       Icon(
                                           Icons.Default.Clear,
                                           contentDescription = null,
                                           tint = if (game) CRAppTheme.colorScheme.textOnGameBackground else CRAppTheme.colorScheme.textOnBackground
                                       )
                                   }
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = if (game) CRAppTheme.colorScheme.onGameBackground else CRAppTheme.colorScheme.onBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50.dp))
                )
                // selected friends profile here
                LazyRow (
                    modifier = Modifier
                        .fillMaxWidth()
                ){
                    items(selectedUsers) {user ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(10.dp)
                                .clickable { toggleUserSelection(user) }
                        ){
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                            ){
                                Image(
                                    painter = rememberAsyncImagePainter(user.imageUrl),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                ){
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(20.dp)
                                    )
                                }
                            }
                            Text(
                                user.fname,
                                fontSize = 20.sp
                            )

                        }
                    }
                }

                // friends list here
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ){
                    items(filteredUsers) {user ->
                        Row (
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp)
                                .clickable { toggleUserSelection(user) }
                        ){
                            Image(
                                painter = rememberAsyncImagePainter(user.imageUrl),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                            )
                            Text(
                                "${user.fname} ${user.lname}",
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .weight(1f)
                            )
                            Checkbox(
                                checked = selectedUsers.contains(user),
                                onCheckedChange = {
                                    toggleUserSelection(user)
                                }
                            )
                        }
                    }
                }

                if (selectedUsers.isNotEmpty()){
                    Text(
                        "Selected: ${selectedUsers.size} user(s)",
                        style = CRAppTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }




            }
        }


    )
}

@Composable
fun LazyFriendSelect(
    filteredUsers: List<UserProfile>,
    onUserSelected: (UserProfile) -> Unit
) {
    LazyColumn (
        modifier = Modifier
            .padding(8.dp)
    ){
        items(filteredUsers) {user ->
            FriendInfoRow(
                game = false,
                user = user,
                onUserSelected = onUserSelected,
                state = RowState.check.string

            )
        }
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode", showBackground = true)
@Composable
fun PreviewInvite() {
    CRAppTheme () {
        Surface {
            InviteScreen(CRRoomId = "",game = false, navController = rememberNavController())
        }
    }
}