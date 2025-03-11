package com.example.chatterplay.screens.login

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.MainActivity
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.data_class.DateOfBirth
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.data_class.uriToByteArray
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@Composable
fun SignupScreen4(
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
    about: String,
    navController: NavController,
    game: Boolean,
    viewModel: ChatViewModel = viewModel()
) {

    val space = 15
    val coroutineScope = rememberCoroutineScope()
    var showPopUp by remember { mutableStateOf(false)}

    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var byteArray by remember { mutableStateOf<ByteArray?>(null)}

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
        byteArray = uri?.uriToByteArray(context)
    }

    (context as? MainActivity)?.setCurrentScreen(("SignupScreen4"))

    Column (
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(if (game) CRAppTheme.colorScheme.gameBackground else CRAppTheme.colorScheme.background)
            .padding(26.dp)
    ){
        Text(
            text = "Let's get your photo",
            textAlign = TextAlign.Center,
            style = CRAppTheme.typography.H3,
            color = if (game) Color.White else Color.Black,
            modifier = Modifier.padding(top = 20.dp, bottom = space.dp)
        )
        Text(
            "Let's upload a photo of you.",
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = if (game) Color.White else Color.Black,
            modifier = Modifier
                .padding(bottom = 100.dp)
        )


        Box(
            modifier = Modifier
                .size(250.dp)
        ){
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Black, CircleShape)
            ){
                if (imageUri != null){
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                } else {
                    Text("No Image Selected",
                        modifier = Modifier
                            .align(Alignment.Center)
                        )
                }
            }
            Box(
                modifier = Modifier
                    .size(75.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Black, CircleShape)
                    .align(Alignment.BottomEnd)
                    .background(CRAppTheme.colorScheme.background)
            ){


                IconButton(onClick = {
                                     launcher.launch("image/*")
                    if (imageUri == null){
                        Log.d("Test Message", "Image selection was canceled")
                    }
                },
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                    ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)

                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = {
            if (month.isBlank() || day.isBlank() || year.isBlank()){
                showPopUp = true
                return@Button
            }
            val dob = DateOfBirth(
                month = month,
                day = day,
                year = year
            )
            if (imageUri != null && fName.isNotBlank() && lName.isNotBlank() && age.isNotBlank() && gender.isNotBlank()){
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser?.uid

                if (currentUser == null) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val newUserId = task.result?.user?.uid ?: return@addOnCompleteListener
                                if (byteArray != null){
                                    viewModel.selectUploadAndGetImage(game = false, newUserId, byteArray!!) {url, error ->
                                        if (url != null) {
                                            val newUserProfile = UserProfile(
                                                userId = newUserId,
                                                fname = fName,
                                                lname = lName,
                                                gender = gender,
                                                dob = dob,
                                                age = age,
                                                location = location,
                                                imageUrl = url,
                                                about = about,
                                            )
                                            viewModel.saveUserProfile(
                                                context = context,
                                                userId = newUserId,
                                                userProfile = newUserProfile,
                                                game = false
                                            )
                                            coroutineScope.launch {
                                                // Log the event in Firebase Analytics
                                                val params = Bundle().apply {
                                                    putString("age", age)
                                                    putString("gender", gender)
                                                    putString("location", location)
                                                }
                                                AnalyticsManager.getInstance(context).logEvent("new_user_created", params)
                                            }
                                            navController.navigate("loginScreen") {
                                                popUpTo("signupScreen1") { inclusive = true }
                                            }
                                        }
                                    }
                                }

                            }else {
                                Log.d("Test Message", "Error creating account: ${task.exception?.localizedMessage}")
                            }
                        }


                } else {
                    if (byteArray != null){
                        viewModel.selectUploadAndGetImage(
                            game = true,
                            currentUser,
                            byteArray!!
                        ) { url, error ->
                            if (url != null) {
                                val exsistingUserProfile = UserProfile(
                                    userId = currentUser,
                                    fname = fName,
                                    lname = lName,
                                    gender = gender,
                                    dob = dob,
                                    age = age,
                                    location = location,
                                    imageUrl = url,
                                    about = about,
                                )
                                viewModel.saveUserProfile(
                                    context = context,
                                    userId = currentUser,
                                    userProfile = exsistingUserProfile,
                                    game = true
                                )
                                coroutineScope.launch {
                                    // Log the event in Firebase Analytics
                                    val params = Bundle().apply {
                                        putString("age", age)
                                        putString("gender", gender)
                                        putString("location", location)
                                    }
                                    AnalyticsManager.getInstance(context).logEvent("alternate_profile_created", params)
                                }
                                repeat(3) {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }

                }
            } else {
                showPopUp = true
            }

        },
    modifier = Modifier.fillMaxWidth()) {
        Text("Create my Profile")
    }

        if (showPopUp){
            SimplePopupScreen(
                text = "Please fill out all the fields",
                showPopup = false,
                onDismissRequest = { showPopUp = false }
            )
        }


    }

}
