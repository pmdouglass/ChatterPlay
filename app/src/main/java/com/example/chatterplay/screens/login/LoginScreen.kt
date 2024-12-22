package com.example.chatterplay.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chatterplay.ui.theme.CRAppTheme
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {




    var email by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("")}

    fun login(){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    navController.navigate("roomSelect")
                } else {

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
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
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
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
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

@Preview
@Composable
fun PreviewLogin() {
    CRAppTheme {
        Surface {
            LoginScreen(navController = rememberNavController())
        }
    }
}