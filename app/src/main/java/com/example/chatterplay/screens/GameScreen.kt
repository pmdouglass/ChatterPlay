package com.example.chatterplay.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.data_class.Answers
import com.example.chatterplay.data_class.Questions
import com.example.chatterplay.data_class.Title
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatRiseViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay


fun notes(){
    //               basic question an answer
//             OR   statement from game
    /*
    who ask's question? game or player?
    question can be public/anonymous and/or answers can be public/anonymous
    questions can be game targeted or to anyone
     */


//                  multiple choice
    /*
    questions asked by game
    answers a,b,c,d recorded and shows who answered what
     */


//               yes/no agree/disagree
    /*
    questions are created by game
    answers are 2 choice options
    answers recorded ans shows who answered what
     */


//                     which player
    /*
    game question = which player most likely
    answer selection of other players
     */


// abc and yes/no can be combined, multiple choice is either 2 or 4. questions reflect which mode multiple/pair

    /*
                                    game phases
     questions asked
     wait for all to complete
     show results
     */
}


@Composable
fun PairGameScreen(
    crRoomId: String,
    allChatRoomMembers: List<UserProfile>,
    crViewModel: ChatRiseViewModel = viewModel()
){
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val hasAnswered by crViewModel.isDoneAnswering // sees if current user done with answers
    val isAllDoneWithQuestions by crViewModel.isAllDoneWithQuestions // waits until everyone done with answers
    val questions by crViewModel.gameQuestion.collectAsState()  // gets questions from supabase "questions" table
    val gameInfo by crViewModel.gameInfo.collectAsState()  // gets gameInfo from UserProfile



    LaunchedEffect(crRoomId) {

        crViewModel.getGameInfo(crRoomId)  // initialize 'gameInfo'
        crViewModel.monitorUntilAllUsersDoneAnsweringQuestions(crRoomId) // initialize 'isAllDoneWithQuestions'

    }
    LaunchedEffect(gameInfo){
        gameInfo?.let { game ->
            crViewModel.checkForUsersCompleteAnswers(crRoomId, game.id, userId) // initialize 'hasAnswered'
            crViewModel.fetchQuestions(game.id) // initialize 'questions'
        }
    }

    if (gameInfo != null){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CRAppTheme.colorScheme.onGameBackground)
        ) {
            if (!isAllDoneWithQuestions) {
                PairQuestions(
                    crRoomId = crRoomId,
                    hasAnswered = hasAnswered,
                    gameInfo = gameInfo!!,
                    questions = questions
                )
            } else {
                PairAnswerScreen(
                    gameInfo = gameInfo!!,
                    allChatRoomMembers = allChatRoomMembers
                )
            }
        }
        Column(){Text("gameInfo is null")}
    }else {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(CRAppTheme.colorScheme.onGameBackground)
        ) {
            Text(
                "No Game's Playable",
                style = CRAppTheme.typography.H1,
                color = Color.White
            )
        }
    }
}
@Composable
fun PairQuestions(
    crRoomId: String,
    hasAnswered: Boolean,
    gameInfo: Title,
    questions: List<Questions>,
    crViewModel: ChatRiseViewModel = viewModel()
){
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val currentQuestionIndex = remember { mutableStateOf(0) }
    val recordedAnswers = remember { mutableStateListOf<Answers>()}


    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ){
        Text(
            gameInfo.title,
            style = CRAppTheme.typography.H2,
            modifier = Modifier
                .padding(20.dp)
        )
        if(questions.isEmpty()){
            Text(
                "Loading questions...",
                color = Color.Black,
                style = CRAppTheme.typography.H1
            )
        } else {
            if (!hasAnswered){
                if (currentQuestionIndex.value < questions.size){
                    val currentQuestion = questions[currentQuestionIndex.value]
                    PairStructure(
                        currentQuestion.Question,
                        gameInfo = gameInfo,
                        onAnswer = { answer ->
                            recordedAnswers.add(
                                Answers(
                                    crRoomId = crRoomId,
                                    userId = userId,
                                    titleId = currentQuestion.TitleId,
                                    questionId = currentQuestion.id,
                                    question = currentQuestion.Question,
                                    answerPair = answer
                                )
                            )
                            currentQuestionIndex.value += 1 // next question
                        }
                    )
                } else {
                    // all finished operations

                    crViewModel.savePairAnswers(crRoomId, recordedAnswers)
                    /*crViewModel.addOrUpdateGame(
                        crRoomId = crRoomId,
                        gameName = gameTitle,
                        doneStatusUpdate = true
                    )*/
                    crViewModel.updateHasAnswered(
                        crRoomId = crRoomId,
                        questionsComplete = true
                    )
                }
            } else {
                GameWaiting(10)
            }
        }
    }
}
@Composable
fun GameWaiting(seconds: Int){
    val countdownTime = remember { mutableStateOf(seconds) }
    LaunchedEffect(Unit){
        while (true){
            for (time in seconds downTo 0){
                countdownTime.value = time
                delay((1000L * seconds) / 10)
            }
        }
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
    ){
        Text(
            "Waiting for others to complete...",
            color = Color.White,
            style = CRAppTheme.typography.H1,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        Text(
            "Next check in: ${countdownTime.value} seconds",
            color = Color.White,
            style = CRAppTheme.typography.H2
        )
    }
}

@Composable
fun PairStructure(
    question: String,
    gameInfo: Title,
    onAnswer: (Boolean) -> Unit
){
    Column (
    ) {
        Text(
            question,
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp) // outside
                .padding(top = 20.dp)
                .background(CRAppTheme.colorScheme.gameBackground)
                .padding(15.dp) // inside
        )
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ){
            Text(
                "Yes",
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .width(100.dp)
                    .padding(10.dp) // outside
                    .background(CRAppTheme.colorScheme.gameBackground)
                    .padding(15.dp) // inside
                    .clickable {
                        onAnswer(true)
                    }
            )
            Text(
                "No",
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .width(100.dp)
                    .padding(10.dp) // outside
                    .background(CRAppTheme.colorScheme.gameBackground)
                    .padding(15.dp) // inside
                    .clickable {
                        onAnswer(false)
                    }
            )
        }
    }
}

@Composable
fun PairAnswerScreen(
    gameInfo: Title,
    allChatRoomMembers: List<UserProfile>,
    crViewModel: ChatRiseViewModel = viewModel()
){
    val answers = remember { mutableStateOf<List<Answers>>(emptyList())}
    val userProfiles = remember { mutableStateOf<Map<String, UserProfile>>(emptyMap())}


    LaunchedEffect(true){

        crViewModel.fetchPairAnswers(gameInfo.id) {retrievedAnswers ->
            answers.value = retrievedAnswers
        }

        val members = allChatRoomMembers
        userProfiles.value = members.associateBy { it.userId }
    }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(CRAppTheme.colorScheme.onGameBackground)
            .padding(16.dp)
    ){
        if (answers.value.isEmpty()){
            Text(
                "No answers found",
                color = Color.Gray,
                style = CRAppTheme.typography.H1
            )
        }else {
            Text(
                gameInfo.title,
                color = Color.White,
                style = CRAppTheme.typography.H1,
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )
            // display answers for each question individually
            answers.value.distinctBy { it.questionId }.forEach { userAnswer ->
                val yesUsers = answers.value.filter { it.questionId == userAnswer.questionId && it.answerPair }
                    .mapNotNull { userProfiles.value[it.userId] }
                val noUsers = answers.value.filter { it.questionId == userAnswer.questionId && !it.answerPair }
                    .mapNotNull { userProfiles.value[it.userId] }

                UsersPairAnswers(
                    userAnswer.question,
                    yesUser = yesUsers,
                    noUsers = noUsers
                )
            }
        }
    }
}

@Composable
fun UsersPairAnswers(
    question: String,
    yesUser: List<UserProfile>,
    noUsers: List<UserProfile>
){
    Column(
        modifier = Modifier
            .padding(top = 10.dp, bottom = 10.dp)
    ){
        val answers by remember { mutableStateOf(Answers)}

        Text(
            question,
            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .background(CRAppTheme.colorScheme.gameBackground)
                .padding(10.dp)
        )

        // yes row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .padding(start = 15.dp, end = 15.dp)
                .padding(5.dp)
                .background(CRAppTheme.colorScheme.gameBackground)
                .padding(5.dp)
        ){
            Text(
                "Yes",
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
            )
            yesUser.forEach { userProfile ->
                Image(
                    rememberAsyncImagePainter(userProfile.imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(start = 2.dp, end = 2.dp)
                        .size(25.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                )
            }
        }

        // No row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .padding(start = 15.dp, end = 15.dp)
                .padding(5.dp)
                .background(CRAppTheme.colorScheme.gameBackground)
                .padding(5.dp)
        ){
            Text(
                "No",
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
            )
            noUsers.forEach { userProfile ->
                Image(
                    rememberAsyncImagePainter(userProfile.imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(start = 2.dp, end = 2.dp)
                        .size(25.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Black, CircleShape)

                )
            }
        }

    }
}