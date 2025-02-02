package com.example.chatterplay.seperate_composables

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.R
import com.example.chatterplay.data_class.AlertType
import com.example.chatterplay.data_class.ChatMessage
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.data_class.formattedDayTimestamp
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatRiseViewModel
import com.example.chatterplay.view_model.ChatRiseViewModelFactory
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
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()

    // Fetch chat messages when roomId or game changes
    LaunchedEffect(roomId, game) {
        viewModel.fetchChatMessages(context = context, crRoomId = crRoomId, roomId = roomId, game = game, mainChat = mainChat)
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
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()

    // Fetch chat messages when roomId or game changes
    LaunchedEffect(roomId) {
        viewModel.fetchChatMessages(context = context, crRoomId = crRoomId, roomId = roomId, game = true, mainChat = true)
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
fun ChatInput(viewModel: ChatViewModel = viewModel(), memberCount: Int, crRoomId: String, roomId: String, game: Boolean, mainChat: Boolean) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    Log.d("Debug-Message", "Current user: ${currentUser?.uid}")
    var input by remember { mutableStateOf("") }
    val context = LocalContext.current

    fun send(){
        if (input.isNotBlank()) {
            viewModel.sendMessage(
                context = context,
                crRoomId = crRoomId,
                roomId = roomId,
                message = input,
                memberCount = memberCount,
                game = game,
                mainChat = mainChat
            )
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
fun AlertingScreen(
    crRoomId: String,
    onDone: () -> Unit
){

    // Create SharedPreferences
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Initialize ChatRiseViewModel with the factory
    val crViewModel: ChatRiseViewModel = viewModel(
        factory = ChatRiseViewModelFactory(sharedPreferences)
    )

    val systemAlertType by crViewModel.systemAlertType.collectAsState() // checks what AlertType it is
    val gameInfo by crViewModel.gameInfo.collectAsState() // gets gameInfo 'Title' from room

    LaunchedEffect(Unit){
        crViewModel.fetchSystemAlertType(crRoomId) // initialize systemAlertType

    }
    LaunchedEffect(systemAlertType){
        when (systemAlertType){
            AlertType.game.string -> {
                crViewModel.fetchGameInfo(crRoomId) // initialize 'gameInfo
            }
        }
    }


    val pitch0 = "Alert!"
    val pitch1 =
        when (systemAlertType){
            AlertType.none.string -> {""}
            AlertType.new_player.string -> {""}
            AlertType.game.string ->
                gameInfo?.let { game ->
                    "You will now Play\n\n\n${game.title}"
                } ?: "Game Information Not Available"
            AlertType.game_results.string -> {"The results are in"}
            AlertType.ranking.string -> "It's time to rank your fellow players and decide who stands out in the game."
            AlertType.rank_results.string -> {""}
            AlertType.blocking.string -> {""}
            else -> {""}

        }
    val pitch2 =
        when (systemAlertType) {
            AlertType.none.string -> {""}
            AlertType.new_player.string -> {""}
            AlertType.game.string ->
                gameInfo?.let { game ->
                    when (game.mode){
                        "questions" -> "You will be asked a Question"
                        else -> "You will be presented with a series of Questions"
                    }
                } ?: "Game Information Not Available"
            AlertType.game_results.string -> gameInfo?.let {game ->
                "See what everyone else answered for\n\n${game.title}"
            } ?: "Game Information Not Available"
            AlertType.ranking.string -> "Your rankings will shape the competition, so choose wisely and strategically."
            AlertType.rank_results.string -> {""}
            AlertType.blocking.string -> {""}
            else -> "nothing selected"
        }
    val pitch3 =
        when (systemAlertType) {
            AlertType.none.string -> {""}
            AlertType.game.string -> gameInfo?.let { game ->
                when(game.type){
                    "agree/disagree" -> "Respond with\n\n\nAgree or Disagree"
                    "yes/no" -> "Respond with\n\n\nYes or No"
                    "anonymousStatement" -> "Your Reply will be kept anonymous"
                    else -> "nothing"
                }
            } ?: "Game Information Not Available"
            AlertType.game_results.string -> {"The results May suprize you!"}
            AlertType.ranking.string -> "Remember, your decisions remain confidential, but your choices could change everything."
            AlertType.rank_results.string -> {""}
            AlertType.blocking.string -> {""}
            else -> "Nothing"
        }
    val texts = listOf(pitch0, pitch1, pitch2, pitch3)
    var currentIndex by remember { mutableStateOf(0)}
    val isLastItem = currentIndex == texts.size -1
    val transition = updateTransition(currentIndex, label = "Text Transition")
    val alpha by transition.animateFloat(label = "Alpha Transition") { index ->
        if (index == currentIndex) 1f else 0f
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(CRAppTheme.colorScheme.onGameBackground)
            .clickable(
                indication = rememberRipple(false),
                interactionSource = remember { MutableInteractionSource()}
            ) {
                if (isLastItem){
                    onDone()
                } else {
                    currentIndex++
                }
            }
    ){
        Text(
            texts[currentIndex],
            style = if (texts[currentIndex] == pitch0) CRAppTheme.typography.H7 else CRAppTheme.typography.H4,
            textAlign = TextAlign.Center,
            color = Color.Red,
            modifier = Modifier
                .alpha(alpha)
                .padding(horizontal = 16.dp)
        )
    }
}
/*
@Composable
fun AlertDialogSplash(
    crRoomId: String,
    game: Boolean = false,
    rank: Boolean = false,
    system: Boolean = false,
    gameInfo: Title? = null,
    onDone: () -> Unit
){
    // Create SharedPreferences
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Initialize ChatRiseViewModel with the factory
    val crViewModel: ChatRiseViewModel = viewModel(
        factory = ChatRiseViewModelFactory(sharedPreferences)
    )

    val pitch0 = "Alert!"
    val pitch1 =
        when{
            game ->
                gameInfo?.let { game ->
                    "You will now Play\n\n\n${game.title}"
                } ?: "Game Information Not Available"

            rank -> "It's time to rank your fellow players and decide who stands out in the game."
            system -> "this is system"
            else -> "No Specific Alert type"

        }
    val pitch2 =
        when {
            game ->
                gameInfo?.let { game ->
                    when (game.mode){
                        "questions" -> "You will be asked a Question"
                        else -> "You will be presented with a series of Questions"
                    }
                } ?: "Game Information Not Available"
            rank -> "Your rankings will shape the competition, so choose wisely and strategically."
            else -> "nothing selected"
        }
    val pitch3 =
        when {
            game -> gameInfo?.let { game ->
                when(game.type){
                    "agree/disagree" -> "Respond with\n\n\nAgree or Disagree"
                    "yes/no" -> "Respond with\n\n\nYes or No"
                    "anonymousStatement" -> "Reply anonymously"
                    else -> "nothing"
                }
            } ?: "Game Information Not Available"

            rank -> "Remember, your decisions remain confidential, but your choices could change everything."
            system -> "System Alert: Pleas pay attention to this important information."
            else -> "Nothing"
        }
    val texts = listOf(pitch0, pitch1, pitch2, pitch3)
    var currentIndex by remember { mutableStateOf(0)}
    val isLastItem = currentIndex == texts.size -1
    val transition = updateTransition(currentIndex, label = "Text Transition")
    val alpha by transition.animateFloat(label = "Alpha Transition") { index ->
        if (index == currentIndex) 1f else 0f
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(CRAppTheme.colorScheme.onGameBackground)
            .clickable(
                indication = rememberRipple(false),
                interactionSource = remember { MutableInteractionSource()}
            ) {
                if (isLastItem){
                    onDone()
                    when{
                        game -> {
                            crViewModel.updateShowAlert(
                                crRoomId = crRoomId,
                                alertStatus = true
                            )
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
            color = Color.Red,
            modifier = Modifier
                .alpha(alpha)
                .padding(horizontal = 16.dp)
        )
    }
}

 */

