package com.example.chatterplay.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.chatterplay.seperate_composables.ChatRiseThumbnail
import com.example.chatterplay.seperate_composables.RoomRow
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import okhttp3.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRoomSelect(navController: NavController, viewModel: ChatViewModel = viewModel()) {

    val userProfile by viewModel.userProfile.collectAsState()
    val crUserProfile by viewModel.crUserProfile.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "123"
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val gameStatus = "start"
    var search by remember { mutableStateOf("")}

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 20.dp)
                ){
                    IconButton(onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                    IconButton(onClick = {navController.navigate("settingsScreen")}) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                }

                Divider()
                Spacer(modifier = Modifier.height(20.dp))
                NavigationDrawerItem(
                    label = {
                        Text("Profile")
                            },
                    selected = false,
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        navController.navigate("profileScreen/false/true")
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Sign Out") },
                    selected = false,
                    icon = {
                           Icons.Default.AutoStories
                    },
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("loginScreen") {
                            popUpTo(0)
                        }
                    }
                )

            }

        }
    ) {
        Scaffold (
            topBar = {
                TopAppBar(title = {  },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CRAppTheme.colorScheme.background
                    ),
                    navigationIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.cool_neon),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .clickable {
                                    coroutineScope.launch {
                                        drawerState.open()
                                    }
                                }
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("imagepicker") {
                                popUpTo(0)
                            }
                        }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            val CRRoomId = "0"
                            navController.navigate("inviteScreen/$CRRoomId/false")
                        }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                )
            },
            content = {PaddingValues ->
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CRAppTheme.colorScheme.background)
                        .padding(PaddingValues)
                ){
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(userId)
                    Spacer(modifier = Modifier.height(10.dp))
                    ChatRiseThumbnail(navController = navController)
                    Divider()
                    Text(
                        "Conversations",
                        style = CRAppTheme.typography.headingMedium,
                        modifier = Modifier.padding(start = 8.dp, bottom = 10.dp)
                    )
                    OutlinedTextField(
                        value = search,
                        onValueChange = {search = it},
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CRAppTheme.colorScheme.textOnBackground)},
                        placeholder = {Text("Search", color = CRAppTheme.colorScheme.textOnBackground)},
                        modifier = Modifier
                            .fillMaxWidth()
                            //.height(25.dp)
                            .padding(start = 10.dp, end = 10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp)
                    ){
                        Text("Sort By")
                        Text("Filter")

                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    RoomRow(
                        members = 3,
                        title = "Pence Park Ultimate Frisbee",
                        who = "Tim C",
                        message = "Did you guys see my awsome play?",
                        time = "10:15am",
                        unread = 12,
                        game = false,
                        navController = navController
                    )
                    RoomRow(
                        members = 13,
                        title = "Movie Buffs",
                        who = "John L",
                        message = "Just saw the latest thriller - the plot twist was insane!",
                        time = " 6:20pm",
                        unread = 7,
                        game = false,
                        navController = navController
                    )
                    RoomRow(
                        members = 2,
                        title = "Tech Talk",
                        who = "Chris O",
                        message = "Has anyone tried the new VR headset? Wondering if it's worth the hype!",
                        time = "1:15 pm",
                        unread = 0,
                        game = false,
                        navController = navController
                    )
                    RoomRow(
                        members = 21,
                        title ="Book Nook",
                        who = "Amy W",
                        message = "Just finished Pachinko - the family dynamics were so intense.",
                        time = "4:10 pm",
                        unread = 21,
                        game = false,
                        navController = navController
                    )

                }
            }
        )
    }

}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode", showBackground = true)
@Composable
fun ThemeRoomSelect() {
    CRAppTheme () {
        Surface {
            MainRoomSelect(navController = rememberNavController())
        }
    }
}