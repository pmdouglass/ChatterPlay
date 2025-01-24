package com.example.chatterplay.screens
/*
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.chatterplay.data_class.Answers
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.data_class.askQuestion
import com.example.chatterplay.seperate_composables.rememberCRProfile
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatRiseViewModel
import com.google.firebase.auth.FirebaseAuth


@Composable
fun ask2(
    crRoomId: String,
    crViewModel: ChatRiseViewModel = viewModel()
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CRAppTheme.colorScheme.onGameBackground)
            .padding(10.dp)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = CRAppTheme.colorScheme.gameBackground),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {

        }
    }
}
@Composable
fun ask(
    crRoomId: String,
    allChatRoomMembers: List<UserProfile>,
    crViewModel: ChatRiseViewModel = viewModel()
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val userPairs by crViewModel.userPairs.collectAsState()
    val youAskPair = userPairs.find { it.first == userId }
    val yourQuestionPair = userPairs.find { it.second == userId }
    val yourPair by crViewModel.yourAnonQuestion
    val targetQuestion by crViewModel.yourQuestion
    val QuestionToYou = remember { mutableStateOf<askQuestion?>(null) }
    val gameInfo by crViewModel.gameInfo.collectAsState()  // gets current game from UserProfile
    val hasAsked by crViewModel.theyHaveAsked.collectAsState()


    LaunchedEffect(crRoomId) {
        crViewModel.getGameInfo(crRoomId)  // initialize 'gameInfo'
        youAskPair?.let { pair ->
            crViewModel.fetchToUserQuestion(
                crRoomId = crRoomId,
                userId = userId,
                toUserId = pair.second
            )
        }
        crViewModel.fetchTargetQuestion(crRoomId = crRoomId, userId = userId)

        gameInfo?.let { game ->
            crViewModel.fetchMysteryCallerPairs(
                crRoomId = crRoomId,
                gameName = game.title
            )
        }
    }

    Log.d("QuestionScreen", "yourAnonQuestion: ${yourPair}")
    Log.d("QuestionScreen", "QuestionToYou: ${QuestionToYou.value}")
    gameInfo?.let { game ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CRAppTheme.colorScheme.onGameBackground)
                .padding(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CRAppTheme.colorScheme.gameBackground),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                ) {
                    if (hasAsked){
                        YourQuestionToMember(
                            crRoomId = crRoomId,
                            pair = pair,
                            title = game.title,
                            question = question,
                            toYou = false
                        )
                    }else {
                        YouAskMember(
                            crRoomId = crRoomId,
                            title = game.title,
                            pair = pair
                        )
                    }














                    // current user asks a question
                    youAskPair?.let { pair ->
                        if (yourPair == null) {
                            Log.d("QuestionScreen", "yourAnonQuestion == null")
                            YouAskMember(
                                crRoomId = crRoomId,
                                title = game.title,
                                pair = pair
                            )

                        } else {
                            Log.d("QuestionScreen", "yourAnonQuestion != null")
                            yourPair?.let { question ->
                                YourQuestionToMember(
                                    crRoomId = crRoomId,
                                    pair = pair,
                                    title = game.title,
                                    question = question,
                                    toYou = false
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CRAppTheme.colorScheme.gameBackground),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                ) {
                    yourQuestionPair?.let { pairQuestion ->
                        if (targetQuestion == null) {
                            Log.d("QuestionScreen", "target question is null")
                            Text("no Pair Found")
                        } else {
                            Log.d("QuestionScreen", "target question != null")
                            targetQuestion?.let { question ->
                                YourQuestionToMember(
                                    crRoomId = crRoomId,
                                    pair = pairQuestion,
                                    title = game.title,
                                    question = question,
                                    toYou = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TheAsk(
    crRoomId: String,
    pair: Pair<String, String>,
    crViewModel: ChatRiseViewModel = viewModel()
){
    Column(

    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp, bottom = 30.dp)
        ){

        }
    }
}

@Composable
fun YouAskMember(
    crRoomId: String,
    title: String,
    pair: Pair<String, String>,
    crViewModel: ChatRiseViewModel = viewModel()
){
    val profile = rememberCRProfile(crRoomId = crRoomId)
    val otherUser = remember { mutableStateOf<UserProfile?>(null)}
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(crRoomId){
        crViewModel.fetchUserProfile(crRoomId, pair.second) { profile ->
            otherUser.value = profile
        }
    }
    otherUser.value?.let { them ->
        Column{
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, bottom = 30.dp)
            ){
                Text(
                    "Ask or share anything with ${them.fname} anonymously—your identity will stay completely confidential.",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Image(
                        painterResource(R.drawable.person_sillouette),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        "Anonymous",
                        color = Color.White
                    )
                }
                Text(
                    "To",
                    style = CRAppTheme.typography.H2,
                    color = Color.White
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ){

                    Image(
                        rememberAsyncImagePainter(them.imageUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                    )

                    Text(
                        text =
                        them.fname,
                        color = Color.White
                    )

                }

            }
            Spacer(modifier = Modifier.height(10.dp))
            AnonSend(
                crRoomId = crRoomId,
                title = title,
                otherUser = pair.second
            )
        }
    }

}
@Composable
fun YourQuestionToMember(
    crRoomId: String,
    pair: Pair<String, String>,
    title: String,
    question: askQuestion,
    toYou: Boolean,
    crViewModel: ChatRiseViewModel = viewModel()
){
    val otherUser = remember { mutableStateOf<UserProfile?>(null)}
    val hasAnsweredQuestion by crViewModel.hasAnsweredAnonQuestion

    LaunchedEffect(crRoomId){
        crViewModel.fetchUserProfile(crRoomId, pair.second) { profile ->
            otherUser.value = profile
        }
        crViewModel.fetchAnonAnswer(crRoomId, question = question.question)
    }

    otherUser.value?.let { them ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ){
            Text(
                "To ",
                style = CRAppTheme.typography.H1,
                color = Color.White
            )
            Image(
                rememberAsyncImagePainter(them.imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Black, CircleShape)
            )
            Text(
                if (toYou) " You" else " ${them.fname}",
                style = CRAppTheme.typography.H1,
                color = Color.White
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ){
            AnonChatbubble(
                message = question.question,
                senderName = "Anon",
                image = "",
                isFromMe = false,
                anon = true
            )
            if (hasAnsweredQuestion){
                AnonChatbubble(
                    message = "Hello there howlksdfkj sdf lskdjf ll;kesjkgernlk;gmpwoeg kljdrnm;l a s,v b ,.d,;f v ldsn ,mm,n ctuyjl, n b hjgtv  do you do?",
                    senderName = "Phillip",
                    image = "",
                    isFromMe = false,
                    anon = false
                )
            }
            if (toYou){
                AnonSend(
                    crRoomId = crRoomId,
                    title = title,
                    question = question.question,
                    reply = true,
                )
            }
        }
    }

}




@Composable
fun AnonChatbubble(
    message: String,
    senderName: String,
    image: String,
    isFromMe: Boolean,
    anon: Boolean = false
){
    val borderRad = 30.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        // ---------------- if left than members picture
        if (!isFromMe) {
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
                if (!isFromMe) {
                    Text(
                        text =
                        if (anon){
                            ""
                        }else {
                            senderName
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
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
                    .background(
                        if (anon){
                            Color.Gray
                        }else {
                            if (!isFromMe){
                                CRAppTheme.colorScheme.primary
                            }else {
                                CRAppTheme.colorScheme.onBackground
                            }
                        }
                    )
                    .padding(10.dp),

                ) {

                Text(
                    text = message,
                    color = Color.Black,
                    lineHeight = 23.sp,
                    letterSpacing = 1.sp
                )

            }
        }
    }
}
@Composable
fun AnonSend(
    crRoomId: String,
    title: String,
    question: String? = null,
    reply: Boolean = false,
    otherUser: String? = null,
    crViewModel: ChatRiseViewModel = viewModel()
){
    val currentUser = FirebaseAuth.getInstance().currentUser
    Log.d("Debug-Message", "Current user: ${currentUser?.uid}")
    var input by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false)}

    fun send(){
        if (input.isNotBlank()) {
            if (!reply && otherUser != null){
                val question = askQuestion(
                    crRoomId = crRoomId,
                    question = input,
                    userId = currentUser?.uid ?: "",
                    toUserId = otherUser
                )
                crViewModel.saveTargetQuestion(crRoomId = crRoomId, title = title, userId = currentUser?.uid ?: "", toUserId = otherUser ,question = question)
            } else {
                if (title != null && question != null){
                    val answer = Answers(
                        userId = currentUser?.uid ?: "",
                        questionId = 0,
                        question = question,
                        crRoomId = crRoomId,
                        title = title,
                        choice = input
                    )
                    crViewModel.saveAnonAnswer(answer)
                }

            }
            input = ""
            sent = true
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
            if (!sent){
                send()
            }
        }) {
            Icon(Icons.AutoMirrored.Default.Send, contentDescription = "")
        }

    }
}




@Composable
fun YourQuekjnstionToMember(
    crRoomId: String,
    title: String,
    user: UserProfile,
    question: String,
    toYou: Boolean,
    crViewModel: ChatRiseViewModel = viewModel()
){

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val userPairs by crViewModel.userPairs.collectAsState()
    val youFirst = userPairs.find { it.first == userId }
    val youSecond = userPairs.find { it.second == userId }

    // question = find pair for you AND from you your question their question
    val hasAnswered = question touserId
    val yourQuestion
    val theirQuestion
    val yourAnswer
    val theirAnswer

    LaunchedEffect(crRoomId){
        // load hasAsked
        crViewModel.fetchMysteryCallerPairs(crRoomId = crRoomId, gameName = title) // initiate userPairs
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
    ){
        Text(
            "To ",
            style = CRAppTheme.typography.H1,
            color = Color.White
        )
        Image(
            rememberAsyncImagePainter(user.imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Black, CircleShape)
        )
        Text(
            if (toYou) "You" else user.fname,
            style = CRAppTheme.typography.H1,
            color = Color.White
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ){
        AnonChatbubble(
            message =
            if (toYou) theirQuestion else yourQuestion,
            senderName = "Anon",
            image = "",
            isFromMe = false,
            anon = true
        )
        if (hasAnswered){
            AnonChatbubble(
                message =
                if (toYou) yourAnswer else theirAnswer,
                senderName = "Phillip",
                image = "",
                isFromMe = false,
                anon = false
            )
        }

        if (toYou && !hasAnswered){
            AnonSend(
                crRoomId = crRoomId,
                title = title,
                question = question,
                reply = true,
            )
        }
    }

}*/