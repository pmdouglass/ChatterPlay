package com.example.chatterplay.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chatterplay.seperate_composables.BottomInputBar
import com.example.chatterplay.seperate_composables.ChatBubble
import com.example.chatterplay.seperate_composables.MainTopAppBar
import com.example.chatterplay.seperate_composables.PersonRow
import com.example.chatterplay.seperate_composables.PrivateDrawerRoomList
import com.example.chatterplay.seperate_composables.RightSideModalDrawer
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.ui.theme.customPurple
import kotlinx.coroutines.launch

@Composable
fun MainScreen(navController: NavController) {

    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    RightSideModalDrawer(
        drawerState  = drawerState,
        drawerContent = {
                        PrivateDrawerRoomList(
                            onTap = { coroutineScope.launch { drawerState.close() } },
                            onLongPress = { /*TODO*/ },
                            navController = navController
                        )
        },
        content = {
            Scaffold(
                topBar = {
                    MainTopAppBar(
                        title = "ChatRise",
                        action = true,
                        actionIcon = Icons.Default.Menu,
                        onAction = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        navController = navController
                        )
                },
                bottomBar = {
                    BottomInputBar()
                },
                content = {paddingValues ->
                    Column (
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(customPurple)
                            .padding(paddingValues)
                    ){
                        PersonRow(
                            PicSize = 40,
                            txtSize = 8,
                            game = true,
                            self = false,
                            navController = navController,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 10.dp)
                        )
                        Divider()
                        Column (
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            ChatBubble(true, false)
                            Spacer(modifier = Modifier.height(25.dp))
                            ChatBubble(true, true)
                            Spacer(modifier = Modifier.height(25.dp))
                        }
                    }
                }
            )
        }
    )




}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode", showBackground = true)
@Composable
fun PreviewMainScreen() {
    CRAppTheme () {
        Surface {
            MainScreen(navController = rememberNavController())
        }
    }
}