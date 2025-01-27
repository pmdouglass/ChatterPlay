package com.example.chatterplay.screens

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.analytics.ScreenPresenceLogger
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.seperate_composables.DateDropDown
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

enum class profileInfo (val string: String){
    pname("Name"),
    age("Age"),
    location("Location"),
    about("About Me"),
    gender("Identify As")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen2(
    crRoomId: String,
    profile: UserProfile,
    game: Boolean,
    self: Boolean,
    isEditable: Boolean,
    viewModel: ChatViewModel = viewModel(),
    navController: NavController
){
    var roomId by remember { mutableStateOf<String?>(null)}
    //var noteInput by remember { mutableStateOf("")}
    var editProfile by remember { mutableStateOf(false)}
    var bigPicture by remember { mutableStateOf(false)}
    val picSize = if (bigPicture) 800 else 200
    val roomList by remember { mutableStateOf<List<UserProfile>>(emptyList())}


    Log.d("riser", "other userId is ${profile.userId}")

    LaunchedEffect(crRoomId){
        Log.d("riser", "inside profileScreen2 launched Effect")
        viewModel.fetchSingleRoom(crRoomId, profile.userId) { fetchedRoomId ->
            roomId = fetchedRoomId
        }
    }

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current
    LaunchedEffect(Unit){
        // Log the event in Firebase Analytics
        val params = Bundle().apply {
            putString("screen_name", "GameProfileScreen")
            putString("user_id", userId)
        }
        AnalyticsManager.getInstance(context).logEvent("screen_view", params)
    }
    ScreenPresenceLogger(screenName = "GameProfileScreen", userId = userId)


    Column (
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (game)
                    CRAppTheme.colorScheme.onGameBackground
                else CRAppTheme
                    .colorScheme.onBackground)
            .padding(10.dp)
            .verticalScroll(rememberScrollState())
    ){

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(picSize.dp)
        ){
            Image(
                painter = rememberAsyncImagePainter(profile.imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(picSize.dp - 50.dp)
                    .clickable { bigPicture = !bigPicture }
            )
            Box(
                modifier = Modifier
                    .then(if (game && !self) Modifier.size(50.dp) else Modifier.size(75.dp))
                    .align(Alignment.BottomStart)
                    .offset(x = 10.dp)
            ){
                Image(
                    rememberAsyncImagePainter(profile.imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(4.dp, if (game) CRAppTheme.colorScheme.gameBackground else CRAppTheme.colorScheme.background, CircleShape)
                )
            }
            if (editProfile){
                Button(onClick = {editProfile = false},
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                ){
                    Text("Save")
                }


                IconButton(onClick = {

                },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ){
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier
                            .size(25.dp)
                    )
                }
            }
        }
        Row (
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        ){
            Text(profile.fname,
                style = if (game && !self) CRAppTheme.typography.T3 else CRAppTheme.typography.T5,
                color = if (!game) Color.Black else Color.White
            )
            Text(profile.age,
                style = if (game && !self) CRAppTheme.typography.T3 else CRAppTheme.typography.T5,
                color = if (!game) Color.Black else Color.White,
                modifier = Modifier.padding(start = 10.dp)
            )
            Text(profile.gender,
                style = if (game && !self) CRAppTheme.typography.T3 else CRAppTheme.typography.T5,
                color = if (!game) Color.Black else Color.White,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
        Text(profile.location,
            color = if (!game) Color.Black else Color.White,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        if (editProfile){
            EditInfo(title = profileInfo.pname, game = game)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ){
                EditInfo(title = profileInfo.age, game = game)
                Spacer(modifier = Modifier.width(20.dp))
                EditInfo(title = profileInfo.location, game = game)
            }
            EditInfo(title = profileInfo.gender, game = game)
            EditInfo(title = profileInfo.about, game = game)
        }else {
            if (self){
                if (isEditable){
                    Button(onClick = {editProfile = true},
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)){
                        Row {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Edit Profile", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }else {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                ){
                    if (!game){
                        IconButton(
                            onClick = {  },
                            modifier = Modifier
                                .size(35.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        ){
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            if (roomId != null){
                                navController.navigate("chatScreen/${crRoomId}/${roomId}/true/false")
                            } else {
                                // create chat room and navigate

                                viewModel.createAndInviteToChatRoom(
                                    crRoomId = crRoomId,
                                    memberIds = roomList.map { it.userId }.toMutableList().apply { add(profile.userId) },
                                    roomName = profile.fname,
                                    onRoomCreated = { roomId ->
                                        navController.navigate("chatScreen/${crRoomId}/${roomId}/true/false")
                                    }
                                )
                                Log.d("riser", "RoomId is null")
                            }





                        },
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    ){
                        Icon(
                            Icons.AutoMirrored.Default.Message,
                            contentDescription = null
                        )
                    }

                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (game) CRAppTheme.colorScheme.gameBackground else CRAppTheme.colorScheme.background
                ),
                elevation = CardDefaults.cardElevation( 8.dp)
            ){
                Text("About Me", color = if (game) Color.White else Color.Black, modifier = Modifier.padding(8.dp))
                Text(profile.about, color = if (game) Color.White else Color.Black, modifier = Modifier.padding(8.dp))
                Text("Friends", color = if (game) Color.White else Color.Black, modifier = Modifier.padding(8.dp))
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        tint = if (game) Color.White else Color.Black,
                        modifier = Modifier
                            .size(23.dp)
                            .padding(start = 8.dp)
                    )
                    Text("155", color = if (game) Color.White else Color.Black, modifier = Modifier.padding(start = 4.dp))
                }
            }
            Spacer(modifier = Modifier.padding(10.dp))
            /*
            if (!self){
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (game) CRAppTheme.colorScheme.gameBackground else CRAppTheme.colorScheme.background
                    ),
                    elevation = CardDefaults.cardElevation( 8.dp)
                ){
                    Text("Notes", color = if (game) Color.White else Color.Black, modifier = Modifier.padding(8.dp))
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = {noteInput = it},
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,

                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }

             */
        }

    }
}




@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditInfo(
    title: profileInfo,
    game: Boolean
){
    var editInput by remember { mutableStateOf("")}
    var editLname by remember { mutableStateOf("")}

    Column(
        modifier = Modifier
            //.fillMaxWidth()
    ){
        Text(if (title == profileInfo.age)"" else title.string, style = CRAppTheme.typography.T4, color = if (game) Color.White else Color.Black, modifier = Modifier.padding(bottom = 4.dp))
        when (title){
            profileInfo.pname, profileInfo.about, profileInfo.location, profileInfo.gender -> {
                TextField(
                    value = editInput,
                    onValueChange = { newValue ->
                        editInput = when {
                            title == profileInfo.location -> {
                                val filteredValue = newValue.filter { it.isLetter() }
                                if (filteredValue.length <=2) filteredValue.uppercase() else editInput
                            }
                            else -> newValue
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = if (game) Color.White else Color.Black,
                        unfocusedTextColor = if (game) Color.White else Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = if (game) Color.LightGray else Color.Black,
                            shape = if (game) RoundedCornerShape(8.dp) else RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                        .clip(
                            shape = if (game) RoundedCornerShape(8.dp) else RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )

                if (!game && title == profileInfo.pname){
                    TextField(
                        value = editLname,
                        onValueChange = { editLname = it},
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                2.dp,
                                Color.Black,
                                RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                            )
                            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    )
                }
            }
            profileInfo.age -> {
                DateDropDown(age = true, game = game){selected -> editInput = selected}
            }
        }

    }
}