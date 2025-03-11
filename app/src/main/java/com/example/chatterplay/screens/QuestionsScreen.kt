package com.example.chatterplay.screens

import android.content.Context
import android.os.Bundle
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatterplay.MainActivity
import com.example.chatterplay.R
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.analytics.ScreenPresenceLogger
import com.example.chatterplay.data_class.Answers
import com.example.chatterplay.data_class.Title
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatRiseViewModel
import com.example.chatterplay.view_model.ChatRiseViewModelFactory
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun QuestionsScreen(
    crRoomId: String,
    gameInfo: Title,
    AllRisers: List<UserProfile>,
    viewModel: ChatViewModel = viewModel()
){
    // Create SharedPreferences
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Initialize ChatRiseViewModel with the factory
    val crViewModel: ChatRiseViewModel = viewModel(
        factory = ChatRiseViewModelFactory(sharedPreferences, viewModel)
    )

    Log.d("QuestionsScreen", "Inside QuestionsScreen")
    val question by crViewModel.currentQuestion.collectAsState()
    val allAnswers = remember { mutableStateOf<List<Answers>>(emptyList())}
    val usersAnswer by crViewModel.userAnswer.collectAsState()
    val done by crViewModel.allDoneAnswering.collectAsState()


    LaunchedEffect(Unit){
        crViewModel.startListeningForAllAnswered(crRoomId, gameInfo, AllRisers, context)
    }
    LaunchedEffect(gameInfo){
        crViewModel.fetchQuestionForUser(crRoomId, gameInfo.title) // initialize question
        crViewModel.fetchUsersAnswers(crRoomId, gameInfo.title) // initialize usersAnswer
        crViewModel.fetchAnswers(crRoomId, gameInfo.title){retrievedAnswers -> // allAnswers
            allAnswers.value = retrievedAnswers
        }


    }

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    LaunchedEffect(Unit){
        // Log the event in Firebase Analytics
        val params = Bundle().apply {
            putString("screen_name", "QuestionsScreen")
            putString("user_id", userId)
            putString("timestamp", System.currentTimeMillis().toString())
        }
        AnalyticsManager.getInstance(context).logEvent("screen_view", params)
    }
    ScreenPresenceLogger(screenName = "QuestionsScreen", userId = userId)
    (context as? MainActivity)?.setCurrentScreen(("QuestionsScreen"))



    /*val isLoading = remember {
        question == null ||
                hasAnswered == null ||
                usersAnswer == null ||
                //(isAllDoneWithQuestions == false && answers.value.isEmpty())
    }

     */
    //Log.d("QuestionsScreen", "isLoading: $isLoading")
    Log.d("QuestionsScreen", "question: $question")
    Log.d("QuestionsScreen", "usersAnswer: $usersAnswer")
    //Log.d("QuestionsScreen", "isAllDoneWithQuestions: $isAllDoneWithQuestions")
    Log.d("QuestionsScreen", "answers: ${allAnswers.value}")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        if (question != null){
            Log.d("QuestionsScreen", "questions is not null")
            question?.let { gameQuestion ->
                Text(
                    gameInfo.title,
                    style = CRAppTheme.typography.H2,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                )
                if (!done){
                    Log.d("QuestionsScreen", "inside not all done")
                    Text(
                        "Your responses are completely anonymous, allowing you to answer freely and honestly. Only you can choose to reveal your identity—the game will never share or disclose it, ensuring total confidentiality.",
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        modifier = Modifier
                            .padding(bottom = 60.dp)
                    )

                    AnonBubble(
                        message = gameQuestion.question,
                        systemMessage = true
                    )

                    Spacer(modifier = Modifier.padding(10.dp))
                    AnonBubble(
                        message = when {
                            usersAnswer != null -> usersAnswer!!.choice
                            else -> null
                        }
                    )
                    if (usersAnswer != null){
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                        ){
                            Text("Waiting on Others . . .", color = Color.White)
                        }
                    }else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    if (usersAnswer == null){
                        questionSend(
                            crRoomId = crRoomId,
                            gameInfo = gameInfo,
                            question = gameQuestion.question
                        )
                    }
                }else {
                    // everyones answer
                    Log.d("QuestionsScreen", "Inside Lazycolumn")
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(50.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                    ){
                        items(allAnswers.value){ answer ->
                            AnonBubble(
                                message = answer.question,
                                systemMessage = true
                            )
                            AnonBubble(
                                message = answer.choice
                            )
                        }
                    }
                }


            }

        }else {
            Log.d("QuestionsScreen", "questions is null")

            Text("Questions is Null")
        }

    }
}


@Composable
fun questionSend(
    crRoomId: String,
    gameInfo: Title,
    question: String,
    viewModel: ChatViewModel = viewModel()
){
    // Create SharedPreferences
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Initialize ChatRiseViewModel with the factory
    val crViewModel: ChatRiseViewModel = viewModel(
        factory = ChatRiseViewModelFactory(sharedPreferences, viewModel)
    )

    val currentUser = FirebaseAuth.getInstance().currentUser
    Log.d("Debug-Message", "Current user: ${currentUser?.uid}")
    var input by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    fun send(){
        val answer = Answers(
            userId = currentUser?.uid ?: "",
            question = question,
            crRoomId = crRoomId,
            title = gameInfo.title,
            choice = input
        )
        crViewModel.saveQuestionStatement(
            crRoomId = crRoomId,
            answer = answer,
            gameInfo = gameInfo,
            context = context
        )
        crViewModel.updateUserHasAnswered(
            crRoomId = crRoomId,
            gameInfo = gameInfo,
            context = context
        )

        input = ""
        sent = true
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
                unfocusedContainerColor = if (sent) Color.Red else Color.White,
                focusedContainerColor = if (sent) Color.Red else Color.White,
                focusedIndicatorColor = if (sent) Color.Red else Color.White,
                unfocusedIndicatorColor = if (sent) Color.Red else Color.White
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
fun AnonBubble(
    message: String? = null,
    systemMessage: Boolean = false
){
    val borderRad = 30.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter =
            if (!systemMessage)
                painterResource(R.drawable.person_sillouette)
            else
                painterResource(R.drawable.account_select_person2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            //--------------------------- Text Message
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = borderRad,
                            bottomStart = borderRad,
                            bottomEnd = borderRad
                        )
                    )
                    .background(if (!systemMessage) Color.White else Color.Gray)
                    .then(if (systemMessage)
                        Modifier.border(
                            1.dp,
                            Color.Red,
                            RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = borderRad,
                                bottomStart = borderRad,
                                bottomEnd = borderRad
                            )
                        )
                        else
                        Modifier
                    )
                    .padding(10.dp)                ,

                ) {
                Text(
                    text = message ?: "Type your response here. Once submitted, your reply will appear in this space.",
                    color = if (!systemMessage) Color.Black else Color.White,
                    lineHeight = 23.sp,
                    letterSpacing = 1.sp
                )

            }
        }
    }
}
