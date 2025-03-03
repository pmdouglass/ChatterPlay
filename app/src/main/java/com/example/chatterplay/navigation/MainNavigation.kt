package com.example.chatterplay.navigation

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chatterplay.screens.ChattingScreen
import com.example.chatterplay.screens.EditPersonalInfo
import com.example.chatterplay.screens.EditProfileScreen
import com.example.chatterplay.screens.FindFriends
import com.example.chatterplay.screens.InviteScreen
import com.example.chatterplay.screens.MainRoomSelect
import com.example.chatterplay.screens.MainScreen
import com.example.chatterplay.screens.ProfileScreen
import com.example.chatterplay.screens.SettingsScreen
import com.example.chatterplay.screens.login.LoginScreen
import com.example.chatterplay.screens.login.SignupScreen1
import com.example.chatterplay.screens.login.SignupScreen2
import com.example.chatterplay.screens.login.SignupScreen3
import com.example.chatterplay.screens.login.SignupScreen4
import com.example.chatterplay.screens.subscreens.AboutChatRise
import com.example.chatterplay.screens.subscreens.TermsAndConditionsScreen
import com.example.chatterplay.view_model.EntryViewModelFactory
import com.example.chatterplay.view_model.RoomCreationViewModel
import com.example.chatterplay.view_model.SettingsViewModel
import com.example.chatterplay.view_model.SettingsViewModelFactory
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    val application = context.applicationContext as Application

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            application = application,
            sharedPreferences = sharedPreferences
        )
    )
    val entryViewModel: RoomCreationViewModel = viewModel(
        factory = EntryViewModelFactory(sharedPreferences)
    )

    val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val crRoomIdState by entryViewModel.crRoomId.collectAsState()

    if (crRoomIdState == null) {
        Log.d("MainNavigation", "Waiting for crRoomId to load...")
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator() // Show loading indicator while waiting for crRoomId
        }
        return
    }

    val crRoomId = crRoomIdState ?: "0"
    Log.d("MainNavigation", "crRoomId loaded: $crRoomId")

    val startOnMainScreen = settingsViewModel.startOnMainScreen.collectAsState()

    val startDestination =
        if (currentUser.isNotEmpty()) {
            if (startOnMainScreen.value) {
                if (crRoomId != "0") {
                    Log.d("MainNavigation", "Navigating to mainScreen with crRoomId: $crRoomId")
                    "mainScreen/$crRoomId"
                } else {
                    Log.d("MainNavigation", "crRoomId is 0, navigating to settingsScreen")
                    "settingsScreen"
                }
            } else {
                Log.d("MainNavigation", "Navigating to roomSelect")
                "roomSelect"
            }
        } else {
            Log.d("MainNavigation", "User not logged in, navigating to loginScreen")
            "loginScreen"
        }

    LaunchedEffect(crRoomId){
        if (crRoomId != "0" && startOnMainScreen.value){
            Log.d("MainNavigation", "crRoomId updated, navigating to mainScreen/$crRoomId")
            navController.navigate("mainScreen/$crRoomId")
        }
    }


    if (startDestination != "Loading"){
        NavHost(navController = navController, startDestination =  startDestination){

            composable("loginScreen"){
                LoginScreen(navController = navController)
            }
            composable("termsAndConditions"){
                TermsAndConditionsScreen(navController = navController)
            }
            composable("signupScreen1"){
                SignupScreen1(navController = navController)
            }
            composable("signupScreen2/{email}/{password}/{game}", arguments = listOf(navArgument("game") {type = NavType.BoolType})){backstackEntry ->
                val email = backstackEntry.arguments?.getString("email") ?: ""
                val password = backstackEntry.arguments?.getString("password") ?: ""
                val game = backstackEntry.arguments?.getBoolean("game") ?: false
                SignupScreen2(email = email,password = password,navController =  navController,game = game)
            }
            composable("signupScreen3/{email}/{password}/{fName}/{lName}/{month}/{day}/{year}/{age}/{gender}/{location}/{game}",
                arguments = listOf(
                    navArgument("email") {type = NavType.StringType},
                    navArgument("password") {type = NavType.StringType},
                    navArgument("fName") {type = NavType.StringType},
                    navArgument("lName") {type = NavType.StringType},
                    navArgument("month") {type = NavType.StringType},
                    navArgument("day") {type = NavType.StringType},
                    navArgument("year") {type = NavType.StringType},
                    navArgument("age") {type = NavType.StringType},
                    navArgument("gender") {type = NavType.StringType},
                    navArgument("location") {type = NavType.StringType},
                    navArgument("game") {type = NavType.BoolType}
                )
            ){backstackEntry ->
                val email = backstackEntry.arguments?.getString("email") ?: ""
                val password = backstackEntry.arguments?.getString("password") ?: ""
                val fName = backstackEntry.arguments?.getString("fName") ?: ""
                val lName = backstackEntry.arguments?.getString("lName") ?: ""
                val gender = backstackEntry.arguments?.getString("gender") ?: ""
                val age = backstackEntry.arguments?.getString("age") ?: ""
                val location = backstackEntry.arguments?.getString("location") ?: ""
                val month = backstackEntry.arguments?.getString("month") ?: ""
                val day = backstackEntry.arguments?.getString("day") ?: ""
                val year = backstackEntry.arguments?.getString("year") ?: ""
                val game = backstackEntry.arguments?.getBoolean("game") ?: false

                SignupScreen3(email = email, password = password, fName = fName, lName = lName, month = month, day = day, year = year, age = age, gender = gender, location = location, navController = navController, game = game)

            }
            composable("signupScreen4/{email}/{password}/{fName}/{lName}/{month}/{day}/{year}/{age}/{gender}/{location}/{about}/{game}",
                arguments = listOf(
                    navArgument("email") {type = NavType.StringType},
                    navArgument("password") {type = NavType.StringType},
                    navArgument("fName") {type = NavType.StringType},
                    navArgument("lName") {type = NavType.StringType},
                    navArgument("month") {type = NavType.StringType},
                    navArgument("day") {type = NavType.StringType},
                    navArgument("year") {type = NavType.StringType},
                    navArgument("age") {type = NavType.StringType},
                    navArgument("gender") {type = NavType.StringType},
                    navArgument("location") {type = NavType.StringType},
                    navArgument("game") {type = NavType.BoolType}
                )
            ){backstackEntry ->
                val email = backstackEntry.arguments?.getString("email") ?: ""
                val password = backstackEntry.arguments?.getString("password") ?: ""
                val about = backstackEntry.arguments?.getString("about") ?: ""
                val fName = backstackEntry.arguments?.getString("fName") ?: ""
                val lName = backstackEntry.arguments?.getString("lName") ?: ""
                val gender = backstackEntry.arguments?.getString("gender") ?: ""
                val age = backstackEntry.arguments?.getString("age") ?: ""
                val location = backstackEntry.arguments?.getString("location") ?: ""
                val month = backstackEntry.arguments?.getString("month") ?: ""
                val day = backstackEntry.arguments?.getString("day") ?: ""
                val year = backstackEntry.arguments?.getString("year") ?: ""
                val game = backstackEntry.arguments?.getBoolean("game") ?: false


                SignupScreen4(email = email, password = password, fName = fName, lName = lName, month = month, day = day, year = year, age = age, gender = gender, location = location, about = about, navController = navController, game = game)
            }

            composable("roomSelect"){
                MainRoomSelect(navController = navController)
            }
            composable("mainScreen/{crRoomId}"){backStackEntry ->
                Log.d("MainNavigation", "Navigating to mainScreen")
                val crRoomId = backStackEntry.arguments?.getString("crRoomId")
                if (crRoomId != null){
                    Log.d("MainScreen", "crRoomId: $crRoomId")
                    MainScreen(crRoomId = crRoomId, navController = navController)
                }else {
                    Log.d("MainScreen", "crRoomId is null, navigation error")
                }

            }
            composable("inviteScreen/{crRoomId}/{game}",
                arguments = listOf(
                    navArgument("game") {type = NavType.BoolType}
                )){backStackEntry ->
                val game = backStackEntry.arguments?.getBoolean("game") ?: false
                val crRoomId = backStackEntry.arguments?.getString("crRoomId") ?: ""

                InviteScreen(crRoomId = crRoomId, game = game, navController = navController)
            }
            composable("profileScreen/{game}/{self}/{userId}",
                arguments = listOf(
                    navArgument("game") {type = NavType.BoolType},
                    navArgument("self") {type = NavType.BoolType}
                )){backStackEntry ->
                val game = backStackEntry.arguments?.getBoolean("game") ?: false
                val self = backStackEntry.arguments?.getBoolean("self") ?: false
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                ProfileScreen(userId = userId, game = game, self = self, navController = navController)
            }
            composable("chatScreen/{crRoomId}/{roomId}/{game}/{mainChat}", arguments = listOf(
                navArgument("game") {type = NavType.BoolType},
                navArgument("mainChat") {type = NavType.BoolType}
            )){backStackEntry ->
                val game = backStackEntry.arguments?.getBoolean("game") ?: false
                val mainChat = backStackEntry.arguments?.getBoolean("mainChat") ?: false
                val crRoomId = backStackEntry.arguments?.getString("crRoomId") ?: "0"
                val roomId = backStackEntry.arguments?.getString("roomId")

                if (roomId != null){
                    ChattingScreen(crRoomId = crRoomId, roomId = roomId, game = game, mainChat = mainChat, navController = navController)
                }
            }
            composable("settingsScreen") {
                SettingsScreen(game = false, navController = navController)
            }
            composable("editPersonalInfo") {
                EditPersonalInfo(navController = navController)
            }
            composable("editProfile"){
                EditProfileScreen(navController = navController)
            }
            composable("aboutChatrise"){
                AboutChatRise(navController = navController)
            }
            composable("friendsScreen") {
                FindFriends(navController = navController)
            }
        }
    }


}
