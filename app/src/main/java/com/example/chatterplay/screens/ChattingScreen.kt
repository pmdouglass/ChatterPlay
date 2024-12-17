package com.example.chatterplay.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chatterplay.R
import com.example.chatterplay.seperate_composables.BottomInputBar
import com.example.chatterplay.seperate_composables.ChatBubble
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatterplay.seperate_composables.MainTopAppBar
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChattingScreen(
    game: Boolean,
    roomId: String,
    viewModel: ChatViewModel = viewModel(),
    navController: NavController
) {

    Scaffold (
        topBar = {
            if (game){
                MainTopAppBar(
                    title = "Private Chat",
                    action = true,
                    actionIcon = Icons.Default.Menu,
                    onAction = { /*TODO*/ },
                    navController = navController
                )
            } else {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Private Chat",
                            style = CRAppTheme.typography.headingLarge,
                            ) },
                    navigationIcon = {
                        IconButton(onClick = {navController.popBackStack()}) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = null,
                                Modifier
                                    .size(35.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CRAppTheme.colorScheme.onBackground
                    )

                )
            }
        },
        bottomBar = {
            BottomInputBar()
        }
    ){paddingValues ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (game) CRAppTheme.colorScheme.gameBackground else CRAppTheme.colorScheme.background
                )
                .padding(paddingValues)
        ){
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ){
                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cool_neon),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                    )
                    Text (
                        "Anthony",
                        style = CRAppTheme.typography.titleSmall
                    )
                }
            }
            Divider()
            Column (
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                ChatBubble(game, false)
                Spacer(modifier = Modifier.height(25.dp))
                ChatBubble(game, true)
                Spacer(modifier = Modifier.height(25.dp))

            }

        }
    }
}


@Preview
@Composable
fun TestChattingScreen() {
    CRAppTheme {
        ChattingScreen(game = true , roomId = "", navController = rememberNavController())
    }
}