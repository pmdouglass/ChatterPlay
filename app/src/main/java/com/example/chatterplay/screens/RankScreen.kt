package com.example.chatterplay.screens

import android.content.Context
import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.MainActivity
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.analytics.ScreenPresenceLogger
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.seperate_composables.rememberCRProfile
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatRiseViewModel
import com.example.chatterplay.view_model.ChatRiseViewModelFactory
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay


@Composable
fun RankingScreen(
    crRoomId: String,
    allChatRoomMembers: List<UserProfile>,
    viewModel: ChatViewModel = viewModel()
){

    // Create SharedPreferences
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Initialize ChatRiseViewModel with the factory
    val crViewModel: ChatRiseViewModel = viewModel(
        factory = ChatRiseViewModelFactory(sharedPreferences, viewModel)
    )

    val currentMode by crViewModel.rankingStatus.collectAsState()
    val profile = rememberCRProfile(crRoomId)
    val currentUser = FirebaseAuth.getInstance().currentUser
    val allRisers by viewModel.allRisers.collectAsState()
    val chatRoomMembers = allChatRoomMembers.filter { it.userId != currentUser?.uid }
    val RisersAll = allRisers
        .toMutableList()
        .apply {
            add(profile)
        }

    val selectedAction = remember { mutableStateOf<Int?>(null)}
    val isSwapWithRightMode = remember { mutableStateOf(false) }
    val swapWithRightIndex = remember { mutableStateOf<Int?>(null) }
    val isSwapWithLeftMode = remember { mutableStateOf(false) }
    val swapWithLeftIndex = remember { mutableStateOf<Int?>(null) }
    val isRightSideComplete = remember { mutableStateOf(false)}
    val isRightSideClickable = remember { mutableStateOf(true)}
    val selectedImage = remember { mutableStateOf<UserProfile?>(null)}


    LaunchedEffect(crRoomId, currentMode){
        viewModel.fetchAllRisers(crRoomId)
        crViewModel.checkUserRankingStatus(crRoomId = crRoomId, userId = currentUser?.uid ?: "")
        crViewModel.fetchRankingsList(crRoomId) // rankingList
        crViewModel.monitorUsersDoneRankingStatus(crRoomId, RisersAll,context)
    }
    val rightRiser = remember { mutableStateListOf<UserProfile?>().apply { repeat(allRisers.size) {add(null)} }}
    val leftRiser = remember {
        mutableStateListOf<UserProfile?>().apply {
            addAll(allRisers)
        }
    }

    isRightSideComplete.value = rightRiser.all { it != null }


    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    LaunchedEffect(Unit){
        // Log the event in Firebase Analytics
        val params = Bundle().apply {
            putString("screen_name", "RankingScreen")
            putString("user_id", userId)
            putString("timestamp", System.currentTimeMillis().toString())
        }
        AnalyticsManager.getInstance(context).logEvent("screen_view", params)
    }
    ScreenPresenceLogger(screenName = "RankingScreen", userId = userId)
    (context as? MainActivity)?.setCurrentScreen(("RankingScreen"))


    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(CRAppTheme.colorScheme.onGameBackground)
    ) {
        Text(
            when (currentMode){
                "View" -> {
                    "Current Ranks"
                }
                "Ranking" -> {
                    "Vote your players"
                }
                "Done" -> {
                    "Waiting on Others"
                }
                else -> {
                    "Other"
                }
            },
            style = CRAppTheme.typography.T4,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(20.dp))
        HorizontalDivider()

        Box(
            modifier = Modifier
                .fillMaxSize()
        ){
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when (currentMode) {
                    "View" -> {
                        CurrentRanks(crRoomId = crRoomId)
                    }

                    "Ranking" -> {
                        LeftList(
                            currentMode = "Ranking",
                            rightRiser = rightRiser,
                            selectedImage = selectedImage,
                            leftRiser = leftRiser,
                            isSwapWithLeftMode = isSwapWithLeftMode,
                            swapWithLeftIndex = swapWithLeftIndex,
                            isSwapWithRightMode = isSwapWithRightMode
                        )

                        rightList(
                            rightRiser = rightRiser,
                            selectedAction = selectedAction,
                            selectedImage = selectedImage,
                            leftRiser = leftRiser,
                            isSwapWithRightMode = isSwapWithRightMode,
                            swapWithRightIndex = swapWithRightIndex,
                            isSwapWithLeftMode = isSwapWithLeftMode,
                            swapWithLeftIndex = swapWithLeftIndex,
                            isRightSideClickable = isRightSideClickable
                        )
                    }

                    "Done" -> {
                        UsersSelectedRank(crRoomId)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (!isSwapWithLeftMode.value && !isSwapWithRightMode.value){
                        Modifier.background(Color.Transparent)
                    } else {
                        Modifier.background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    if (isSwapWithLeftMode.value) Color.Transparent else Color.Gray.copy(alpha = 0.8f),
                                    if (isSwapWithRightMode.value) Color.Transparent else Color.Gray.copy(alpha = 0.8f)
                                ),
                                startX = 450f,
                                endX = 850f
                            )
                        )
                    }
                    )
            ){
                if (isSwapWithLeftMode.value || isSwapWithRightMode.value){
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = (-200).dp)
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color.Black, CircleShape)
                            .clickable {
                                isSwapWithLeftMode.value = false
                                isSwapWithRightMode.value = false
                                swapWithLeftIndex.value = null
                                swapWithRightIndex.value = null
                                selectedImage.value = null
                            }
                    ){
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(70.dp)
                        )
                    }
                }




                if (isRightSideComplete.value && isRightSideClickable.value) {
                    Button(
                        onClick = {
                            finalizeRanking(
                                crRoomId = crRoomId,
                                rightRiser = rightRiser,
                                userId = currentUser?.uid ?: "",
                                crViewModel = crViewModel
                            )
                            isRightSideComplete.value = false
                            isRightSideClickable.value = false
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = (-75).dp)
                    ){
                        Text("Finalize",
                            style = CRAppTheme.typography.H3)
                    }
                }


                if (selectedImage.value != null){
                    // display Selected Image until choice is made
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(125.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Black, CircleShape)
                    ){
                        Image(
                            painter = rememberAsyncImagePainter(selectedImage.value?.imageUrl),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxSize()
                        )
                    }
                }
            }

        }
    }
}
@Composable
fun CurrentRanks(
    crRoomId: String,
    viewModel: ChatViewModel = viewModel()
){

    // Create SharedPreferences
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Initialize ChatRiseViewModel with the factory
    val crViewModel: ChatRiseViewModel = viewModel(
        factory = ChatRiseViewModelFactory(sharedPreferences, viewModel)
    )
    val userAlertType by crViewModel.usersAlertType.collectAsState()
    val systemAlertType by crViewModel.systemAlertType.collectAsState()
    val allRisers by viewModel.allRisers.collectAsState()
    val profile = rememberCRProfile(crRoomId = crRoomId)
    val AllRisers = allRisers
        .toMutableList()
        .apply {
            add(profile)
        }
    val rankingList by crViewModel.userRankList.collectAsState()
    val hasSeenRankResult by crViewModel.hasSeenRankResult.collectAsState()
    val displayRanks = if (rankingList.isEmpty()) AllRisers.map { it to 0 } else rankingList
    val sortedRanks = displayRanks
    val revealOrder = sortedRanks
    var revealindex by remember { mutableStateOf(0)}


    LaunchedEffect(crRoomId){
        crViewModel.fetchRankingsList(crRoomId) // rankingList
        crViewModel.checkSeenRankResult(crRoomId)
        crViewModel.fetchSystemAlertType(crRoomId) // systemAlertType
        viewModel.fetchAllRisers(crRoomId)
        if (systemAlertType != null){
            systemAlertType?.let {alertType ->
                crViewModel.monitorForAllSeenResult(crRoomId,AllRisers, alertType = alertType ,context)
            }
        }
    }
    LaunchedEffect(revealOrder){
        for (i in 1..revealOrder.size){
            delay(3000L)
            revealindex = i
        }
    }
    LaunchedEffect(revealindex){
        if (revealindex == revealOrder.size && !hasSeenRankResult){
            crViewModel.updateSeenRankResult(crRoomId, true)
            crViewModel.checkSeenRankResult(crRoomId)
        }
    }

        Column(
            //verticalArrangement = Arrangement.SpaceEvenly,
            //horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxSize()
            ){
                LazyColumn(
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    reverseLayout = if (!hasSeenRankResult) true else false,
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    if (hasSeenRankResult){
                        itemsIndexed(displayRanks){ index, (member) ->
                            Row (
                                modifier = Modifier
                                    .then(
                                        if (rankingList.isEmpty()){
                                            Modifier
                                        }else {
                                            if (index == 0 || index == 1){
                                                Modifier.border(2.dp, CRAppTheme.colorScheme.highlight)
                                            } else {
                                                Modifier
                                            }
                                        }
                                    )
                                    .padding(2.dp)
                            ){
                                Text(
                                    if (rankingList.isEmpty()) "" else getOrdinal(index + 1),
                                    color = Color.White)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ){
                                    Image(
                                        painter = rememberAsyncImagePainter(member.imageUrl),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                    )
                                    Text(member.fname, color = Color.White)
                                }
                            }
                        }

                    }else {
                        itemsIndexed(revealOrder.reversed()) { index, (member) ->
                            var isVisible by remember { mutableStateOf(false) }

                            LaunchedEffect(index){
                                delay(index * 5000L)
                                isVisible = true
                            }
                            AnimatedVisibility(
                                visible = isVisible,
                                enter = fadeIn(animationSpec = tween(durationMillis = 5000)) + slideInVertically()
                            ){
                                Row(
                                    modifier = Modifier
                                        .then(
                                            if (rankingList.isNotEmpty() && (index == sortedRanks.size -1 || index == sortedRanks.size - 2)){
                                                Modifier.border(2.dp, CRAppTheme.colorScheme.highlight)
                                            }else Modifier
                                        )
                                        .padding(2.dp)
                                ){
                                    Text(
                                        if (rankingList.isEmpty()) "" else getOrdinal(sortedRanks.size - index),
                                        color = Color.White
                                    )
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(member.imageUrl),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                        )
                                        Text(member.fname, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

}

@Composable
fun LeftList(
    currentMode: String,
    leftRiser: MutableList<UserProfile?>,
    rightRiser: MutableList<UserProfile?>,
    selectedImage: MutableState<UserProfile?>,
    isSwapWithLeftMode: MutableState<Boolean>,
    swapWithLeftIndex: MutableState<Int?>,
    isSwapWithRightMode: MutableState<Boolean>
) {

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
    ) {
        Text(
            when (currentMode){
                "View" -> {
                    "Current"
                }
                "Ranking" -> {
                    "Players"
                }
                "Done" -> {
                    "Waiting..."
                }
                else -> {
                    "Other"
                }
            },
            style = CRAppTheme.typography.H2,
            color = Color.White
        )
        leftRiser.forEachIndexed { index, member ->
            Row (
                modifier = Modifier
                    .let { baseModifier ->
                        if (currentMode == "Ranking") {
                            baseModifier.clickable(
                                enabled = !isSwapWithRightMode.value
                            ) {
                                if (isSwapWithLeftMode.value && swapWithLeftIndex.value != null) {
                                    val rightIndex = swapWithLeftIndex.value!!
                                    val temp = leftRiser[index]
                                    leftRiser[index] = rightRiser[rightIndex]
                                    rightRiser[rightIndex] = temp

                                    isSwapWithLeftMode.value = false
                                    swapWithLeftIndex.value = null
                                    selectedImage.value = null
                                    return@clickable
                                }

                                val emptySlotIndex = rightRiser.indexOfFirst { it == null }
                                if (emptySlotIndex != -1) {
                                    rightRiser[emptySlotIndex] = leftRiser[index]
                                    leftRiser[index] = null
                                }
                            }
                        } else {
                            baseModifier
                        }
                    }
                    /*
                    .let { baseModifier ->
                        if (currentMode == "Ranking" && (index == 0 || index == 1)) {
                            baseModifier.border(2.dp, CRAppTheme.colorScheme.highlight)
                        } else {
                            baseModifier
                        }
                    }

                     */
                    .padding(2.dp)

            ){
                if (currentMode == "Ranking"){
                    Text(getOrdinal(index + 1),
                        color = Color.White)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    if (leftRiser[index] != null && member != null) {
                        Image(
                            painter = rememberAsyncImagePainter(member.imageUrl),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
                        Text(member.fname, color = Color.White)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Gray, CircleShape)
                        )
                        Text("")
                    }
                }
            }
        }
    }
}

@Composable
fun rightList(
    leftRiser: MutableList<UserProfile?>,
    rightRiser: MutableList<UserProfile?>,
    selectedAction: MutableState<Int?>,
    selectedImage: MutableState<UserProfile?>,
    isSwapWithRightMode: MutableState<Boolean>,
    swapWithRightIndex: MutableState<Int?>,
    isSwapWithLeftMode: MutableState<Boolean>,
    swapWithLeftIndex: MutableState<Int?>,
    isRightSideClickable: MutableState<Boolean>
) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
    ) {
        Text(
            if (isRightSideClickable.value) "New" else "Your Placement",
            style = CRAppTheme.typography.H2,
            color = Color.White
        )
        rightRiser.forEachIndexed { index, member ->
            //val imageRes = rightRiser.getOrNull(index)
            Row (
                modifier = Modifier
                    .then(if (index == 0 || index == 1){
                        Modifier.border(2.dp, CRAppTheme.colorScheme.highlight)
                    } else {
                        Modifier
                    })
                    .clickable (
                        enabled = !isSwapWithLeftMode.value && isRightSideClickable.value
                    ) {
                        if (member != null){
                            if (isSwapWithRightMode.value){
                                if (swapWithRightIndex.value != null && swapWithRightIndex.value != index){
                                    rightRiser.swap(swapWithRightIndex.value!!, index)
                                    isSwapWithRightMode.value = false
                                    swapWithRightIndex.value = null
                                    selectedImage.value = null
                                }
                            } else if (isSwapWithLeftMode.value && swapWithLeftIndex.value == null){
                                swapWithLeftIndex.value = index
                            } else {
                                selectedAction.value = index
                                selectedImage.value = member
                            }
                        }
                    }
            ){
                Text(getOrdinal(index + 1), color = Color.White, modifier = Modifier.padding(8.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier

                ) {
                    if (member != null) {
                        Image(
                            painter = rememberAsyncImagePainter(member.imageUrl),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Black, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Black, CircleShape)
                        )
                    }
                    Text("")
                }
            }
        }
    }

    // Show selection dialog when an image is clicked
    selectedAction.value?.let { index ->
        ActionDialog(
            onDismiss = {
                selectedAction.value = null
                selectedImage.value = null
            },
            onChoiceSelected = { choice ->
                // handle choice action here
                when (choice) {
                    1 -> {
                        moveToLeftColumn(
                            index,
                            rightRiser,
                            leftRiser
                        )
                        selectedImage.value = null
                    }
                    2 -> {
                        isSwapWithRightMode.value = true
                        swapWithRightIndex.value = index
                    }
                    3 -> {
                        isSwapWithLeftMode.value = true
                        swapWithLeftIndex.value = index
                    }

                }
                selectedAction.value = null
            }
        )
    }
}
@Composable
fun UsersSelectedRank(
    crRoomId: String,
    viewModel: ChatViewModel = viewModel()
) {


    // Create SharedPreferences
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    // Initialize ChatRiseViewModel with the factory
    val crViewModel: ChatRiseViewModel = viewModel(
        factory = ChatRiseViewModelFactory(sharedPreferences, viewModel)
    )


    LaunchedEffect(crRoomId){
        crViewModel.fetchUserVote(crRoomId)
    }
    val rankVote by crViewModel.userRankVote.collectAsState()
    val usesdfrVote by remember { crViewModel.userRankVote }.collectAsState()

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
    ) {
        Text(
            "Your New Choices",
            style = CRAppTheme.typography.H2,
            color = Color.White
        )
        rankVote.forEachIndexed { index, (member) ->
            Row (
                modifier = Modifier
                    .then(if (index == 0 || index == 1){
                        Modifier.border(2.dp, CRAppTheme.colorScheme.highlight)
                    } else {
                        Modifier
                    })

            ){
                Text(getOrdinal(index + 1), color = Color.White, modifier = Modifier.padding(8.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier

                ) {
                    if (member != null) {
                        Image(
                            painter = rememberAsyncImagePainter(member.imageUrl),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Black, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Black, CircleShape)
                        )
                    }
                    Text("")
                }
            }
        }
    }
}

@Composable
fun ActionDialog(
    onDismiss: () -> Unit,
    onChoiceSelected: (Int) -> Unit
){
    Dialog(
        onDismissRequest = onDismiss,
        content = {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                IconButton(onClick = { onChoiceSelected(1)}){
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(75.dp)
                    )
                }
                Spacer(modifier = Modifier.height(100.dp))
                Text(
                    "Switch Ranks",
                    color = Color.White,
                    modifier = Modifier
                        .padding(10.dp)
                        .clickable {
                            onChoiceSelected(2)
                        }
                )
                Spacer(modifier = Modifier.height(100.dp))
                Text(
                    "Swap With Left",
                    color = Color.White,
                    modifier = Modifier
                        .padding(10.dp)
                        .clickable {
                            onChoiceSelected(3)
                        }
                )
            }
        }
    )
}

fun moveToLeftColumn(
    index: Int,
    rightRiser: MutableList<UserProfile?>,
    leftRiser: MutableList<UserProfile?>
){
    val member = rightRiser[index]
    if (member != null){
        // find first available empty slot on left side
        val emptySlotIndex = leftRiser.indexOfFirst { it == null }
        if (emptySlotIndex != -1){
            // add image to left side
            leftRiser[emptySlotIndex] = member
            // remove member from right side
            rightRiser[index] = null
        }
        return
    }
}
fun <T> MutableList<T>.swap(index1: Int, index2: Int){
    val temp = this[index1]
    this[index1] = this[index2]
    this[index2] = temp
}

fun getOrdinal(number: Int): String {
    return when {
        number % 100 in 11..13 -> "${number}th" // Handle special case for 11th, 12th, 13th
        number % 10 == 1 -> "${number}st"
        number % 10 == 2 -> "${number}nd"
        number % 10 == 3 -> "${number}rd"
        else -> "${number}th"
    }
}


fun finalizeRanking(
    crRoomId: String,
    rightRiser: List<UserProfile?>,
    userId: String,
    crViewModel: ChatRiseViewModel
){
    val sortedMembers = rightRiser.filterNotNull().reversed()
    var points = 5 // increments of 5

    sortedMembers.forEachIndexed { _, member ->
        crViewModel.saveRanking(
            crRoomId = crRoomId,
            memberId = member.userId,
            userId = userId,
            newPoints = points
        )
        points += 5
    }
}