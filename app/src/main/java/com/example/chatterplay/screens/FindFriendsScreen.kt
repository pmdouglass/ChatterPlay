package com.example.chatterplay.screens

import android.os.Bundle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.analytics.ScreenPresenceLogger
import com.example.chatterplay.data_class.DateOfBirth
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.seperate_composables.FriendInfoRow
import com.example.chatterplay.seperate_composables.RowState
import com.example.chatterplay.ui.theme.CRAppTheme
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindFriends(navController: NavController) {

    val tabs = listOf("Following", "Followers", "Friends")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current
    LaunchedEffect(Unit){
        // Log the event in Firebase Analytics
        val params = Bundle().apply {
            putString("screen_name", "FindFriends")
            putString("user_id", userId)
        }
        AnalyticsManager.getInstance(context).logEvent("screen_view", params)
    }
    ScreenPresenceLogger(screenName = "FindFriends", userId = userId)


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
                            FriendInfoRow(game = false, user = fakeData, onUserSelected = {}, state = RowState.None.string)
                        }
                    }
                    // Followers
                    1 -> {
                        friends.forEach { friend ->
                            FriendInfoRow(game = false, user = fakeData, onUserSelected = {}, state = RowState.Follow.string)
                        }
                    }
                    // Friends
                    2 -> {
                        friends.forEach { friend ->
                            FriendInfoRow(game = false, user = fakeData, onUserSelected = {}, state = RowState.Check.string)
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
