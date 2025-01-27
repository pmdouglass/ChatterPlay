@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.chatterplay.seperate_composables

import android.os.Build
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ImageAspectRatio
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.R
import com.example.chatterplay.data_class.ChatMessage
import com.example.chatterplay.data_class.ChatRoom
import com.example.chatterplay.data_class.DateOfBirth
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.data_class.formattedDayTimestamp
import com.example.chatterplay.repository.fetchUserProfile
import com.example.chatterplay.screens.login.calculateAgeToDate
import com.example.chatterplay.screens.login.calculateBDtoAge
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.ui.theme.darkPurple
import com.example.chatterplay.view_model.ChatRiseViewModel
import com.example.chatterplay.view_model.ChatViewModel
import com.example.chatterplay.view_model.RoomCreationViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate


enum class RowState (val string: String){
    None("nothing"),
    Follow("follow"),
    Check("check")
}

@Composable
fun rememberProfileState(userId: String, viewModel: ChatViewModel = viewModel()): Pair<UserProfile, UserProfile> {
    val personalState by viewModel.userProfile.collectAsState()
    val alternateState by viewModel.alternateUserProfile.collectAsState()

    val personalProfile = personalState ?: UserProfile()
    val alternateProfile = alternateState ?: UserProfile()

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile(userId = userId)
    }
    return  Pair(personalProfile, alternateProfile)

}
@Composable
fun rememberCRProfile(crRoomId: String, viewModel: ChatRiseViewModel = viewModel()): UserProfile{
    val profileState by viewModel.userProfile.collectAsState()
    val profile = profileState ?: UserProfile()
    LaunchedEffect(Unit){
        viewModel.getUserProfile(crRoomId = crRoomId)
    }
    return profile
}
@Composable
fun ChatRiseThumbnail(
    viewModel: ChatViewModel = viewModel(),
    crViewModel: ChatRiseViewModel = viewModel(),
    roomCreate: RoomCreationViewModel = viewModel(),
    navController: NavController
) {
    val email by remember { mutableStateOf("email")}
    val password by remember { mutableStateOf("password")}
    val currentUser = FirebaseAuth.getInstance().currentUser
    var selfSelect by remember { mutableStateOf(true)}
    var altSelect by remember { mutableStateOf(false)}
    val (personalProfile, alternateProfile) = rememberProfileState(userId = currentUser?.uid ?: "", viewModel)
    val crRoomId by roomCreate.crRoomId.collectAsState()
    val allChatRoomMembers by viewModel.allChatRoomMembers.collectAsState()


    val width = 100

    val userStatus by roomCreate.userStatus.collectAsState()
    val roomReady by roomCreate.roomReady.collectAsState()


    val hasAlternateProfile = alternateProfile.fname.isNotBlank()

    val gameInfo by crViewModel.gameInfo.collectAsState() // gets gameInfo 'Title' from UserProfile
    val usersGameAlertStatus by crViewModel.usersAlertStatus.collectAsState()
    val isDoneAnswering by crViewModel.isDoneAnswering // sees if current user done with answers
    val thereIsAnAlertMessage by remember {
        derivedStateOf {
            gameInfo != null && usersGameAlertStatus == false}
    }


    LaunchedEffect(crRoomId, gameInfo){
        crRoomId?.let { roomId ->
            if (roomId.isNotEmpty()){
                if (gameInfo == null){
                    crViewModel.fetchGameInfo(roomId) // initialize gameInfo
                }else {
                    gameInfo?.let { game ->
                        crViewModel.fetchUsersGameAlert(roomId, currentUser?.uid ?: "", game.title) // initialize hadAlert
                        crViewModel.checkUserForAllCompleteAnswers(roomId, game.title) // initialize isDoneAnswering
                    }
                }
            }
        }
    }


    val isReadyToDisplay by remember {
        derivedStateOf {
            crRoomId != null &&
                    crRoomId!!.isNotEmpty() &&
                    gameInfo != null &&
                    isDoneAnswering != null &&
                    userStatus != null
        }
    }
    Log.d("Reusable", "crRoomId: $crRoomId")
    Log.d("Reusable", "gameInfo: $gameInfo")
    Log.d("Reusable", "isDoneAnswering: $isDoneAnswering")
    Log.d("Reusable", "isReadyToDisplay: $isReadyToDisplay")
    Log.d("Reusable", "userStatus: $userStatus")


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
            /*
            if (userStatus != null){
            }
             */
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Text(
                    text = "ChatRise",
                    style = CRAppTheme.typography.headingMedium,
                    modifier = Modifier
                        .then(if (userStatus == "NotPending") Modifier.padding(end = 20.dp) else Modifier.weight(1f))
                )
                when  {
                    userStatus == "NotPending" -> {
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
                    userStatus == "Pending" -> {}
                    roomReady -> {

                        LaunchedEffect(Unit){
                            if (crRoomId != "0"){
                                crRoomId?.let {room ->
                                    viewModel.fetchChatRoomMembers(crRoomId = room, roomId = room, game = true, mainChat = true)
                                }
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "${allChatRoomMembers.size}",
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
            if (isReadyToDisplay){
                when {
                    userStatus == "NotPending" -> {
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
                                    viewModel.saveUserProfile(userId = currentUser?.uid ?: "", userProfile = updatedProfile, game = false)
                                    viewModel.saveUserProfile(userId = currentUser?.uid ?: "", userProfile = altUpdatedProfile, game = true)
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
                    userStatus == "Pending" -> {
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
                        if (crRoomId != "0"){
                            crRoomId?.let { crRoomId ->
                                Column(
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            navController.navigate("mainScreen/$crRoomId")
                                        }
                                ) {

                                    if (thereIsAnAlertMessage){
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxSize()
                                        ){
                                            Text(
                                                "ALERT!",
                                                style = CRAppTheme.typography.H6,
                                                color = Color.Red
                                            )
                                        }
                                    }else {
                                        if (isDoneAnswering == true){
                                            ChatMainPreviewLazyColumn(
                                                crRoomId = crRoomId,
                                                roomId = crRoomId,
                                            )
                                        }else {
                                            if (gameInfo != null){
                                                gameInfo?.let { game ->
                                                    Row(
                                                        horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                    ){
                                                        Text(
                                                            "You Must Complete\n\n${game.title}",
                                                            style = CRAppTheme.typography.H3,
                                                            color = Color.Black,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }






                                }
                            }
                        }
                    }
                    else -> {
                        Text("Nothing Selected")
                    }


                }
            }else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    CircularProgressIndicator(color = Color.Black)
                }
            }

        }

    }
}
@Composable
fun ThumbnailChatList(
    image: String,
    message: ChatMessage,
    isFromMe: Boolean,
){
    Row (
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ){
        if (!isFromMe){
            Image(
                painter = rememberAsyncImagePainter(image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = message.message,
            style = CRAppTheme.typography.bodyLarge
        )
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
fun PrivateGroupPicThumbnail(game: Boolean, memberCount: Int, imageUrls: List<String>) {

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
fun RoomSelectionView(game: Boolean, room: ChatRoom, membersCount: Int, replyCount: Int, onClick: () -> Unit) {

    val lastReplyImage = room.lastProfile
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val otherUserIds = room.members.filter {it != currentUserId}
    /*val otherUserProfile by produceState<UserProfile?>(initialValue = null, key1 = otherUserIds){
        value = otherUserIds.let { fetchUserProfile(it) }
    }*/
    val otherUserProfiles by produceState(initialValue = emptyList(), key1 = otherUserIds) {
        value = otherUserIds.mapNotNull { fetchUserProfile(it) }
    }
    val imageUrls = otherUserProfiles.map { it.imageUrl }
    //val theirImage = otherUserProfile?.imageUrl
    val theirName = if (game) {
        otherUserProfiles.firstOrNull()?.fname ?: ""
    }else {
        otherUserProfiles.joinToString (", ") { "${it.fname} ${it.lname}"}
    }
    //val theirName = otherUserProfiles.joinToString(", ") { if(game) "$it.fname" else "${it.fname} ${it.lname}" }



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
        HorizontalDivider(modifier = Modifier.padding(top = 10.dp, start = 15.dp, end = 15.dp))
    }
}
@Composable
fun MainTopAppBar(title: String, action: Boolean, actionIcon: ImageVector, onAction: () -> Unit, navController: NavController) {

    val pad = 15
    var expand by remember { mutableStateOf(false) }
    val tabs = listOf(
        "null" to Icons.Default.Home,
        "null" to Icons.Default.Person,
        "null" to Icons.Default.Menu,
        "null" to Icons.Default.ImageAspectRatio
    )
    var selectedTabindex by remember { mutableIntStateOf(1) }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!expand) Modifier.height(56.dp) else Modifier)
            .clickable { expand = !expand }
            .background(CRAppTheme.colorScheme.gameBackground)
    ) {
        // topbar
        // inside
        // navigationrow

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {

            //               top bar
            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
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





        //           Inside Content
        Image(
            painter = painterResource(id = R.drawable.anonymous),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable {

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

            }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            IconButton(onClick = {

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
                    imageVector = Icons.AutoMirrored.Default.List,
                    contentDescription = null,
                    tint = Color.White
                )
            }

        }
        Spacer(modifier = Modifier.height(pad.dp + 10.dp))
        NavigationRow(
            tabs = tabs,
            selectedTabIndex = selectedTabindex,
            onTabSelected = {index ->
                selectedTabindex = index
            }
        )
    }
}

@Composable
fun ChatRiseTopBar(
    profile: UserProfile,
    onClick: () -> Unit,
    onAction: () -> Unit,
    showTopBarInfo: Boolean,
    navController: NavController
){

    Column (
        modifier = Modifier
            .background(CRAppTheme.colorScheme.gameBackground)
    ) {
        TopBar(
            onClick = {onClick()},
            onAction = {onAction()},
            navController = navController)

        if (showTopBarInfo){
            TopBarInformation(profile = profile)
        }
    }
}

@Composable
fun TopBar(
    onClick: () -> Unit,
    onAction: () -> Unit,
    navController: NavController
){

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
    ){
        IconButton(onClick = {
            navController.popBackStack()
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(35.dp)
            )
        }

        Text(
            text = "ChatRise",
            style = CRAppTheme.typography.headingLarge,
            color = Color.White,
            modifier = Modifier
                .clickable { onClick() }
        )

        IconButton(onClick = {onAction()}){
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(35.dp)
            )
        }
    }
}
@Composable
fun TopBarInformation(
    profile: UserProfile
){
    val pad = 15

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
    ){
        Image(
            painter = rememberAsyncImagePainter(profile.imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable {

                }
        )

        Text(
            profile.fname,
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
    }

}
@Composable
fun NavigationRow(
    tabs: List<Pair<String, ImageVector>>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    disabledTabIndices: List<Int> = emptyList()
) {
    val selectedColor = Color.White
    val unselectedColor = Color.Gray

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = CRAppTheme.colorScheme.gameBackground,
        contentColor = selectedColor,
        indicator = { tabPositions ->
            SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = selectedColor
            )
        }
    ) {
        tabs.forEachIndexed { index, tab ->
            val (title, icon) = tab
            val isDisabled = index in disabledTabIndices

            Tab(
                selected = selectedTabIndex == index,
                onClick = {
                    if (!isDisabled){
                        onTabSelected(index)
                    }
                          },
                enabled = !isDisabled,
                /*icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (selectedTabIndex == index) selectedColor else unselectedColor
                    )
                }*/
            ){
                Box{
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (selectedTabIndex == index) selectedColor else unselectedColor,
                        modifier = Modifier
                            .size(28.dp)
                    )
                    if (isDisabled){
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(28.dp)
                        )
                    }
                }
            }
        }
    }
}








@Composable
fun RightSideModalDrawer(
    drawerContent: @Composable () -> Unit,
    reset: () -> Unit,
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(
        DrawerValue.Closed
    ),
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val drawerWidth = 350.dp // Adjust the width as needed
    val drawerWidthPx = with(LocalDensity.current) { drawerWidth.toPx() }
    val openOffsetX = 0f

    val offsetX by animateFloatAsState(
        targetValue = if (drawerState.isOpen) openOffsetX else drawerWidthPx, label = ""
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
                            reset()
                        }
                    }
            )
        }

        Box(
            Modifier
                .offset { IntOffset(offsetX.toInt(), 0) }
                .fillMaxHeight()
                .width(drawerWidth)
                .background(CRAppTheme.colorScheme.onGameBackground.copy(alpha = .9f))
                .border(
                    2.dp,
                    if (drawerState.isOpen) CRAppTheme.colorScheme.highlight else Color.Transparent
                )
                //.padding(8.dp)
        ) {
            drawerContent()
        }
    }
}
@Composable
fun PrivateDrawerRoomList(
    crRoomId: String,
    onInvite: () -> Unit,
    viewModel: ChatViewModel = viewModel(),
    navController: NavController
) {

    var searchChats by remember{ mutableStateOf("") }

    LaunchedEffect(crRoomId){
        viewModel.fetchAllRiserRooms(crRoomId = crRoomId)
    }


    val chatRooms by viewModel.allRiserRooms.collectAsState()
    val allRooms = chatRooms.sortedByDescending { it.lastMessageTimestamp}



    Column(
        modifier = Modifier
            .fillMaxHeight()
    ){
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
        ){
            IconButton(onClick = { onInvite()}) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(35.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(50.dp))

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
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = CRAppTheme.colorScheme.onGameBackground,
                focusedContainerColor = CRAppTheme.colorScheme.onGameBackground,
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
        HorizontalDivider(modifier = Modifier.padding(bottom = 20.dp))

        LazyColumn (
            modifier = Modifier
                .fillMaxSize()
        ){
            items(allRooms){ room ->
                RoomSelectionView(
                    game = true,
                    room = room,
                    membersCount = room.members.size,
                    replyCount = /*unreadMessageCount[room.roomId] ?: 0,*/ 50,
                    onClick = {
                        navController.navigate("chatScreen/${crRoomId}/${room.roomId}/true/false")
                    }
                )
            }
        }

    }
}







@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditInfoDialog(edit: String, userData: String, userProfile: UserProfile, game: Boolean, onDismiss: () -> Unit, viewModel: ChatViewModel = viewModel()) {
    val userId = userProfile.userId
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
        editInfo = if (userProfile.gender != "Man" && userProfile.gender != "Woman"){
            userProfile.gender
        }else{
            ""
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
                          colors = TextFieldDefaults.colors(
                              unfocusedContainerColor = Color.White,
                              focusedContainerColor = Color.White,
                              focusedIndicatorColor = Color.Transparent,
                              unfocusedIndicatorColor = Color.Transparent
                          ),
                          trailingIcon = {
                              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                  Icon(
                                      imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Default.VisibilityOff,
                                      contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                  )
                              }
                          },
                          visualTransformation = if (!passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
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
                          colors = TextFieldDefaults.colors(
                              unfocusedContainerColor = Color.White,
                              focusedContainerColor = Color.White,
                              focusedIndicatorColor = Color.Transparent,
                              unfocusedIndicatorColor = Color.Transparent
                          ),
                          trailingIcon = {
                              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                  Icon(
                                      imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Default.VisibilityOff,
                                      contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                  )
                              }
                          },
                          visualTransformation = if (!passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
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
                          colors = TextFieldDefaults.colors(
                              unfocusedContainerColor = Color.White,
                              focusedContainerColor = Color.White,
                              focusedIndicatorColor = Color.Transparent,
                              unfocusedIndicatorColor = Color.Transparent
                          ),
                          trailingIcon = {
                              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                  Icon(
                                      imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Default.VisibilityOff,
                                      contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                  )
                              }
                          },
                          visualTransformation = if (!passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
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
                          colors = TextFieldDefaults.colors(
                              unfocusedContainerColor = Color.White,
                              focusedContainerColor = Color.White,
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
                              colors = TextFieldDefaults.colors(
                                  unfocusedContainerColor = Color.White,
                                  focusedContainerColor = Color.White,
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
                          colors = TextFieldDefaults.colors(
                              unfocusedContainerColor = Color.White,
                              focusedContainerColor = Color.White,
                              focusedIndicatorColor = Color.Transparent,
                              unfocusedIndicatorColor = Color.Transparent
                          ),
                          trailingIcon = {
                              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                  Icon(
                                      imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Default.VisibilityOff,
                                      contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                  )
                              }
                          },
                          visualTransformation = if (!passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
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
                          colors = TextFieldDefaults.colors(
                              unfocusedContainerColor = Color.White,
                              focusedContainerColor = Color.White,
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
                          colors = TextFieldDefaults.colors(
                              unfocusedContainerColor = Color.White,
                              focusedContainerColor = Color.White,
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
                          editInfo = if (editInfo.isNotEmpty()){
                              when {
                                  manSelect -> "Man"
                                  womanSelect -> "Woman"
                                  otherSelect -> editInfo
                                  else -> "Non Binary"
                              }
                          } else "Not Specified"
                          val saveChangedProfile = when (edit) {
                              "Name" -> {
                                  userProfile.copy(fname = editFname, lname = editLname)
                              }
                              "About" -> {
                                  userProfile.copy(about = editAbout)
                              }
                              "Gender" -> {
                                  if (otherSelect){
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
                                  val dob = DateOfBirth(month = randomMonth, day = randomDay, year = pickedYear)
                                  userProfile.copy(
                                      age = editAge,
                                      dob = dob
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
    extraChoice: Boolean = false,
    onClick: () -> Unit,
    select: Boolean = false,
    bio: Boolean = false,
    edit: Boolean = false,
    editClick: Boolean = true,
    image: Boolean = false
) {
    when {
        select -> {
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
                            Icons.AutoMirrored.Filled.ArrowForward,
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
        bio -> {
            HorizontalDivider()
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
        edit -> {
            HorizontalDivider()
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






        image -> {
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
fun DateDropDown(month: Boolean = false, day: Boolean = false, year: Boolean = false, age: Boolean = false, game: Boolean, viewModel: ChatViewModel = viewModel(), onOptionSelected: (String) -> Unit) {
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
        if (personalProfile.fname.isBlank()){
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
        if (alternateProfile.fname.isBlank()){
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
                .border(1.dp, if (game) Color.LightGray else Color.Black, RoundedCornerShape(20.dp))
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
fun AnimatedDots(dotCount: Int = 4) {
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
fun FriendInfoRow(user: UserProfile, onUserSelected: (UserProfile) -> Unit, descriptionText: String = "Jocely Jackson", state: String = RowState.None.string, game: Boolean) {

    var isSelected by remember { mutableStateOf(false)}

    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable {
                if (state == RowState.Check.string) {
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
            if (state == RowState.Follow.string){
                Text(
                    descriptionText,
                    modifier = Modifier
                        .padding(start = 10.dp)
                )
            }
        }
        when (state){
            RowState.Follow.string -> {
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
            RowState.Check.string -> {
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
