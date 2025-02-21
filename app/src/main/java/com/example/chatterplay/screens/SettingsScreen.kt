package com.example.chatterplay.screens

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chatterplay.MainActivity
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.analytics.ScreenPresenceLogger
import com.example.chatterplay.seperate_composables.MainTopAppBar
import com.example.chatterplay.seperate_composables.SettingsInfoRow
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(game: Boolean, settingsModel: SettingsViewModel = viewModel(), navController: NavController) {

    val coroutineScope = rememberCoroutineScope()
    val isAnalyticsEnabled by settingsModel.isAnalyticsEnabled.collectAsState(true)

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current
    LaunchedEffect(Unit){
        // Log the event in Firebase Analytics
        val params = Bundle().apply {
            putString("screen_name", "SettingsScreen")
            putString("user_id", userId)
            putString("timestamp", System.currentTimeMillis().toString())
        }
        AnalyticsManager.getInstance(context).logEvent("screen_view", params)
    }
    ScreenPresenceLogger(screenName = "SettingsScreen", userId = userId)
    (context as? MainActivity)?.setCurrentScreen(("SettingsScreen"))


    Scaffold(
        topBar = {
            if (!game) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Settings",
                            style = CRAppTheme.typography.headingLarge,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
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
            } else {
                MainTopAppBar(
                    title = "Profile",
                    action = true,
                    actionIcon = Icons.Default.MoreVert,
                    onAction = {

                    },
                    navController = navController
                )
            }

        },
        content = { paddingValues ->
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxSize()
                    .background(CRAppTheme.colorScheme.background)
                    .padding(10.dp)
                    .padding(paddingValues)
            ) {
                Text(
                    text = "Account",
                    style = CRAppTheme.typography.H1,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp)

                )
                Card (
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    elevation = CardDefaults.cardElevation(8.dp)
                ){
                    SettingsInfoRow(
                        select = true,
                        icon = Icons.Default.Person,
                        contentDescription = null,
                        arrow = true,
                        title = "Personal Info",
                        onClick = { navController.navigate("editPersonalInfo") }
                    )
                    SettingsInfoRow(
                        select = true,
                        icon = Icons.Default.ManageAccounts,
                        contentDescription = null,
                        arrow = true,
                        title = "Edit Profile",
                        onClick = {navController.navigate("editProfile")}
                    )

                }


                Text(
                    text = "Support and About",
                    style = CRAppTheme.typography.H1,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp)

                )
                Card (
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    elevation = CardDefaults.cardElevation(8.dp)
                ){
                    SettingsInfoRow(
                        select = true,
                        icon = Icons.Default.Info,
                        contentDescription = null,
                        title = "Terms of Service",
                        onClick = {
                            navController.navigate("termsAndConditions")
                        }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ){
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 30.dp)
                        )
                        Text(
                            "Collect Data",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = isAnalyticsEnabled,
                            onCheckedChange = { isChecked ->
                                coroutineScope.launch {
                                    settingsModel.setAnalyticsEnabled(isChecked)
                                }
                            }
                        )
                    }
                }


                Text(
                    text = "Actions",
                    style = CRAppTheme.typography.H1,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp)
                )
                Card (
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    elevation = CardDefaults.cardElevation(8.dp)
                ){
                    SettingsInfoRow(
                        select = true,
                        icon = Icons.AutoMirrored.Default.Logout,
                        contentDescription = null,
                        title = "Log out",
                        onClick = {

                            coroutineScope.launch {
                                // Log the logout event
                                val params = Bundle().apply {
                                    putString("user_id", userId)
                                    putString("login_method", "email") // Change as needed for other methods
                                }
                                AnalyticsManager.getInstance(context).logEvent("user_logout", params)
                            }

                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("loginScreen") {
                                popUpTo(0)
                            }
                        }
                    )

                }

            }
        }
    )
}
