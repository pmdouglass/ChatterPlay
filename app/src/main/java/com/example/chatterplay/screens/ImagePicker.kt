package com.example.chatterplay.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.data_class.UserState
import com.example.chatterplay.data_class.UserState.Loading
import com.example.chatterplay.data_class.UserState.Success
import com.example.chatterplay.data_class.uriToByteArray
import com.example.chatterplay.seperate_composables.rememberProfileState
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ImagePickerScreen(navController: NavController, viewModel: ChatViewModel = viewModel()) {




    val context = LocalContext.current
    val userState = viewModel.userState.value
    var currentUserState by remember { mutableStateOf("")}

    var imageUri by remember{ mutableStateOf<Uri?>(null)}

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ){uri: Uri? ->
        imageUri = uri
    }

    var imageUrl by remember{ mutableStateOf("")}




    Column (
        modifier = Modifier
            .fillMaxSize()
    ){
        Button(onClick = {
            viewModel.createBucket("photo")
        }){
            Text("Create Bucket")
        }
        Button(onClick = {
            launcher.launch("image/*")
        }){
            Text("Select an Image")
        }
        if (imageUri != null){
            Button(onClick = {
                val imageByteArray = imageUri?.uriToByteArray(context)
                imageByteArray?.let {
                    viewModel.uploadImage("photo", "newImage", it)
                }
            }){
                Text("Upload Image")
            }
        }
        Button(onClick = {
            viewModel.readPublicFile("photo", "newImage"){
                imageUrl = it
            }
        }){
            Text("Get File")
        }
        Button(onClick = {
            val imageByteArray = imageUri?.uriToByteArray(context)
            imageByteArray?.let {
                viewModel.uploadImage("photo", "userId", it)
            }
        }){
            Text("Get and Upload File")
        }


        when (userState){
            is Loading -> {
                Text("Loading....")
            }
            is Success -> {
                val message = (userState as Success).message
                currentUserState = message
            }
            is Error -> {
                val message = (userState as UserState.Error).message
                currentUserState = message
            }
            else -> {
                Text("Is Nothing")
            }
        }


        Text("UserId: || userId ||  is the first name")
        Text(currentUserState)
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = null
        )


    }


}
