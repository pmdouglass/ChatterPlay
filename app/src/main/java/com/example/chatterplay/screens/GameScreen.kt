package com.example.chatterplay.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatterplay.data_class.Answers
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatRiseViewModel
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

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



@Composable
fun GameScreen(crViewModel: ChatRiseViewModel = viewModel()){
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val questions by crViewModel.gameQuestion.collectAsState()
    val currentQuestionIndex = remember { mutableStateOf(0) }
    val recordedAnswers = remember { mutableStateListOf<Answers>()}

    LaunchedEffect(true){
        crViewModel.fetchQuestions(2)
    }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(CRAppTheme.colorScheme.onGameBackground)
    ){
        if(questions.isEmpty()){
            Text(
                "Loading questions...",
                color = Color.Black,
                style = CRAppTheme.typography.H1
            )
        } else {
            if (currentQuestionIndex.value < questions.size){
                val currentQuestion = questions[currentQuestionIndex.value]
                PairQuestion(
                    currentQuestion.Question,
                    onAnswer = { answer ->
                        recordedAnswers.add(
                            Answers(
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
                Text(
                    "Done!",
                    color = Color.Black,
                    style = CRAppTheme.typography.H1,
                    modifier = Modifier.padding(20.dp)
                )

                crViewModel.savePairAnswers(recordedAnswers, titleId = 2)
            }
        }
    }
}


@Composable
fun PairQuestion(
    question: String,
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
fun AnswerScreen(
    allChatRoomMembers: List<UserProfile>,
    crViewModel: ChatRiseViewModel = viewModel()
){
    val answers = remember { mutableStateOf<List<Answers>>(emptyList())}
    val userProfiles = remember { mutableStateOf<Map<String, UserProfile>>(emptyMap())}


    LaunchedEffect(true){
        crViewModel.fetchPairAnswers(titleId = 2) {retrievedAnswers ->
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
                "Answer History",
                color = Color.White,
                style = CRAppTheme.typography.H1,
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )
            // display answers for each question individually
            answers.value.distinctBy { it.questionId }.forEach { userAnswer ->
                val yesUsers = answers.value.filter { it.questionId == userAnswer.questionId && it.answerPair }
                    .mapNotNull { userProfiles.value[it.userId]?.fname }
                val noUsers = answers.value.filter { it.questionId == userAnswer.questionId && !it.answerPair }
                    .mapNotNull { userProfiles.value[it.userId]?.fname }

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
    yesUser: List<String>,
    noUsers: List<String>
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
                .padding(start = 15.dp, end = 15.dp)
                .padding(5.dp)
                .background(CRAppTheme.colorScheme.gameBackground)
                .padding(10.dp)
        ){
            Text(
                "Yes",
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
            )
            yesUser.forEach { firstName ->
                Text(
                    // for each userId that answered yes
                    firstName,
                    color = Color.White
                )
            }
        }

        // No row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp)
                .padding(5.dp)
                .background(CRAppTheme.colorScheme.gameBackground)
                .padding(10.dp)
        ){
            Text(
                "No",
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
            )
            noUsers.forEach { firstName ->
                Text(
                    // for each userId that answered no
                    firstName,
                    color = Color.White
                )
            }
        }

    }
}