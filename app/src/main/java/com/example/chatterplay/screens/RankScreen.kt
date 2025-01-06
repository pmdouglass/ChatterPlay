package com.example.chatterplay.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chatterplay.R
import com.example.chatterplay.ui.theme.CRAppTheme

@Composable
fun RankScreen() {
    val selectedRisers = remember { mutableStateListOf<Int>()}
    val leftImages = remember { mutableStateListOf<Int?>( R.drawable.anonymous, R.drawable.anonymous, R.drawable.anonymous, R.drawable.anonymous, R.drawable.anonymous, R.drawable.anonymous, R.drawable.anonymous, ) }
    val selectedImageIndex = remember { mutableStateOf<Pair<Boolean, Int>?>(null) } // Pair to track if selected from left (true) or right (false)

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
    ) {
        LeftRankSelectRow(
            selectedRiser = selectedRisers
        )

        RightRankSelectRow(
            selectedRiser = selectedRisers
        )
    }
}

@Composable
fun LeftRankSelectRow(
    selectedRiser: MutableList<Int>
) {
    val visibleState = remember { mutableStateListOf(*Array(7) { true})}
    val imageResouce = R.drawable.anonymous

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .padding(start = 20.dp)
    ) {
        repeat(7) { index ->
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                if (visibleState[index]) {
                    Image(
                        painter = painterResource(imageResouce),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .clickable {
                                visibleState[index] = false
                                selectedRiser.add(imageResouce)
                            }
                    )
                    Text("James")
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
    selectedRiser: List<Int?>,

    ) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .padding(start = 100.dp)
    ) {
        repeat(7) { index ->
            val imageRes = selectedRiser.getOrNull(index)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
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