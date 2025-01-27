package com.example.chatterplay.screens.login

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatterplay.MainActivity
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.ui.theme.CRAppTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {




    var email by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("")}

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(Unit){
        // Log the event in Firebase Analytics
        val params = Bundle().apply {
            putString("screen_name", "LoginScreen")
        }
        AnalyticsManager.getInstance(context).logEvent("screen_view", params)
    }
    (context as? MainActivity)?.setCurrentScreen(("LoginScreen"))


    fun login(){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    coroutineScope.launch {
                        val userId = task.result?.user?.uid ?: "Unknown"
                        val loginMethod = "email"

                        // Log the login event
                        val params = Bundle().apply {
                            putString("user_id", userId)
                            putString("login_method", loginMethod)
                        }
                        AnalyticsManager.getInstance(context).logEvent("user_login", params)
                    }


                    navController.navigate("roomSelect")
                }else {
                    Log.e("LoginScreen", "Login failed: ${task.exception?.message}")
                }
            }
    }
    fun Quicklogin(){
        FirebaseAuth.getInstance().signInWithEmailAndPassword("doug@gmail.com", "qqqqqq")
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    navController.navigate("roomSelect")
                }
            }
    }


    Column (
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(CRAppTheme.colorScheme.background)
            .padding(26.dp)
    ){
        Text(
            text = "Login",
            style = CRAppTheme.typography.headingLarge,
            modifier = Modifier.padding(top = 20.dp, bottom = 50.dp)
            )
        TextField(
            value = email,
            onValueChange =  {email = it},
            placeholder = {Text("Email")},
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 50.dp)
        )

        TextField(
            value = password,
            onValueChange =  {password = it},
            placeholder = {Text("Password")},
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 50.dp)
        )

        Button(
            onClick = {
                      login()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 25.dp)
        ) {
            Text (
                "Login",
                style = CRAppTheme.typography.titleMedium,
            )
        }
        Row (
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ){
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword("doug@gmail.com", "qqqqqq")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                navController.navigate("roomSelect")
                            }
                        }
                },
                modifier = Modifier
                    .padding(bottom = 25.dp)
            ) {
                Text (
                    "phillip",
                    style = CRAppTheme.typography.titleMedium,
                )
            }
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword("chris@gmail.com", "qqqqqq")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                navController.navigate("roomSelect")
                            }
                        }
                },
                modifier = Modifier
                    .padding(bottom = 25.dp)
            ) {
                Text (
                    "chris",
                    style = CRAppTheme.typography.titleMedium,
                )
            }
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword("john@gmail.com", "qqqqqq")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                navController.navigate("roomSelect")
                            }
                        }
                },
                modifier = Modifier
                    .padding(bottom = 25.dp)
            ) {
                Text (
                    "john",
                    style = CRAppTheme.typography.titleMedium,
                )
            }
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword("jim@gmail.com", "qqqqqq")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                navController.navigate("roomSelect")
                            }
                        }
                },
                modifier = Modifier
                    .padding(bottom = 25.dp)
            ) {
                Text (
                    "jim",
                    style = CRAppTheme.typography.titleMedium,
                )
            }

        }


        Text(
            "Forgot Password?",
            modifier = Modifier
                .weight(1f)
        )


        Row (
            modifier = Modifier
                .padding(bottom = 20.dp)
        ){
            Text(
                "Don't have an account?"
            )

            Text(
                "  Create Account.",
                color = Color.Blue,
                modifier = Modifier
                    .clickable { navController.navigate("signupScreen1") }
            )

        }

    }

}
