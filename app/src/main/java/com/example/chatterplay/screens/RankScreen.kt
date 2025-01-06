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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.chatterplay.R
import com.example.chatterplay.ui.theme.CRAppTheme

@Composable
fun RankScreen() {
    val imageResources = listOf(
        R.drawable.anonymous,
        R.drawable.account_select_person,
        R.drawable.cool_neon,
        R.drawable.account_select_person2,
        R.drawable.cool_purple,
        R.drawable.person_sillouette,
        R.drawable.pic4
    )
    val selectedRisers = remember { mutableStateListOf<Int?>().apply { repeat(7) {add(null)} }}
    val leftImages = remember { mutableStateListOf<Int?>().apply { repeat(7) {addAll(imageResources)} }}
    val selectedAction = remember { mutableStateOf<Int?>(null)}
    val visibleState = remember { mutableStateListOf(*Array(7) {true}) }
    val isSwapWithRightMode = remember { mutableStateOf(false) } // Track swap mode
    val swapWithRightIndex = remember { mutableStateOf<Int?>(null) } // Store index of first selected image
    val isSwapWithLeftMode = remember { mutableStateOf(false) }
    val swapWithLeftIndex = remember { mutableStateOf<Int?>(null) }




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
            LeftRankSelectRow(
                selectedRiser = selectedRisers,
                leftImages = leftImages,
                visibleState = visibleState,
                isSwapWithLeftMode = isSwapWithLeftMode,
                swapWithLeftIndex = swapWithLeftIndex,
                isSwapWithRightMode = isSwapWithRightMode
            )
            Spacer(modifier = Modifier.width(100.dp))

            RightRankSelectRow(
                selectedRiser = selectedRisers,
                selectedAction = selectedAction,
                visibleState = visibleState,
                leftImages = leftImages,
                isSwapWithRightMode = isSwapWithRightMode,
                swapWithRightIndex = swapWithRightIndex,
                isSwapWithLeftMode = isSwapWithLeftMode,
                swapWithLeftIndex = swapWithLeftIndex
            )
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
                        .offset(y = (-100).dp)
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color.Black, CircleShape)
                        .clickable {
                            isSwapWithLeftMode.value = false
                            isSwapWithRightMode.value = false
                            swapWithLeftIndex.value = null
                            swapWithRightIndex.value = null
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
        }

    }
}

@Composable
fun LeftRankSelectRow(
    selectedRiser: MutableList<Int?>,
    leftImages: MutableList<Int?>,
    visibleState: MutableList<Boolean>,
    isSwapWithLeftMode: MutableState<Boolean>,
    swapWithLeftIndex: MutableState<Int?>,
    isSwapWithRightMode: MutableState<Boolean>
) {
    val names = listOf(
        "James",
        "Henrey",
        "Jason",
        "Robert",
        "Tom",
        "Dave",
        "Hillary"
    )

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
    ) {
        repeat(7) { index ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                if (visibleState[index] && leftImages[index] != null) {
                    Image(
                        painter = painterResource(leftImages[index]!!),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .clickable(
                                enabled = !isSwapWithRightMode.value
                            ){
                                if (isSwapWithLeftMode.value && swapWithLeftIndex.value != null){
                                    val rightIndex = swapWithLeftIndex.value!!
                                    val temp = leftImages[index]
                                    leftImages[index] = selectedRiser[rightIndex]
                                    selectedRiser[rightIndex] = temp

                                    isSwapWithLeftMode.value = false
                                    swapWithLeftIndex.value = null
                                    return@clickable
                                }

                                val emptySlotIndex = selectedRiser.indexOfFirst { it == null }
                                visibleState[index] = false
                                if (emptySlotIndex != -1){
                                    selectedRiser[emptySlotIndex] = leftImages[index]
                                    leftImages[index] = null
                                }
                            }
                    )
                    Text(names[index])
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

@Composable
fun RightRankSelectRow(
    selectedRiser: MutableList<Int?>,
    selectedAction: MutableState<Int?>,
    visibleState: MutableList<Boolean>,
    leftImages: MutableList<Int?>,
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
        repeat(7) { index ->
            val imageRes = selectedRiser.getOrNull(index)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable (
                        enabled = !isSwapWithLeftMode.value
                    ) {
                        if (isSwapWithRightMode.value){
                            if (swapWithRightIndex.value != null && swapWithRightIndex.value != index){
                                selectedRiser.swap(swapWithRightIndex.value!!, index)
                                isSwapWithRightMode.value = false
                                swapWithRightIndex.value = null
                            }
                        } else if (isSwapWithLeftMode.value && swapWithLeftIndex.value == null){
                            swapWithLeftIndex.value = index
                        } else if (imageRes != null) {
                            selectedAction.value = index
                        }
                    }
            ) {
                Text("Rank ${index + 1}", modifier = Modifier.padding(8.dp))

                if (imageRes != null){
                    Image(
                        painter = painterResource(imageRes),
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
            }
        }
    }

    // Show selection dialog when an image is clicked
    selectedAction.value?.let { index ->
        ActionDialog(
            onDismiss = { selectedAction.value = null},
            onChoiceSelected = { choice ->
                // handle choice action here
                when (choice) {
                    1 -> {
                        moveToLeftColumn(
                            index,
                            selectedRiser,
                            visibleState,
                            leftImages = leftImages
                        )
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
                        .clickable { onChoiceSelected(2) }
                )
                Spacer(modifier = Modifier.height(100.dp))
                Text(
                    "Swap With Left",
                    color = Color.White,
                    modifier = Modifier
                        .padding(10.dp)
                        .clickable { onChoiceSelected(3) }
                )
            }
        }
    )
}

fun moveToLeftColumn(
    index: Int,
    selectedRisers: MutableList<Int?>,
    visibleState: MutableList<Boolean>,
    leftImages: MutableList<Int?>
){
    val imageRes = selectedRisers[index]
    if (imageRes != null){
        // find first available empty slot on left side
        val emptySlotIndex = visibleState.indexOfFirst { !it }
        if (emptySlotIndex != -1){
            // add image to left side
            leftImages[emptySlotIndex] = imageRes
            visibleState[emptySlotIndex] = true
        }
        // remove image from right side
        selectedRisers[index] = null
    }
}
fun <T> MutableList<T>.swap(index1: Int, index2: Int){
    val temp = this[index1]
    this[index1] = this[index2]
    this[index2] = temp
}
@Preview
@Composable
fun testRank(){
    CRAppTheme{
        Surface {
            RankScreen()
        }
    }
}