package com.example.chatterplay.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chatterplay.data_class.DateOfBirth
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.seperate_composables.FriendInfoRow
import com.example.chatterplay.seperate_composables.RowState
import com.example.chatterplay.ui.theme.CRAppTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindFriends(navController: NavController) {

    val tabs = listOf("Following", "Followers", "Friends")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profile Name",
                        style = CRAppTheme.typography.headingLarge
                    ) },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier
                                .size(35.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CRAppTheme.colorScheme.onBackground
                )
            )
        },
        content = {paddingValues ->
            Column (
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ){
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index},
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    }
                }
                val friends = List(5) {"Friend $it"}
                val fakeData = UserProfile(
                    userId = "",
                    fname = "Phillip",
                    lname = "Douglass",
                    gender = "male",
                    dob = DateOfBirth(month = "Jan", "25", "1004"),
                    age = "35",
                    location = "PA",
                    about = "ksjf fdklsd flksjkdf nsdnf sj fsdjf",
                    imageUrl = "",
                    pending = "NotPending"

                )
                when (selectedTabIndex) {
                    // Following
                    0 -> {
                        friends.forEach { friend ->
                            FriendInfoRow(game = false, user = fakeData, onUserSelected = {}, state = RowState.none.string)
                        }
                    }
                    // Followers
                    1 -> {
                        friends.forEach { friend ->
                            FriendInfoRow(game = false, user = fakeData, onUserSelected = {}, state = RowState.follow.string)
                        }
                    }
                    // Friends
                    2 -> {
                        friends.forEach { friend ->
                            FriendInfoRow(game = false, user = fakeData, onUserSelected = {}, state = RowState.check.string)
                        }
                    }
                    else -> {
                        Text("Nothing Selected")
                    }
                }
            }
        }
    )
}
