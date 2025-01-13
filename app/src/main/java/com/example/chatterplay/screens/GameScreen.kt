package com.example.chatterplay.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
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
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatRiseViewModel

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
    val questions by crViewModel.gameQuestion.collectAsState()
    val currentQuestionIndex = remember { mutableStateOf(0) }

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
                PairQuestion(
                    questions[currentQuestionIndex.value].Question,
                    onAnswer = {
                        currentQuestionIndex.value += 1 // next question
                    }
                )
            } else {
                Text(
                    "Done!",
                    color = Color.Black,
                    style = CRAppTheme.typography.H1,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
    }
}


@Composable
fun PairQuestion(
    question: String,
    onAnswer: () -> Unit
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
                        onAnswer()
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
                        onAnswer()
                    }
            )
        }
    }
}