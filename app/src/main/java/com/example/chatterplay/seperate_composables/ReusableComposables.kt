@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.chatterplay.seperate_composables

import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.R
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.ui.theme.darkPurple
import com.example.chatterplay.view_model.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatterplay.data_class.ChatMessage
import com.example.chatterplay.data_class.ChatRoom
import com.example.chatterplay.data_class.DateOfBirth
import com.example.chatterplay.data_class.formattedDayTimestamp
import com.example.chatterplay.data_class.uriToByteArray
import com.example.chatterplay.repository.fetchUserProfile
import com.example.chatterplay.screens.login.calculateAgeToDate
import com.example.chatterplay.screens.login.calculateBDtoAge
import com.example.chatterplay.view_model.RoomCreationViewModel
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate


enum class RowState (val string: String){
    none("nothing"),
    follow("follow"),
    check("check")
}

@Composable
fun rememberProfileState(userId: String, viewModel: ChatViewModel = viewModel()): Pair<UserProfile, UserProfile> {
    val personalState by viewModel.userProfile.collectAsState()
    val alternateState by viewModel.crUserProfile.collectAsState()

    val personalProfile = personalState ?: UserProfile()
    val alternateProfile = alternateState ?: UserProfile()

    LaunchedEffect(Unit) {
        viewModel.getUserProfile(userId = userId)
    }
    return  Pair(personalProfile, alternateProfile)

}

@Composable
fun ChatRiseThumbnail(
    viewModel: ChatViewModel = viewModel(),
    roomCreate: RoomCreationViewModel = viewModel(),
    navController: NavController
) {
    val email by remember { mutableStateOf("email")}
    val password by remember { mutableStateOf("password")}
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var selfSelect by remember { mutableStateOf(true)}
    var altSelect by remember { mutableStateOf(false)}
    val (personalProfile, alternateProfile) = rememberProfileState(userId = currentUserId, viewModel)

    val width = 100

    val userState by roomCreate.userState.collectAsState()
    val roomReady by roomCreate.roomReady.collectAsState()
    val CRRoomId by roomCreate.CRRoomId.collectAsState()

    var hasAlternateProfile = if (alternateProfile.fname.isNullOrBlank()) false else true


    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(CRAppTheme.colorScheme.onBackground)
                .border(2.dp, CRAppTheme.colorScheme.highlight, RoundedCornerShape(15.dp))
                .padding(start = 10.dp, end = 10.dp)
        ){
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Text(
                    text = "ChatRise",
                    style = CRAppTheme.typography.headingMedium,
                    modifier = Modifier
                        .then(if (userState == "NotPending") Modifier.padding(end = 20.dp) else Modifier.weight(1f))
                )
                when  {
                    userState == "NotPending" -> {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                                .clickable { navController.navigate("aboutChatrise") }
                        ) {
                            Icon(
                                Icons.Default.QuestionMark,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                    userState == "Pending" -> {}
                    roomReady -> {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "1.3k",
                                style = CRAppTheme.typography.infoMedium
                            )
                            Text(
                                "People",
                                style = CRAppTheme.typography.infoSmall
                            )
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        DynamicCircleBox(number = 121)
                    }
                }

            }
            when {
                userState == "NotPending" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ){
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                        ){
                            Column (
                                horizontalAlignment = Alignment.CenterHorizontally
                            ){
                                Text(
                                    "Play as"
                                )
                                Row (
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                    modifier = Modifier
                                ){

                                    Text(
                                        "Self",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .width(width.dp)
                                            .clip(RoundedCornerShape(25.dp))
                                            .border(2.dp, Color.Black, RoundedCornerShape(25.dp))
                                            .background(if (selfSelect) CRAppTheme.colorScheme.primary else CRAppTheme.colorScheme.background)
                                            .padding(3.dp)
                                            .clickable {
                                                selfSelect = true
                                                altSelect = false
                                            }
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Someone Else",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .width(width.dp)
                                            .clip(RoundedCornerShape(25.dp))
                                            .border(2.dp, Color.Black, RoundedCornerShape(25.dp))
                                            .background(if (altSelect) CRAppTheme.colorScheme.primary else CRAppTheme.colorScheme.background)
                                            .padding(3.dp)
                                            .clickable {
                                                selfSelect = false
                                                altSelect = true
                                            }
                                    )

                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ){
                                if (selfSelect){
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier
                                            .padding(end = 20.dp)
                                    ){
                                        Text(personalProfile.fname,
                                            fontSize = 10.sp
                                        )
                                        Text(personalProfile.age,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Image(
                                        painter = rememberAsyncImagePainter(personalProfile.imageUrl),
                                        contentDescription =null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                }else {
                                    if (hasAlternateProfile){
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            modifier = Modifier
                                                .padding(end = 20.dp)
                                        ){
                                            Text(alternateProfile.fname,
                                                fontSize = 10.sp
                                            )
                                            Text(alternateProfile.age,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Image(
                                            painter = rememberAsyncImagePainter(alternateProfile.imageUrl),
                                            contentDescription =null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    navController.navigate("signupScreen2/${email}/${password}/true")
                                                }
                                        )
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            modifier = Modifier
                                                .padding(end = 20.dp)
                                        ){
                                            Text("Create a profile",
                                                fontSize = 10.sp
                                            )
                                            Text("",
                                                fontSize = 10.sp
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                        ){
                                            IconButton(onClick = {
                                                navController.navigate("signupScreen2/${email}/${password}/true")

                                            }) {
                                                Icon(
                                                    Icons.Default.PersonAdd,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                )
                                            }
                                        }
                                    }

                                }
                            }

                        }
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier
                                .fillMaxWidth()
                        ){
                            Image(
                                painter = painterResource(id = R.drawable.account_select_person2),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .size(130.dp)

                            )
                            Button(onClick = {
                                val updatedProfile = when {
                                    selfSelect -> { personalProfile.copy(selectedProfile = "self")}
                                    altSelect -> { personalProfile.copy(selectedProfile = "alt")}
                                    else -> { personalProfile}
                                }
                                val altUpdatedProfile = when {
                                    selfSelect -> {alternateProfile.copy(selectedProfile = "self")}
                                    altSelect -> {alternateProfile.copy(selectedProfile = "alt")}
                                    else -> { alternateProfile}
                                }
                                viewModel.saveUserProfile(userId = currentUserId, userProfile = updatedProfile, game = false)
                                viewModel.saveUserProfile(userId = currentUserId, userProfile = altUpdatedProfile, game = true)
                                roomCreate.setToPending()
                            },
                                modifier = Modifier
                                    .padding(5.dp)
                            ) {
                                Text("Play")
                            }
                        }

                    }
                }
                userState == "Pending" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ){
                        Image(
                            painter = painterResource(id = R.drawable.waiting),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .size(130.dp)

                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                        ){

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ){
                                Image(
                                    painter = when {
                                        selfSelect -> { rememberAsyncImagePainter(personalProfile.imageUrl)}
                                        altSelect -> { rememberAsyncImagePainter(alternateProfile.imageUrl)}
                                        else -> { painterResource(R.drawable.anonymous)}
                                                   },
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                Text(
                                    text = when {
                                        selfSelect -> {personalProfile.fname}
                                        altSelect -> {alternateProfile.fname}
                                        else -> { "No Name"}

                                                },
                                    fontSize = 10.sp
                                )

                            }

                            Text(
                                "Waiting for\nOthers",
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(start = 5.dp, end = 5.dp)
                            )
                            AnimatedDots()
                            Image(
                                painter = painterResource(id = R.drawable.person_sillouette),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(45.dp)
                            )
                        }
                    }
                }
                roomReady -> {
                    /*CRRoomId?.let { roomId ->

                    }*/
                    Column(
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                navController.navigate("mainScreen")
                            }
                    ) {
                        Row (
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                        ){
                            Image(
                                painter = painterResource(id = R.drawable.cool_neon),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Hey all! Besides the gym, I'm big into hiking and mountain biking. Anyone else here into outdoor stuff?",
                                style = CRAppTheme.typography.bodySmall
                            )
                        }
                        Row (
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                        ){
                            Image(
                                painter = painterResource(id = R.drawable.cool_neon),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Nice. I love hikiing too! I'm also really into cooking, especially trying to make healthy versions of comfort foods. Any foodies here?",
                                style = CRAppTheme.typography.bodySmall
                            )
                        }
                        Row (
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                        ){
                            Image(
                                painter = painterResource(id = R.drawable.cool_neon),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "For sure! I'm more of a runner and a swimmer, but I love trying new recipes. Trying to get into yogo lately, too. What got you guys into your hobbies?",
                                style = CRAppTheme.typography.bodySmall
                            )
                        }
                    }
                }
                else -> {
                    Text("Nothing Selected")
                }


            }
        }
    }
}

@Composable
fun DynamicCircleBox(number: Int) {

    val circleSize = when(number.toString().length) {
        1 -> 40.dp
        2 -> 50.dp
        else -> 60.dp
    }

    if (number != 0) {
        Box(
            modifier = Modifier
                .height(30.dp)
                .width(circleSize)// Dynamic size based on the number
                .background(CRAppTheme.colorScheme.primary, CircleShape), // Circle shape
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = CRAppTheme.typography.infoLarge
            )
        }
    }
}

@Composable
fun PrivateGroupPicThumbnail(
    game: Boolean,
    room: ChatRoom,
    memberCount: Int,
    imageUrls: List<String>
) {



    val displayPics = when {
        memberCount == 2 -> 1
        memberCount >= 5 -> 5
        memberCount < 5 -> memberCount
        else -> 2
    }

    val boxWidth = 60.dp
    val imageSize = if (memberCount == 2) boxWidth else 25.dp
    val availableWidth = boxWidth - imageSize
    val overlapOffset = if (displayPics > 1) availableWidth / (displayPics -1) else 0.dp

    Box(
        modifier = Modifier
            .padding(6.dp)
            .size(boxWidth)
    ) {

        /*for (i in 0 until displayPics){
            if (i < imageUrls.size){
                Image(
                    rememberAsyncImagePainter(imageUrls[i]),
                    contentDescription = null,
                    modifier = Modifier
                        .size(imageSize)
                        .offset(
                            x = (i * overlapOffset.value).dp, y = if (memberCount >= 3) {
                                0.dp
                            } else {
                                0.dp
                            }
                        )
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }*/

        imageUrls.take(5).forEachIndexed { index, url ->
            Image(
                painter = rememberAsyncImagePainter(url),
                contentDescription = null,
                modifier = Modifier
                    .size(imageSize)
                    .offset(x = (index * overlapOffset.value).dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.LightGray, CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        // Text aligned to the bottom of the Box
        if (memberCount >= 3){
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ){
                Text(
                    text = "$memberCount",
                    style = CRAppTheme.typography.infoMedium,
                    color = if (game) Color.White else Color.Black
                )
                Text("People",
                    style = CRAppTheme.typography.infoSmall,
                    color = if (game) Color.White else Color.Black
                    )

            }
        }
    }
}

@Composable
fun RoomRow(members: Int, title: String, who: String, message: String, time: String, unread: Int, game: Boolean, navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp, bottom = 5.dp)
            .clickable {
                if (game) {
                    navController.navigate("chatScreen/true")
                } else {
                    navController.navigate("chatScreen/false")
                }
            }
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
        ){
            //PrivateGroupPicThumbnail(game, memberCount = members)
            Spacer(modifier = Modifier.width(3.dp))
            Column (
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ){
                    if (members <= 2){
                        Text(
                            who,
                            style = CRAppTheme.typography.titleMedium,
                            color = if (game) Color.White else Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                        )
                    } else {
                        Text(
                            title,
                            style = CRAppTheme.typography.titleMedium,
                            color = if (game) Color.White else Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
                    if (!game){
                        Text(
                            "10/12",
                            style = CRAppTheme.typography.infoMedium,
                            color = if (game) Color.White else Color.Black,
                        )
                        if (members >= 2){
                            Text(
                                time,
                                style = CRAppTheme.typography.infoMedium,
                                color = if (game) Color.White else Color.Black,
                                modifier = Modifier
                                    .padding(start = 10.dp, end = 5.dp)
                            )
                        }
                    }
                }
                if (members >= 2){

                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp)
                    ){
                        Image(
                            painter = painterResource(id = R.drawable.cool_neon),
                            contentDescription =null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(25.dp)
                                .clip(CircleShape)
                        )

                        Text(
                            who,
                            style = CRAppTheme.typography.bodyLarge,
                            color = if (game) Color.White else Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 5.dp)
                        )
                        if (!game){
                            Text(
                                "$time",
                                style = CRAppTheme.typography.infoMedium,
                                color = if (game) Color.White else Color.Black,
                                modifier = Modifier
                                    .padding(end = 5.dp)
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp)
                ){
                    Text(
                        "$message",
                        style = CRAppTheme.typography.bodySmall,
                        color = if (game) Color.White else Color.Black,
                        maxLines = 3,
                        modifier = Modifier
                            .weight(1f)
                    )
                    DynamicCircleBox(number = unread)
                }
            }
        }
        Divider(modifier = Modifier.padding(top = 10.dp, start = 15.dp, end = 15.dp))
    }
}

@Composable
fun RoomSelectionView(
    game: Boolean,
    room: ChatRoom,
    membersCount: Int,
    replyCount: Int,
    onClick: () -> Unit
) {

    val lastReplyImage = room.lastProfile
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val otherUserIds = room.members.filter {it != currentUserId}
    /*val otherUserProfile by produceState<UserProfile?>(initialValue = null, key1 = otherUserIds){
        value = otherUserIds.let { fetchUserProfile(it) }
    }*/
    val otherUserProfiles by produceState<List<UserProfile>>(initialValue = emptyList(), key1 = otherUserIds) {
        value = otherUserIds.mapNotNull { fetchUserProfile(it) }
    }
    val imageUrls = otherUserProfiles.mapNotNull { it.imageUrl }
    //val theirImage = otherUserProfile?.imageUrl
    val theirName = otherUserProfiles.joinToString(", ") { "${it.fname} ${it.lname}" }



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp, bottom = 5.dp)
            .clickable { onClick()}
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
        ){
            PrivateGroupPicThumbnail(
                game = game,
                room = room,
                memberCount = membersCount,
                imageUrls = imageUrls
                )
            Spacer(modifier = Modifier.width(3.dp))



            Column (verticalArrangement = Arrangement.Top, modifier = Modifier
                    .fillMaxWidth()){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ){
                    Text(
                        text =
                        if (membersCount <= 2)
                            theirName
                        else
                            room.roomName,
                        style = CRAppTheme.typography.titleMedium,
                        color = if (game) Color.White else Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                    )
                    Text(
                        formattedDayTimestamp(room.lastMessageTimestamp),
                        style = CRAppTheme.typography.infoMedium,
                        color = if (game) Color.White else Color.Black,
                    )
                }


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp)
                ){
                    if (membersCount >= 3){
                        Image(
                            painter = rememberAsyncImagePainter(lastReplyImage),
                            contentDescription =null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(25.dp)
                                .clip(CircleShape)
                        )
                    }

                    /*Text(
                        "Them",
                        style = CRAppTheme.typography.bodyLarge,
                        color = if (game) Color.White else Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 5.dp)
                    )
                    Text(
                        formattedDayTimestamp(room.lastMessageTimestamp),
                        style = CRAppTheme.typography.infoMedium,
                        color = if (game) Color.White else Color.Black,
                        modifier = Modifier
                            .padding(end = 5.dp)
                    )*/
                    Text(
                        room.lastMessage,
                        style = CRAppTheme.typography.bodySmall,
                        color = if (game) Color.White else Color.Black,
                        maxLines = 3,
                        modifier = Modifier
                            .weight(1f)
                    )
                    DynamicCircleBox(number = replyCount)

                }
            }
        }
        Divider(modifier = Modifier.padding(top = 10.dp, start = 15.dp, end = 15.dp))
    }
}

@Composable
fun MainTopAppBar(title: String, action: Boolean, actionIcon: ImageVector, onAction: () -> Unit, navController: NavController) {

    val pad = 15
    var expand by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!expand) Modifier.height(56.dp) else Modifier)
            .clickable { expand = !expand }
            .background(darkPurple)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(darkPurple)
        ) {
            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(35.dp)
                        .align(AbsoluteAlignment.CenterLeft)
                )
            }

            Text(
                text = title,
                style = CRAppTheme.typography.headingLarge,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
            )

            if (action) {
                IconButton(
                    onClick = { onAction() },
                    modifier = Modifier
                        .size(35.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        tint = Color.White
                    )
                }


            }
        }
        Image(
            painter = painterResource(id = R.drawable.anonymous),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable {
                    navController.navigate("profileScreen/true/true")
                }
        )

        Text(
            "Phillip Douglass",
            style = CRAppTheme.typography.headingLarge,
            color = Color.White,
            modifier = Modifier
                .padding(15.dp)
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                "Rating: 1",
                style = CRAppTheme.typography.infoMedium,
                color = Color.White,
                modifier = Modifier
                    .padding(start = pad.dp, end = pad.dp, bottom = pad.dp)
            )
            Text(
                "Chat Info: ",
                style = CRAppTheme.typography.infoMedium,
                color = Color.White,
                modifier = Modifier
                    .padding(start = pad.dp, end = pad.dp, bottom = pad.dp)
            )
            Text(
                "Chat Info: ",
                style = CRAppTheme.typography.infoMedium,
                color = Color.White,
                modifier = Modifier
                    .padding(start = pad.dp, end = pad.dp, bottom = pad.dp)
            )

        }

        Spacer(modifier = Modifier.height(pad.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            IconButton(onClick = {
                navController.navigate("mainScreen")
            }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            IconButton(onClick = {
                navController.navigate("profileScreen/true/true")
            }) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            IconButton(onClick = {

            }) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            IconButton(onClick = {

            }) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = Color.White
                )
            }

        }
        Spacer(modifier = Modifier.height(pad.dp + 10.dp))
    }
}

@Composable
fun RightSideModalDrawer(drawerContent: @Composable () -> Unit, modifier: Modifier = Modifier, drawerState: DrawerState = rememberDrawerState(
        DrawerValue.Closed
    ), content: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()
    val drawerWidth = 285.dp // Adjust the width as needed
    val drawerWidthPx = with(LocalDensity.current) { drawerWidth.toPx() }
    val closedOffsetX = drawerWidthPx
    val openOffsetX = 0f

    val offsetX by animateFloatAsState(
        targetValue = if (drawerState.isOpen) openOffsetX else closedOffsetX
    )

    Box(
        modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        content()

        if (drawerState.isOpen) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable {
                        scope.launch {
                            drawerState.close()
                        }
                    }
            )
        }

        Box(
            Modifier
                .offset { IntOffset(offsetX.toInt(), 0) }
                .fillMaxHeight()
                .width(drawerWidth)
                .background(darkPurple.copy(alpha = .8f))
                .border(
                    2.dp,
                    if (drawerState.isOpen) CRAppTheme.colorScheme.highlight else Color.Transparent
                )
                .padding(8.dp)
        ) {
            drawerContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateDrawerRoomList(onTap: () -> Unit, onLongPress: () -> Unit, navController: NavController, modifier: Modifier = Modifier) {
    var searchChats by remember{ mutableStateOf("") }



    Column(
        modifier = Modifier
            .fillMaxHeight()
    ){
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
        ){
            IconButton(onClick = { navController.navigate("inviteScreen/true")}) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = modifier
                        .size(35.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(200.dp))

        Text(
            "Conversations",
            style = CRAppTheme.typography.headingMedium,
            color = Color.White,
            modifier = Modifier.padding(start = 8.dp, bottom = 10.dp)
        )

        TextField(
            value = searchChats,
            onValueChange = {searchChats = it},
            placeholder = {Text("Search",
                style = CRAppTheme.typography.infoLarge,
                color = CRAppTheme.colorScheme.textOnGameBackground
            )},
            leadingIcon = {
                Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = CRAppTheme.colorScheme.textOnGameBackground

            )
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = CRAppTheme.colorScheme.onGameBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .clip(RoundedCornerShape(50.dp))

        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 20.dp)
        ){
            Text(
                "Private",
                style = CRAppTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                "Groups",
                style = CRAppTheme.typography.titleMedium,
                color = Color.White
            )


        }
        Divider(modifier = Modifier.padding(bottom = 20.dp))
        RoomRow(
            members = 13,
            title = "Coffee and Conversations",
            who = "Hunter D",
            message = "Perfect for early birds and night owls looking for warm vibes and meaningful exchanges.",
            time = " 6:20pm",
            unread = 7,
            game = true,
            navController = navController
        )
        RoomRow(
            members = 3,
            title = "Fitness Frenzy",
            who = "Kayla D",
            message = "Motivation central for gym-goers, runners, and yoga enthusiasts alike. Get your daily dose of workout tips, recipes, and accountability buddies!",
            time = " 6:20pm",
            unread = 2,
            game = true,
            navController = navController
        )
        RoomRow(
            members = 2,
            title = "Movie Buffs",
            who = "John L",
            message = "Just saw the latest thriller - the plot twist was insane!",
            time = " 6:20pm",
            unread = 1,
            game = true,
            navController = navController
        )
        RoomRow(
            members = 4,
            title = "Tech Talkers",
            who = "Bill K",
            message = "A geek's paradise for discussions on the latest gadgets, coding hacks, and tech trends.",
            time = " 6:20pm",
            unread = 701,
            game = true,
            navController = navController
        )

    }
}

@Composable
fun PersonRow(userProfile: UserProfile, PicSize: Int, txtSize: Int, modifier: Modifier, game: Boolean, self: Boolean, navController: NavController, chatRoomMembers: List<UserProfile>) {
    /*Row (
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
    ){
        PersonIcon(imgSize = PicSize, firstName = "Tim", txtSize = txtSize, game = game, self = self, navController = navController)
        PersonIcon(imgSize = PicSize, firstName = "Clay", txtSize = txtSize, game = game, self = self, navController = navController)
        PersonIcon(imgSize = PicSize, firstName = "Jason", txtSize = txtSize, game = game, self = self, navController = navController)
        PersonIcon(imgSize = PicSize, firstName = "Alexandria", txtSize = txtSize, game = game, self = self, navController = navController)
        PersonIcon(imgSize = PicSize, firstName = "Mammoa", txtSize = txtSize, game = game, self = self, navController = navController)
        PersonIcon(imgSize = PicSize, firstName = "Daddy", txtSize = txtSize, game = game, self = self, navController = navController)
        PersonIcon(imgSize = PicSize, firstName = "Timothy", txtSize = txtSize, game = game, self = self, navController = navController)

    }*/
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        items(chatRoomMembers){ member ->
            PersonIcon(
                member = member,
                clickable = true,
                game = false,
                self = false,
                navController = navController
            )
        }
    }
}

@Composable
fun PersonIcon(
    member: UserProfile,
    imgSize: Int = 30,
    txtSize: Int = 10,
    clickable: Boolean = true,
    game: Boolean,
    self: Boolean,
    navController: NavController
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(10.dp)
            .clickable {
                if (clickable){
                    navController.navigate("profileScreen/${game}/${self}/${member.userId}")
                }else {

                }
            },
        verticalArrangement = Arrangement.Center
    ){
        Image(
            painter = rememberAsyncImagePainter(member.imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(imgSize.dp)
                .clip(CircleShape)
        )
        Text(
            member.fname,
            fontSize = txtSize.sp
        )

    }
}

@Composable
fun UserProfileIcon(
    chatMember: UserProfile,
    imgSize: Int = 30,
    txtSize: Int = 10,
    game: Boolean,
    self: Boolean,
    navController: NavController
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(10.dp)
            .clickable { navController.navigate("profileScreen/${game}/${self}/${chatMember.userId}") }
    ){
        Image(
            painter = rememberAsyncImagePainter(chatMember.imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(imgSize.dp)
                .clip(CircleShape)
        )
        Text(
            chatMember.fname,
            fontSize = txtSize.sp
        )

    }
}

@Composable
fun ChatBubbleMock(game: Boolean, isFromMe: Boolean = false) {
    Row (
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
    ){
        if (!isFromMe){
            Image(
                painter = painterResource(id = R.drawable.cool_neon),
                contentDescription =null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column (
            modifier = Modifier
                .width(275.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(
                    if (isFromMe)
                        CRAppTheme.colorScheme.primary
                    else if (game)
                        CRAppTheme.colorScheme.onGameBackground
                    else
                        CRAppTheme.colorScheme.background

                )
                .border(
                    width = if (isFromMe) 2.dp else 0.dp,
                    color = if (isFromMe) CRAppTheme.colorScheme.highlight else Color.Transparent,
                    shape = RoundedCornerShape(15.dp)
                )
                .padding(6.dp)
        ){
            Column {
                if (!isFromMe){
                    Text("Tim C",
                        style = CRAppTheme.typography.titleMedium
                    )
                }
                Text(text = "I've been using your app nonstop, and I can't believe how intuitive and visually appealing it is—every detail seems so thoughtfully crafted, making each feature not just functional but also a joy to use, which is rare to find these days!",
                    style = CRAppTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp))
            }
        }


    }
}

@Composable
fun BottomInputBar() {

    var input by remember { mutableStateOf("")}

    Row (
        modifier = Modifier.fillMaxWidth()
    ){
        OutlinedTextField(
            value = input,
            onValueChange = { input = it},
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .background(Color.White)
        )
        IconButton(onClick = { /*TODO*/ },
            modifier = Modifier
                .background(Color.Blue)
                .height(50.dp)) {
            Icon(Icons.Default.Send, contentDescription = "" )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditInfoDialog(
    edit: String,
    userData: String,
    userProfile: UserProfile,
    game: Boolean,
    onDismiss: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val userId = userProfile.userId
    val (personalProfile, alternateProfile) = rememberProfileState(userId = userId, viewModel)
    var editFname by remember { mutableStateOf(userProfile.fname)}
    var editLname by remember { mutableStateOf(userProfile.lname)}
    var editAbout by remember{ mutableStateOf(userProfile.about)}
    var editGender by remember{ mutableStateOf("")}
    var editMonth by remember { mutableStateOf(userProfile.dob.month)}
    var editDay by remember { mutableStateOf(userProfile.dob.day)}
    var editYear by remember { mutableStateOf(userProfile.dob.year)}
    val rndMonth = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val rndDay = (1..29).map { it.toString() }
    val randomMonth = rndMonth.random()
    val randomDay = rndDay.random()
    var editAge by remember { mutableStateOf(userProfile.age)}
    var editLocation by remember { mutableStateOf(userProfile.location)}
    val context = LocalContext.current
    var ImageUri by remember { mutableStateOf<Uri?>(null) }
    var byteArray by remember { mutableStateOf<ByteArray?>(null)}

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        ImageUri = uri
        byteArray = uri?.uriToByteArray(context)
    }
    var editInfo by remember { mutableStateOf(userData)}

    var currentPassword by remember{ mutableStateOf("")}
    var newPassword by remember { mutableStateOf("")}

    var passwordVisible by remember { mutableStateOf(false)}

    var manSelect by remember { mutableStateOf(false)}
    var womanSelect by remember { mutableStateOf(false)}
    var otherSelect by remember { mutableStateOf(false)}
    when (userProfile.gender) {
        "Man" -> {
            manSelect = true
            womanSelect = false
            otherSelect = false
            editGender = "Man"
        }
        "Woman" -> {
            manSelect = false
            womanSelect = true
            otherSelect = false
            editGender = "Woman"
        }
        else -> {
            manSelect = false
            womanSelect = false
            otherSelect = true
            editGender = editInfo
        }
    }
    if (edit == "Gender"){
        if (userProfile.gender != "Man" && userProfile.gender != "Woman"){
            editInfo = userProfile.gender
        }else{
            editInfo = ""
        }
    }



    Dialog(onDismissRequest = {onDismiss()}) {
        Surface (
            shape = RoundedCornerShape(8.dp),
            color = CRAppTheme.colorScheme.background
        ){
          Column(
              verticalArrangement = Arrangement.Top,
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                  .padding(16.dp)
          )  {
              Text("Edit $edit", modifier = Modifier.padding(bottom = 16.dp))



              if (edit == "Gender"){
                  Row (
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceEvenly,
                      modifier = Modifier
                          .fillMaxWidth()
                          .height(50.dp)
                  ){
                      Text(
                          "Man",
                          style = CRAppTheme.typography.headingLarge,
                          textAlign = TextAlign.Center,
                          modifier = Modifier
                              .width(125.dp)
                              .clip(RoundedCornerShape(25.dp))
                              .border(2.dp, Color.Black, RoundedCornerShape(25.dp))
                              .background(if (manSelect) CRAppTheme.colorScheme.primary else Color.White)
                              .padding(8.dp)
                              .clickable {
                                  manSelect = true
                                  womanSelect = false
                                  otherSelect = false
                                  editGender = "Man"
                              }
                      )

                      Text(
                          "Woman",
                          style = CRAppTheme.typography.headingLarge,
                          textAlign = TextAlign.Center,
                          modifier = Modifier
                              .width(125.dp)
                              .clip(RoundedCornerShape(25.dp))
                              .border(2.dp, Color.Black, RoundedCornerShape(25.dp))
                              .background(if (womanSelect) CRAppTheme.colorScheme.primary else Color.White)
                              .padding(8.dp)
                              .clickable {
                                  manSelect = false
                                  womanSelect = true
                                  otherSelect = false
                                  editGender = "Woman"
                              }
                      )

                  }
                  Spacer(modifier = Modifier.height(20.dp))
                  Text(
                      "Other",
                      style = CRAppTheme.typography.headingLarge,
                      textAlign = TextAlign.Center,
                      modifier = Modifier
                          .width(260.dp)
                          .clip(RoundedCornerShape(25.dp))
                          .border(2.dp, Color.Black, RoundedCornerShape(25.dp))
                          .background(if (otherSelect) CRAppTheme.colorScheme.primary else Color.White)
                          .padding(8.dp)
                          .clickable {
                              manSelect = false
                              womanSelect = false
                              otherSelect = true
                              editGender = editInfo
                          }
                  )
              }

              Spacer(modifier = Modifier.height(20.dp))


              when (edit){

                  "Password" -> {
                      Text(
                          "Your password must be at least 6 characters and should include a combination of numbers, letters and special characters (!$@%)",
                          modifier = Modifier
                              .padding(bottom = 16.dp)
                      )
                      TextField(
                          value = currentPassword,
                          onValueChange = {currentPassword = it},
                          placeholder = {Text("Current Password")},
                          colors = TextFieldDefaults.textFieldColors(
                              containerColor = Color.White,
                              focusedIndicatorColor = Color.Transparent,
                              unfocusedIndicatorColor = Color.Transparent
                          ),
                          trailingIcon = {
                              if (edit == "Password") {
                                  IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                      Icon(
                                          imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Default.VisibilityOff,
                                          contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                      )
                                  }
                              }
                          },
                          visualTransformation = if (edit == "Password" && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                          modifier = Modifier
                              .fillMaxWidth()
                              .border(2.dp, Color.LightGray, RoundedCornerShape(8.dp))
                              .clip(RoundedCornerShape(8.dp))
                      )
                      Spacer(modifier = Modifier.height(10.dp))
                      TextField(
                          value = newPassword,
                          onValueChange = {newPassword = it},
                          placeholder = {Text("New password")},
                          colors = TextFieldDefaults.textFieldColors(
                              containerColor = Color.White,
                              focusedIndicatorColor = Color.Transparent,
                              unfocusedIndicatorColor = Color.Transparent
                          ),
                          trailingIcon = {
                              if (edit == "Password") {
                                  IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                      Icon(
                                          imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Default.VisibilityOff,
                                          contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                      )
                                  }
                              }
                          },
                          visualTransformation = if (edit == "Password" && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                          modifier = Modifier
                              .fillMaxWidth()
                              .border(2.dp, Color.LightGray, RoundedCornerShape(8.dp))
                              .clip(RoundedCornerShape(8.dp))
                      )
                      Spacer(modifier = Modifier.height(10.dp))
                      TextField(
                          value = editInfo,
                          onValueChange = {editInfo = it},
                          placeholder = {Text("Re-type new password")},
                          colors = TextFieldDefaults.textFieldColors(
                              containerColor = Color.White,
                              focusedIndicatorColor = Color.Transparent,
                              unfocusedIndicatorColor = Color.Transparent
                          ),
                          trailingIcon = {
                              if (edit == "Password") {
                                  IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                      Icon(
                                          imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Default.VisibilityOff,
                                          contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                      )
                                  }
                              }
                          },
                          visualTransformation = if (edit == "Password" && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                          modifier = Modifier
                              .fillMaxWidth()
                              .border(2.dp, Color.LightGray, RoundedCornerShape(8.dp))
                              .clip(RoundedCornerShape(8.dp))
                      )
                      Spacer(modifier = Modifier.height(20.dp))
                      Text("Forgot your password?",
                          color = Color.Blue,
                          modifier = Modifier
                              .align(Alignment.Start)
                              .clickable { }
                      )
                  }
                  "Name" -> {
                      TextField(
                          value = editFname,
                          onValueChange = {editFname = it},
                          placeholder = {Text("First Name")},
                          colors = TextFieldDefaults.textFieldColors(
                              containerColor = Color.White,
                              focusedIndicatorColor = Color.Transparent,
                              unfocusedIndicatorColor = Color.Transparent
                          ),
                          modifier = Modifier
                              .fillMaxWidth()
                              .border(
                                  2.dp,
                                  Color.LightGray,
                                  if (game){
                                      RoundedCornerShape(8.dp)
                                  }else {
                                      RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                  }
                              )
                              .clip(
                                  if (game){
                                      RoundedCornerShape(8.dp)
                                  }else {
                                      RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                  }
                              )
                      )
                      if (!game){
                          TextField(
                              value = editLname,
                              onValueChange = {editLname = it},
                              placeholder = {Text("Last Name")},
                              colors = TextFieldDefaults.textFieldColors(
                                  containerColor = Color.White,
                                  focusedIndicatorColor = Color.Transparent,
                                  unfocusedIndicatorColor = Color.Transparent
                              ),
                              modifier = Modifier
                                  .fillMaxWidth()
                                  .border(
                                      2.dp,
                                      Color.LightGray,
                                      RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                                  )
                                  .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                          )
                      }
                  }
                  "Email", "Gender" -> {
                      TextField(
                          value = editInfo,
                          onValueChange = {
                              editInfo = it
                              if (edit == "Gender"){
                                  manSelect = false
                                  womanSelect = false
                                  otherSelect = true
                              }
                                          },
                          placeholder = {
                              Text(
                                  when (edit) {
                                      "Email" -> "Email Address"
                                      "Gender" -> "Custom Description"
                                      else -> "Nothing"
                                  }

                              )},
                          colors = TextFieldDefaults.textFieldColors(
                              containerColor = Color.White,
                              focusedIndicatorColor = Color.Transparent,
                              unfocusedIndicatorColor = Color.Transparent
                          ),
                          trailingIcon = {
                              if (edit == "Password") {
                                  IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                      Icon(
                                          imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Default.VisibilityOff,
                                          contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                      )
                                  }
                              }
                          },
                          visualTransformation = if (edit == "Password" && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                          modifier = Modifier
                              .fillMaxWidth()
                              .border(2.dp, Color.LightGray, RoundedCornerShape(8.dp))
                              .clip(RoundedCornerShape(8.dp))
                              .clickable {
                                  manSelect = false
                                  womanSelect = false
                                  otherSelect = true
                                  editGender = editInfo
                              }
                      )
                  }
                  "About" -> {
                      TextField(
                          value = editAbout,
                          onValueChange = {editAbout = it},
                          colors = TextFieldDefaults.textFieldColors(
                              containerColor = Color.White,
                              focusedIndicatorColor = Color.Transparent,
                              unfocusedIndicatorColor = Color.Transparent
                          ),
                          modifier = Modifier
                              .fillMaxWidth()
                              .border(
                                  2.dp,
                                  Color.LightGray,
                                  RoundedCornerShape(8.dp)
                              )
                              .clip(RoundedCornerShape(8.dp))
                      )
                  }
                  "Date of Birth" -> {
                      DateDropDown(month = true, game = game) { selectedOption -> editMonth = selectedOption}
                      DateDropDown(day = true, game = game) { selectedOption -> editDay = selectedOption}
                      DateDropDown(year = true, game = game) { selectedOption -> editYear = selectedOption}
                  }
                  "Location" -> {
                      TextField(
                          value = editLocation,
                          onValueChange = {
                                          if (it.length <= 2 && it.all { char -> char.isLetter()}) {
                                              editLocation = it.uppercase()
                                          }
                          },
                          maxLines = 1,
                          colors = TextFieldDefaults.textFieldColors(
                              containerColor = Color.White,
                              focusedIndicatorColor = Color.Transparent,
                              unfocusedIndicatorColor = Color.Transparent
                          ),
                          modifier = Modifier
                              .width(60.dp)
                              .border(
                                  2.dp,
                                  Color.LightGray,
                                  RoundedCornerShape(8.dp)
                              )
                              .clip(RoundedCornerShape(8.dp))
                      )
                  }
                  "Age" -> {
                      DateDropDown(age = true, game = game){selected -> editAge = selected}
                  }
              }



              // Buttons
              Row (
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceEvenly,
                  modifier = Modifier
                      .fillMaxWidth()
                      .padding(top = 20.dp)
              ){
                  Button(
                      onClick = { onDismiss() },
                      modifier = Modifier
                          .width(100.dp)
                  ) {
                      Text("Cancel")
                  }
                  Button(
                      onClick = {
                          if (editInfo.isNotEmpty()){
                              editInfo = when {
                                  manSelect -> "Man"
                                  womanSelect -> "Woman"
                                  otherSelect -> editInfo
                                  else -> "Non Binary"
                              }
                          } else editInfo = "Not Specified"
                          val saveChangedProfile = when (edit) {
                              "Name" -> {
                                  userProfile.copy(fname = editFname, lname = editLname)
                              }
                              "About" -> {
                                  userProfile.copy(about = editAbout)
                              }
                              "Gender" -> {
                                  if (otherSelect == true){
                                      if (editInfo == ""){
                                          editInfo = "Non Binary"
                                          userProfile.copy(gender = editInfo)
                                      }else{
                                          userProfile.copy(gender = editInfo)
                                      }
                                  } else {
                                      userProfile.copy(gender = editGender)
                                  }
                              }
                              "Date of Birth" -> {
                                  val age = calculateBDtoAge(editYear).toString()
                                  userProfile.copy(
                                      age = age,
                                      dob = DateOfBirth(
                                          month = editMonth,
                                          day = editDay,
                                          year = editYear
                                      )
                                  )
                              }
                              "Location" -> {
                                  userProfile.copy(location = editLocation)
                              }
                              "Age" -> {
                                  if (editAge == "0"){
                                      editAge = "18"
                                  }
                                  val pickedYear = calculateAgeToDate(editAge.toInt())
                                  val Dob = DateOfBirth(month = randomMonth, day = randomDay, year = pickedYear)
                                  userProfile.copy(
                                      age = editAge,
                                      dob = Dob
                                  )
                              }
                              else -> { userProfile }
                          }
                          viewModel.saveUserProfile(userId = userId, userProfile = saveChangedProfile, game = game)
                          onDismiss()

                      },
                      modifier = Modifier
                          .width(100.dp)
                  ) {
                      Text("OK")
                  }
              }
          }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFirstNameDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val userId = userProfile.userId
    var editfName by remember { mutableStateOf(userProfile.fname) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedProfile = userProfile.copy(fname = editfName)
                    viewModel.saveUserProfile(userId, updatedProfile, false)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Edit First Name") },
        text = {
            Column {
                Text("Enter your new first name:")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = editfName,
                    onValueChange = { editfName = it },
                    placeholder = { Text("First Name") }
                )
            }
        }
    )
}


@Composable
fun SettingsInfoRow(
    game: Boolean = false,
    amount: Int = 1,
    icon: ImageVector? = null,
    contentDescription: String? = null,
    title: String,
    body: String = "",
    secondBody: String = "",
    arrow: Boolean = false,
    imagePic: Int? = null,
    extraChoice: Boolean = false,
    onClick: () -> Unit,
    Select: Boolean = false,
    Bio: Boolean = false,
    Edit: Boolean = false,
    editClick: Boolean = true,
    Image: Boolean = false,
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    var ImageUri by remember { mutableStateOf<Uri?>(null) }
    var byteArray by remember { mutableStateOf<ByteArray?>(null)}

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        ImageUri = uri
        byteArray = uri?.uriToByteArray(context)
    }
    when {
        Select -> {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
            ){
                //Divider()
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onClick() }
                ) {
                    if (icon != null){
                        Icon(
                            imageVector = icon,
                            contentDescription = contentDescription,
                            modifier = Modifier.
                            padding(end = 30.dp)
                        )
                    }
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .weight(1f)
                    )
                    if (arrow){
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
                if (extraChoice){
                    Row (
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    ){

                    }
                }
            }
        }
        Bio -> {
            Divider()
            Spacer(modifier = Modifier.padding(top = 10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { onClick() }
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        title,
                        style = CRAppTheme.typography.H1,
                        color = if(game) Color.White else Color.Black,
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                    )
                    Text(
                        "Edit",
                        fontSize = 18.sp,
                        color = Color.Blue,
                        modifier = Modifier
                            .clickable { onClick() }
                    )
                }
                Text(
                    body,
                    color = if(game) Color.White else Color.Black,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                )
            }
        }
        Edit -> {
            Divider()
            Spacer(modifier = Modifier.padding(top = 10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        title,
                        style = CRAppTheme.typography.H1,
                        color = if(game) Color.White else Color.Black,
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                    )
                    if (editClick) {
                        Text(
                            "Edit",
                            fontSize = 18.sp,
                            color = Color.Blue,
                            modifier = Modifier
                                .clickable { onClick() }
                        )
                    }
                }

                if (body.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .padding(start = 16.dp, end = 16.dp)
                    ) {
                        Text(
                            body,
                            fontSize = 16.sp,
                            color = if(game) Color.White else Color.Black,
                        )
                    }
                    if (amount == 2) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(start = 16.dp, end = 16.dp)
                        ) {
                            Text(
                                secondBody,
                                fontSize = 16.sp,
                                color = if(game) Color.White else Color.Black,
                            )
                        }
                    }
                }
            }

        }






        Image -> {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    title,
                    style = CRAppTheme.typography.H1,
                    color = if(game) Color.White else Color.Black,
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                )
                Text(
                    "Edit",
                    fontSize = 18.sp,
                    color = Color.Blue,
                    modifier = Modifier
                        .clickable { onClick() }
                )
            }



            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Image(
                    painter = rememberAsyncImagePainter(body),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                )
            }
        }










        else -> {
            Row(modifier = Modifier.fillMaxWidth()) { Text("Nothing Selected") }
        }
    }


}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateDropDown(
    month: Boolean = false,
    day: Boolean = false,
    year: Boolean = false,
    age: Boolean = false,
    game: Boolean,
    viewModel: ChatViewModel = viewModel(),
    onOptionSelected: (String) -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val (personalProfile, alternateProfile) = rememberProfileState(userId = userId, viewModel)

    // State for the dropdown menu
    val title = when {
        month -> "Month"
        day -> "Day"
        year -> "Year"
        age -> "Age"
        else -> "Select"
    }
    val default =
        if (personalProfile.fname.isNullOrBlank()){
            ""
        } else {
            when {
                month -> personalProfile.dob.month
                day -> personalProfile.dob.day
                year -> personalProfile.dob.year
                else -> "Unspecified"
            }
        }
    val ageTitle =
        if (alternateProfile.fname.isNullOrBlank()){
            "18"
        } else {
            alternateProfile.age
        }
    val defaultTitle = if (age) ageTitle else default
    val options = when {
        month -> listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        day -> (1..31).map { it.toString() }
        year -> {
            val currentYear = LocalDate.now().year - 18
            (currentYear downTo 1950).map { it.toString() }
        }
        age -> (18..60).map { it.toString() }
        else -> emptyList()
    }
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(defaultTitle) }

    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            title,
            style = CRAppTheme.typography.H0,
            color = if (game) Color.White else Color.Black,
            modifier = Modifier
                .padding(bottom = 10.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(130.dp)
                .height(50.dp)
                .padding(6.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, CRAppTheme.colorScheme.highlight, RoundedCornerShape(20.dp))
                .clickable { expanded = !expanded }
        ){
            Text(
                selectedOption,
                modifier = Modifier
                    .padding(start = if (age) 20.dp else 8.dp)
                    .weight(1f)
                    .clickable { expanded = !expanded }

            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier
                    .background(CRAppTheme.colorScheme.onBackground)
                    .fillMaxHeight()
                    .padding(end = 10.dp)
                    .clickable { expanded = !expanded }
            )
        }
    }

    // Dropdown Menu
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false } // Close dropdown when dismissed
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = {Text(option)},
                onClick = {
                    selectedOption = option
                    expanded = false
                    onOptionSelected(option)
                }
            )
        }
    }
}

@Composable
fun ExtraChoice(title1: String, onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { onClick() }
    ){
        Text(
            title1,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(end = 50.dp)
        )
        Icon(
            Icons.Default.ArrowForwardIos,
            contentDescription = null
        )
    }
}


@Composable
fun AnimatedDots(
    dotCount: Int = 4 // Number of dots to animate
) {
    var currentDots by remember { mutableStateOf("") }

    LaunchedEffect(dotCount) {
        while (true) {
            val nextCount = (currentDots.length + 1) % (dotCount + 1)
            currentDots = ".".repeat(nextCount)
            delay(500)
        }
    }

    Text(
        text = currentDots,
        style = CRAppTheme.typography.H3,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .width(70.dp)
    )
}


@Composable
fun FriendInfoRow(
    user: UserProfile,
    onUserSelected: (UserProfile) -> Unit,
    descriptionText: String = "Jocely Jackson",
    state: String = RowState.none.string,
    game: Boolean
) {

    var isSelected by remember { mutableStateOf(false)}

    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable {
                if (state == RowState.check.string) {
                    onUserSelected(user)
                    isSelected = !isSelected
                }
            }
    ){
        Image(
            painter = painterResource(id = R.drawable.person_sillouette),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)

        )
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .weight(1f)
        ){
            Text(
                "${user.fname} ${user.lname}",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(start = 10.dp)
            )
            if (state == RowState.follow.string){
                Text(
                    descriptionText,
                    modifier = Modifier
                        .padding(start = 10.dp)
                )
            }
        }
        when (state){
            RowState.follow.string -> {
                Text(
                    "Follow back",
                    modifier = Modifier
                        .background(CRAppTheme.colorScheme.primary)
                        .padding(6.dp)
                        .clickable { }
                )
                Text(
                    "...",
                    style = CRAppTheme.typography.H0,
                    modifier = Modifier
                        .padding(start = 10.dp)
                )
            }
            RowState.check.string -> {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { isSelected = it},
                    colors = CheckboxDefaults.colors(
                        checkedColor = if (game) darkPurple else Color.Blue,
                        uncheckedColor = if (game) Color.White else Color.Black
                    )
                )
            }
        }

    }
}

@Composable
fun AllMembersRow(chatRoomMembers: List<UserProfile>, game: Boolean, self: Boolean, navController: NavController) {
    LazyRow (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        items(chatRoomMembers) { member ->
            UserProfileIcon(
                chatMember = member,
                game = game,
                self = self,
                imgSize = 50,
                txtSize = 20,
                navController = navController
            )
        }

    }
}
@Composable
fun ChatLazyColumn(
    viewModel: ChatViewModel
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""
    val (personalProfile, alternateProfile) = rememberProfileState(userId = userId, viewModel)
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()

    val ScrollToBottom = remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index != messages.size -1
        }
    }
    if (ScrollToBottom.value){
        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()){
                listState.animateScrollToItem(messages.size -1)
            }

        }
    }

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxSize()
    ) {
        itemsIndexed(messages) { index, message ->
            val previousMessage = if(index >0) messages[index - 1] else null
            ChatBubble(
                image = message.image,
                message = message,
                isFromMe = message.senderId == currentUser?.uid,
                previousMessage = previousMessage
            )
        }
        item {
            Row (
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Text(
                    "Sending as",
                    modifier = Modifier.padding(end = 10.dp)
                )
                Image(
                    painter = rememberAsyncImagePainter(personalProfile.imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                )
                Text(
                    personalProfile.fname,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }

}
@Composable
fun ChatBubble(
    message: ChatMessage,
    image: String,
    isFromMe: Boolean,
    previousMessage: ChatMessage?
) {
    val borderRad = 30.dp
    val showProfileImage = previousMessage?.senderId != message.senderId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        // ---------------- if left than members picture
        if (!isFromMe && showProfileImage) {
            Image(
                painter = rememberAsyncImagePainter(image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
        } else{
            Image(
                painter = rememberAsyncImagePainter(model = ""),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        Column (
            horizontalAlignment = if(isFromMe)Alignment.End else Alignment.Start
        ){
            //----------------- name and time
            Row(
                horizontalArrangement = if (!isFromMe) Arrangement.Start else Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (!isFromMe && showProfileImage) {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                if (showProfileImage){
                    Text(formattedDayTimestamp(message.timestamp), fontWeight = FontWeight.Light)
                }

            }
            //--------------------------- Text Message
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = if (!isFromMe) 0.dp else borderRad,
                            topEnd = if (!isFromMe) borderRad else 0.dp,
                            bottomStart = borderRad,
                            bottomEnd = borderRad
                        )
                    )
                    .background(if (!isFromMe) CRAppTheme.colorScheme.primary else CRAppTheme.colorScheme.onBackground)
                    .padding(10.dp),

                ) {

                Text(
                    text = message.message,
                    color = Color.Black,
                    lineHeight = 23.sp,
                    letterSpacing = 1.sp
                )

            }
        }
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInput(viewModel: ChatViewModel, roomId: String) {
    var input by remember { mutableStateOf("") }

    fun send(){
        if (input.isNotBlank()) {
            viewModel.sendMessage(roomId = roomId, message = input, game = false)
            input = ""
        }
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
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                send()
            }),
        )
        IconButton(onClick = {
            send()
        }) {
            Icon(Icons.Default.Send, contentDescription = "")
        }

    }

}

@Preview(name = "Light Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TestLightMode() {

    CRAppTheme () {
        Surface {
            Column (modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)){

            }
        }
    }
}
