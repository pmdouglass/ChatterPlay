package com.example.chatterplay.seperate_composables

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.R
import com.example.chatterplay.data_class.ChatMessage
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.data_class.formattedDayTimestamp
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ChatLazyColumn(
    roomId: String,
    profile: UserProfile,
    game: Boolean,
    viewModel: ChatViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(roomId){
        viewModel.fetchChatMessages(roomId = roomId, game = game)
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
fun ChatBubble(
    message: ChatMessage,
    image: String,
    isFromMe: Boolean,
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
                painter = rememberAsyncImagePainter(image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
        } else{
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
        Column (
            horizontalAlignment = if(isFromMe) Alignment.End else Alignment.Start
        ){
            //----------------- name and time
            Row(
                horizontalArrangement = if (!isFromMe) Arrangement.Start else Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (!isFromMe && showProfileImage) {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                if (showProfileImage){
                    Text(formattedDayTimestamp(message.timestamp), fontWeight = FontWeight.Light)
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInput(
    viewModel: ChatViewModel = viewModel(),
    roomId: String,
    game: Boolean
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    Log.d("Debug-Message", "Current user: ${currentUser?.uid}")
    var input by remember { mutableStateOf("") }

    fun send(){
        if (input.isNotBlank()) {
            viewModel.sendMessage(roomId = roomId, message = input, game = game)
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
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
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
            Icon(Icons.Default.Send, contentDescription = "")
        }

    }

}

@Composable
fun ChatBubbleMock(game: Boolean, isFromMe: Boolean = false) {
    Row (
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
    ){
        if (!isFromMe){
            Image(
                painter = painterResource(id = R.drawable.cool_neon),
                contentDescription =null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column (
            modifier = Modifier
                .width(275.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(
                    if (isFromMe)
                        CRAppTheme.colorScheme.primary
                    else if (game)
                        CRAppTheme.colorScheme.onGameBackground
                    else
                        CRAppTheme.colorScheme.background

                )
                .border(
                    width = if (isFromMe) 2.dp else 0.dp,
                    color = if (isFromMe) CRAppTheme.colorScheme.highlight else Color.Transparent,
                    shape = RoundedCornerShape(15.dp)
                )
                .padding(6.dp)
        ){
            Column {
                if (!isFromMe){
                    Text("Tim C",
                        style = CRAppTheme.typography.titleMedium
                    )
                }
                Text(text = "I've been using your app nonstop, and I can't believe how intuitive and visually appealing it is—every detail seems so thoughtfully crafted, making each feature not just functional but also a joy to use, which is rare to find these days!",
                    style = CRAppTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp))
            }
        }


    }
}

@Composable
fun BottomInputBar() {

    var input by remember { mutableStateOf("")}

    Row (
        modifier = Modifier.fillMaxWidth()
    ){
        OutlinedTextField(
            value = input,
            onValueChange = { input = it},
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .background(Color.White)
        )
        IconButton(onClick = { /*TODO*/ },
            modifier = Modifier
                .background(Color.Blue)
                .height(50.dp)) {
            Icon(Icons.Default.Send, contentDescription = "" )
        }
    }
}

