package com.example.chatterplay.seperate_composables

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.R
import com.example.chatterplay.data_class.ChatMessage
import com.example.chatterplay.data_class.Title
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.data_class.formattedDayTimestamp
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatRiseViewModel
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ChatLazyColumn(
    crRoomId: String,
    roomId: String,
    profile: UserProfile,
    game: Boolean,
    mainChat: Boolean,
    viewModel: ChatViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()

    // Fetch chat messages when roomId or game changes
    LaunchedEffect(roomId, game) {
        viewModel.fetchChatMessages(crRoomId = crRoomId, roomId = roomId, game = game, mainChat = mainChat)
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
    /*if (scrollToBottom.value) {
    }*/

    // Chat List UI
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxSize()
    ) {
        // Messages
        itemsIndexed(messages) { index, message ->
            val previousMessage = messages.getOrNull(index - 1)
            ChatBubble(
                image = message.image,
                message = message,
                isFromMe = message.senderId == currentUser?.uid,
                previousMessage = previousMessage,
                game = game
            )
        }

        // Footer
        item {
            UserInfoFooter(profile, game)
        }
    }
}

@Composable
fun ChatMainPreviewLazyColumn(
    crRoomId: String,
    roomId: String,
    viewModel: ChatViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()

    // Fetch chat messages when roomId or game changes
    LaunchedEffect(roomId) {
        viewModel.fetchChatMessages(crRoomId = crRoomId, roomId = roomId, game = true, mainChat = true)
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

    // Chat List UI
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxSize()
    ) {
        // Messages
        itemsIndexed(messages) { index, message ->
            ThumbnailChatList(
                image = message.image,
                message = message,
                isFromMe = message.senderId == currentUser?.uid
            )
        }
    }
}
@Composable
fun UserInfoFooter(profile: UserProfile, game: Boolean) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Sending as",
            color = if(game) Color.White else Color.Black,
            modifier = Modifier.padding(end = 8.dp)
        )
        Image(
            painter = rememberAsyncImagePainter(profile.imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
        )
        Text(
            text = profile.fname,
            color = if (game) Color.White else Color.Black,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    game: Boolean,
    image: String,
    isFromMe: Boolean,
    anon: Boolean = false,
    previousMessage: ChatMessage?
) {

    val borderRad = 30.dp
    val showProfileImage = previousMessage?.senderId != message.senderId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        // ---------------- if left than members picture
        if (!isFromMe && showProfileImage) {
            Image(
                painter =
                if (anon){
                    painterResource(R.drawable.person_sillouette)
                }else {
                    rememberAsyncImagePainter(image)
                },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
        } else {
            Image(
                painter = rememberAsyncImagePainter(model = ""),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        Column(
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
        ) {
            //----------------- name and time
            Row(
                horizontalArrangement = if (!isFromMe) Arrangement.Start else Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (!isFromMe && showProfileImage) {
                    Text(
                        text =
                        if (anon){
                            ""
                        }else {
                            message.senderName
                        },
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp,
                        color = if (game) Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                if (showProfileImage) {
                    if (!anon){
                        Text(
                            formattedDayTimestamp(message.timestamp),
                            fontWeight = FontWeight.Light,
                            color = if (game) Color.White else Color.Black
                        )
                    }
                }

            }
            //--------------------------- Text Message
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = if (!isFromMe) 0.dp else borderRad,
                            topEnd = if (!isFromMe) borderRad else 0.dp,
                            bottomStart = borderRad,
                            bottomEnd = borderRad
                        )
                    )
                    .background(if (!isFromMe) CRAppTheme.colorScheme.primary else CRAppTheme.colorScheme.onBackground)
                    .padding(10.dp),

                ) {

                Text(
                    text = message.message,
                    color = Color.Black,
                    lineHeight = 23.sp,
                    letterSpacing = 1.sp
                )

            }
        }
    }

}
@Composable
fun ChatInput(viewModel: ChatViewModel = viewModel(), crRoomId: String, roomId: String, game: Boolean, mainChat: Boolean) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    Log.d("Debug-Message", "Current user: ${currentUser?.uid}")
    var input by remember { mutableStateOf("") }

    fun send(){
        if (input.isNotBlank()) {
            viewModel.sendMessage(crRoomId = crRoomId, roomId = roomId, message = input, game = game, mainChat = mainChat)
            input = ""
        }
    }

    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {

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
        IconButton(onClick = {
            send()
        }) {
            Icon(Icons.AutoMirrored.Default.Send, contentDescription = "")
        }

    }

}
@Composable
fun AlertDialogSplash(
    crRoomId: String,
    game: Boolean = false,
    rank: Boolean = false,
    system: Boolean = false,
    gameInfo: Title? = null,
    onDone: () -> Unit,
    crViewModel: ChatRiseViewModel = viewModel()
){
    val pitch0 = "Alert!"
    val pitch1 =
        when{
            game ->
                gameInfo?.let { game ->
                    "You will now Play\n\n\n${game.title}"
                } ?: "Game Information Not Available"

            rank -> "This is rank"
            system -> "this is system"
            else -> "No Specific Alert type"

        }
    val pitch2 = "You will be presented with a series of questions"
    val pitch3 =
        when(gameInfo?.type){
            "agree/disagree" -> "Respond with\n\n\nAgree or Disagree"
            "yes/no" -> "Respond with\n\n\nYes or No"
            else -> "nothing"
        }
    val texts = listOf(pitch0, pitch1, pitch2, pitch3)
    var currentIndex by remember { mutableStateOf(0)}
    val isLastItem = currentIndex == texts.size -1

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(CRAppTheme.colorScheme.onGameBackground)
            .clickable {
                if (isLastItem){
                    onDone()
                    when{
                        game -> {
                            gameInfo?.let { game ->
                                crViewModel.updateGameAlertStatus(
                                    crRoomId = crRoomId,
                                    gameName = game.title,
                                    hadAlert = true
                                )
                            }
                        }
                        rank -> {

                        }
                        system -> {

                        }
                        else -> {

                        }
                    }
                } else {
                    currentIndex++
                }
                    //currentIndex = (currentIndex + 1) % texts.size
            }
    ){
        Text(
            texts[currentIndex],
            style = if (texts[currentIndex] == pitch0) CRAppTheme.typography.H7 else CRAppTheme.typography.H4,
            textAlign = TextAlign.Center,
            color = Color.Red
        )
    }
}

