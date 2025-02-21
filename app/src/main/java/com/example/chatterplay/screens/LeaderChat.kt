package com.example.chatterplay.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatterplay.data_class.AlertType
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.seperate_composables.AllMembersRow
import com.example.chatterplay.seperate_composables.ChatBubble
import com.example.chatterplay.seperate_composables.UserProfileIcon
import com.example.chatterplay.seperate_composables.rememberCRProfile
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatRiseViewModel
import com.example.chatterplay.view_model.ChatRiseViewModelFactory
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@Composable
fun LeaderChatScreen(
    crRoomId: String,
    roomId: String,
    currentUserId: String,
    otherUserId: String,
    viewModel: ChatViewModel = viewModel()
) {
    // Create SharedPreferences
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Initialize ChatRiseViewModel with the factory
    val crViewModel: ChatRiseViewModel = viewModel(
        factory = ChatRiseViewModelFactory(sharedPreferences, viewModel)
    )

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val profile = rememberCRProfile(crRoomId)
    val allRisers by viewModel.allRisers.collectAsState()
    val AllRisers = allRisers.toMutableList().apply { add(profile) }
    val currentUserProfile = allRisers.find { it.userId == currentUserId }
    val otherUserProfile = allRisers.find { it.userId == otherUserId }
    val filteredMembers = allRisers.filter { it.userId != currentUserId && it.userId != otherUserId}
    val coroutineScope = rememberCoroutineScope()
    val selectedPlayer by crViewModel.currentUsersSelection.collectAsState()
    val otherUserSelectedPlayer by crViewModel.otherUsersSelection.collectAsState()
    var showDialog by remember{ mutableStateOf(false) }
    val currentUserTradeStatus by crViewModel.currentUsersTradeStatus.collectAsState()
    val otherUserTradeStatus by crViewModel.otherUsersTradeStatus.collectAsState()
    var userGoodbyeMessage by remember { mutableStateOf(false) }
    val hasUserSelected by crViewModel.hasUserSelected.collectAsState()
    var selectedMemberProfile by remember { mutableStateOf<UserProfile?>(null)}
    val userSelections by crViewModel.userSelections.collectAsState()
    val result by crViewModel.result.collectAsState()
    val bothDoneSelecting = userSelections.size == 2


    val messages by crViewModel.topPlayerMessages.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(otherUserId) {
        crViewModel.getUsersSelection(crRoomId, otherUserId, false)
    }
    LaunchedEffect(currentUserId) {
        crViewModel.getUsersSelection(crRoomId, currentUserId, true)
    }

    LaunchedEffect(crRoomId) {
        crViewModel.fetchTradeStatus(crRoomId, userId)
        crViewModel.listenForTradeUpdates(crRoomId, userId)
        crViewModel.listenForOtherUserTradeUpdates(crRoomId, otherUserId)
        viewModel.fetchAllRisers(crRoomId)
        crViewModel.fetchTopPlayerChatMessages(crRoomId, roomId)
        crViewModel.checkUserRemoved(crRoomId)
        crViewModel.evaluateDecision(crRoomId)
        crViewModel.checkIfUserSelected(crRoomId)
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(CRAppTheme.colorScheme.onGameBackground)
            .padding(16.dp)
    ) {


        Text("Discuss and agree on which player will be removed from the game.", color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Trade Interface
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trade box for the current user
            Column (horizontalAlignment = Alignment.CenterHorizontally){
                Text(text = "Your Pick", color = Color.White)
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .border(
                            2.dp,
                            if (currentUserTradeStatus == "onHold") Color.Red else CRAppTheme.colorScheme.primary
                        )
                ) {
                    // Placeholder for trade item
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        selectedPlayer?.let { profile ->
                            UserProfileIcon(
                                chatMember = profile,
                                imgSize = 55,
                                txtSize = 20,
                                game = true,
                                self = false,
                                selectedMember = {
                                    crViewModel.saveCurrentUsersSelection(
                                        CRRoomId = crRoomId,
                                        currentUserId = currentUserId,
                                        selectedPlayer = ""
                                    )
                                    crViewModel.updateTradeUserStatus(crRoomId, userId, "Canceled")
                                    crViewModel.updateTradeUserStatus(crRoomId, otherUserId, "Canceled")
                                }
                            )
                        }
                    }
                    if (currentUserTradeStatus != "Canceled"){
                        Icon(Icons.Default.Close, contentDescription = "", modifier = Modifier.fillMaxSize(), tint = Color.Red)
                    }

                }
            }

            if (currentUserTradeStatus != "Confirmed"){
                Button(
                    onClick = {
                        // clickable?
                        if (selectedPlayer == otherUserSelectedPlayer){
                            // put on hold
                            if (currentUserTradeStatus == "Canceled" && otherUserTradeStatus == "onHold"){
                                //personalMessage = true
                                showDialog = true
                            }
                            if (currentUserTradeStatus == "Canceled" && otherUserTradeStatus == "Canceled"){
                                crViewModel.updateTradeUserStatus(crRoomId, userId, "onHold")
                            }
                        }
                    },
                    enabled = currentUserTradeStatus == "Canceled" && selectedPlayer != null
                ){
                    Text(
                        if (currentUserTradeStatus != "onHold") "Accept" else "Waiting..",
                        color =
                        if (selectedPlayer == otherUserSelectedPlayer) Color.White else Color.Gray
                    )
                }
            }

            // Trade box for the other user
            Column (horizontalAlignment = Alignment.CenterHorizontally){
                Text(text = otherUserProfile?.fname + "'s Pick", color = Color.White)

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .border(
                            2.dp,
                            if (otherUserTradeStatus == "onHold") Color.Red else CRAppTheme.colorScheme.primary
                        )
                ) {
                    // Placeholder for trade item
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        otherUserSelectedPlayer?.let { profile ->
                            UserProfileIcon(
                                chatMember = profile,
                                imgSize = 55,
                                txtSize = 20,
                                game = true,
                                self = true
                            )
                        }
                    }
                    if (otherUserTradeStatus != "Canceled"){
                        Icon(Icons.Default.Close, contentDescription = "", modifier = Modifier.fillMaxSize(), tint = Color.Red)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Row of Other Players' Profile Images
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AllMembersRow(
                chatRoomMembers = filteredMembers,
                game = true,
                self = false,
                selectedMember = {member ->
                    Log.d("LeaderChat", "SelectedMembers clicked")
                    coroutineScope.launch {
                        crViewModel.saveCurrentUsersSelection(
                            crRoomId,
                            currentUserId,
                            member.userId
                        )
                        crViewModel.updateTradeUserStatus(crRoomId, userId, "Canceled")
                        crViewModel.updateTradeUserStatus(crRoomId, otherUserId, "Canceled")
                    }
                }
            )

        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 4: Chat Section Placeholder
        when {
            hasUserSelected == false-> {
                // User has not made a selection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(2.dp, CRAppTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(messages) { index, message ->
                                val previousMessage = if (index > 0) messages[index - 1] else null
                                ChatBubble(
                                    message = message,
                                    game = true,
                                    image = message.image,
                                    isFromMe = message.senderId == currentUserId,
                                    previousMessage = previousMessage
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                if (currentUserTradeStatus != "Confirmed") {
                    sendTopPlayerMessage(crRoomId = crRoomId, roomId = roomId)
                }
            }
            /*
            bothDoneSelecting && result == currentUserId -> {
                // Current user was chosen to send the message
                var input by remember { mutableStateOf("") }

                fun send() {
                    if (input.isNotBlank()) {
                        crViewModel.sendGoodbyeMessage(crRoomId, roomId, input, "")
                        //crViewModel.sendGoodbyeMessage(crRoomId, roomId, input, selectedPlayer!!.userId)
                        input = ""
                    }
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AllMembersRow(selectedMember = {}, chatRoomMembers = allRisers, game = true, self = false)

                    Spacer(modifier = Modifier.height(100.dp))
                    // Text input
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            send()
                            crViewModel.updateSystemAlertType(
                                crRoomId = crRoomId,
                                alertType = AlertType.blocking,
                                allMembers = allRisers,
                                userId = "",
                                context = context
                            )
                            crViewModel.updateTradeStatus(crRoomId, otherUserId)
                        }) {
                            Icon(Icons.Default.Send, contentDescription = null)
                        }
                    }
                }
            }
             */
            /*
            bothDoneSelecting -> {
                // Other user was chosen to send the message
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("${otherUserProfile?.fname} was chosen to leave a message")
                    Text("Waiting for ${otherUserProfile?.fname} to leave a message to the group")
                }
            }

             */

            else -> {
                // Both are not done making a selection
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("Waiting for other player to choose who should tell the group...")
                }
            }
        }


    }




    if (showDialog){
        Log.d("Dialog", "Players have agreed")
        AlertDialog(
            onDismissRequest = { showDialog = false},
            title = { Text("Title")},
            text = { Text("Are you sure you want to remove this player?")},
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    //crViewModel.updateTradeStatus(crRoomId, otherUser)

                    selectedPlayer?.let {player ->
                        crViewModel.updateSystemAlertType(crRoomId, AlertType.blocking, AllRisers, player.userId, context)
                        crViewModel.updateTradeUserStatus(crRoomId, userId, "onHold")
                        crViewModel.handleTradeAcceptance(crRoomId, userId, otherUserId)
                    }
                    //personalMessage = true
                    //whoMessage = true
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {

                Button(onClick = {
                    showDialog = false
                }) {
                    Text("No")
                }
            }
        )

    }
    /*
    if (currentUserTradeStatus == "Confirmed" && otherUserTradeStatus == "Confirmed"){
        selectedPlayer?.let { removedUser ->
            crViewModel.updateSystemAlertType(
                crRoomId = crRoomId,
                alertType = AlertType.blocking,
                allMembers = allRisers,
                userId = removedUser.userId,
                context = context
            )
        }
    }

     */
    /*
    if (currentUserTradeStatus == "Confirmed" && hasUserSelected == false){
        AlertDialog(
            onDismissRequest = {whoMessage = false},
            title = { Text("Who will Tell the group?")},
            text = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                ){
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                    ){
                        Button(onClick = {
                            crViewModel.userSelectionPick(crRoomId, "me")
                            crViewModel.checkIfUserSelected(crRoomId)
                        }){
                            Text(
                                "Me",
                                style = CRAppTheme.typography.H1,
                                modifier = Modifier
                                    .padding(10.dp)
                            )
                        }
                        Button(onClick = {
                            crViewModel.userSelectionPick(crRoomId, "you")
                            crViewModel.checkIfUserSelected(crRoomId)
                        }){
                            Text(
                                otherUserProfile!!.fname,
                                style = CRAppTheme.typography.H1,
                                modifier = Modifier
                                    .padding(10.dp)
                            )
                        }

                    }
                    Spacer(modifier = Modifier.height(50.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                    ){
                        Button(onClick = {
                            crViewModel.userSelectionPick(crRoomId, "random")
                            crViewModel.checkIfUserSelected(crRoomId)
                        }){
                            Text(
                                "Pick at Random",
                                style = CRAppTheme.typography.H1,
                                modifier = Modifier
                                    .padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

     */
}

@Composable
fun sendTopPlayerMessage(
    crRoomId: String,
    roomId: String,
    viewModel: ChatViewModel = viewModel()
) {

    // Create SharedPreferences
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Initialize ChatRiseViewModel with the factory
    val crViewModel: ChatRiseViewModel = viewModel(
        factory = ChatRiseViewModelFactory(sharedPreferences, viewModel)
    )

    var input by remember{ mutableStateOf("")}

    fun send(){
        if(input.isNotBlank()){
            crViewModel.sendTopPlayerMessage(crRoomId, roomId, input)
            input = ""
        }
    }

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ){
        TextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier
                .weight(1f)
                .background(Color.White),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                send()
            }),
        )
        IconButton(onClick = {send()}) {
            Icon(Icons.Default.Send, contentDescription = null )
        }

    }

}
