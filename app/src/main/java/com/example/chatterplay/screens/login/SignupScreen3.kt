package com.example.chatterplay.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chatterplay.ui.theme.CRAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen3(
    email: String,
    password: String,
    fName: String,
    lName: String,
    month: String,
    day: String,
    year: String,
    age: String,
    gender: String,
    location: String,
    navController: NavController,
    game: Boolean
) {

    val space = 15

    var about by remember { mutableStateOf("") }

    Column (
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(if(game) CRAppTheme.colorScheme.gameBackground else CRAppTheme.colorScheme.background)
            .padding(26.dp)
    ){
        Text(
            text = "Tell us more about yourself",
            textAlign = TextAlign.Center,
            style = CRAppTheme.typography.H3,
            color = if (game) Color.White else Color.Black,
            modifier = Modifier.padding(top = 20.dp, bottom = space.dp)
        )
        Text(
            "Describe a little about yourself",
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = if (game) Color.White else Color.Black,
            modifier = Modifier
                .padding(bottom = space.dp)
        )

        Text(
            "About me",
            style = CRAppTheme.typography.H0,
            color = if (game) Color.White else Color.Black,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        TextField(
            value = about,
            onValueChange =  {about = it},
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(bottom = space.dp)
                .border(2.dp, Color.Black)
        )





        Spacer(modifier = Modifier.weight(1f))

        Button(onClick =  {
            navController.navigate("signupScreen4/${email}/${password}/${fName}/${lName}/${month}/${day}/${year}/${age}/${gender}/${location}/${about}/$game")
                          },
            modifier = Modifier
                .padding(bottom = 50.dp)
                .fillMaxWidth()) {
            Text(
                "Next",
                style = CRAppTheme.typography.titleMedium
            )
        }


    }



}
