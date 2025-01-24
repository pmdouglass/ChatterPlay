package com.example.chatterplay.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatterplay.seperate_composables.MainTopAppBar
import com.example.chatterplay.seperate_composables.SettingsInfoRow
import com.example.chatterplay.ui.theme.CRAppTheme
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(game: Boolean, navController: NavController) {


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
