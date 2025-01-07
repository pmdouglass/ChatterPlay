package com.example.chatterplay.screens

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.R
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.ui.theme.CRAppTheme

@Composable
fun RankingScreen(
    rankMode: Boolean,
    chatRoomMembers: List<UserProfile>,
    members: Int,
){
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {
        val memberCount = if (rankMode) members else members+1
        Text("Here is where you will rank everyone, do this nmbvvhjvbhgfxhgjb mn jb jh bkjbkjhjbhgc hjjh vhj and that slide here tap there",
            style = CRAppTheme.typography.T4,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(20.dp))
        HorizontalDivider()
        Ranking(
            rankMode = rankMode,
            chatRoomMembers = chatRoomMembers,
            memberCount = memberCount

        )
    }
}
@Composable
fun Ranking(
    memberCount: Int,
    chatRoomMembers: List<UserProfile>,
    rankMode: Boolean
) {
    val rightRiser = remember { mutableStateListOf<UserProfile?>().apply { repeat(chatRoomMembers.size) {add(null)} }}
    val leftRiser = remember { mutableStateListOf<UserProfile?>().apply  {addAll(chatRoomMembers)} }
    val selectedAction = remember { mutableStateOf<Int?>(null)}
    val isSwapWithRightMode = remember { mutableStateOf(false) }
    val swapWithRightIndex = remember { mutableStateOf<Int?>(null) }
    val isSwapWithLeftMode = remember { mutableStateOf(false) }
    val swapWithLeftIndex = remember { mutableStateOf<Int?>(null) }
    val isRightSideComplete = remember { mutableStateOf(false)}
    val selectedImage = remember { mutableStateOf<UserProfile?>(null)}


    isRightSideComplete.value = rightRiser.all { it != null }


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
            CurrentPlace(
                rankMode = rankMode,
                memberCount = memberCount,
                rightRiser = rightRiser,
                selectedImage = selectedImage,
                leftRiser = leftRiser,
                isSwapWithLeftMode = isSwapWithLeftMode,
                swapWithLeftIndex = swapWithLeftIndex,
                isSwapWithRightMode = isSwapWithRightMode
            )
            if (rankMode){
                Spacer(modifier = Modifier.width(50.dp))
                NewPlace(
                    memberCount = memberCount,
                    rightRiser = rightRiser,
                    selectedAction = selectedAction,
                    selectedImage = selectedImage,
                    leftRiser = leftRiser,
                    isSwapWithRightMode = isSwapWithRightMode,
                    swapWithRightIndex = swapWithRightIndex,
                    isSwapWithLeftMode = isSwapWithLeftMode,
                    swapWithLeftIndex = swapWithLeftIndex
                )
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
            if (isRightSideComplete.value) {
                Button(
                    onClick = {},
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
                Image(
                    painter = rememberAsyncImagePainter(selectedImage.value?.imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(125.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Black, CircleShape)
                )
            }
        }

    }
}

@Composable
fun CurrentPlace(
    memberCount: Int,
    rankMode: Boolean,
    rightRiser: MutableList<UserProfile?>,
    selectedImage: MutableState<UserProfile?>,
    leftRiser: MutableList<UserProfile?>,
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
            "Current",
            style = CRAppTheme.typography.H2,
            color = Color.White
        )
        leftRiser.forEachIndexed { index, member ->
            Row (
                modifier = Modifier
                    .let { baseModifier ->
                        if (rankMode) {
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
                    .let { baseModifier ->
                        if (!rankMode && (index == 0 || index == 1)) {
                            baseModifier.border(2.dp, CRAppTheme.colorScheme.highlight)
                        } else {
                            baseModifier
                        }
                    }

            ){
                Text(getOrdinal(index + 1),
                    color = Color.White)
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
fun NewPlace(
    memberCount: Int,
    rightRiser: MutableList<UserProfile?>,
    selectedAction: MutableState<Int?>,
    selectedImage: MutableState<UserProfile?>,
    leftRiser: MutableList<UserProfile?>,
    isSwapWithRightMode: MutableState<Boolean>,
    swapWithRightIndex: MutableState<Int?>,
    isSwapWithLeftMode: MutableState<Boolean>,
    swapWithLeftIndex: MutableState<Int?>
) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
    ) {
        Text(
            "New",
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
                        enabled = !isSwapWithLeftMode.value
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
