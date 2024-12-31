package com.example.chatterplay.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chatterplay.screens.ChatRiseScreen
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
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(navController: NavHostController) {
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val startDestination = if (currentUser.isNotEmpty()) "roomSelect" else "loginScreen"

    if (startDestination != "Loading"){
        NavHost(navController = navController, startDestination =  startDestination){

            composable("loginScreen"){
                LoginScreen(navController = navController)
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
            composable("mainScreen/{CRRoomId}"){backStackEntry ->
                val CRRoomId = backStackEntry.arguments?.getString("CRRoomId")
                if (CRRoomId != null){
                    ChatRiseScreen(CRRoomId = CRRoomId, navController = navController)
                }
            }
            composable("inviteScreen/{CRRoomId}/{game}",
                arguments = listOf(
                    navArgument("game") {type = NavType.BoolType}
                )){backStackEntry ->
                val game = backStackEntry.arguments?.getBoolean("game") ?: false
                val CRRoomId = backStackEntry.arguments?.getString("CRRoomId") ?: ""

                InviteScreen(CRRoomId = CRRoomId, game = game, navController = navController)
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
            composable("chatScreen/{CRRoomId}/{roomId}/{game}", arguments = listOf(
                navArgument("game") {type = NavType.BoolType}
            )){backStackEntry ->
                val game = backStackEntry.arguments?.getBoolean("game") ?: false
                val CRRoomId = backStackEntry.arguments?.getString("CRRoomId") ?: "0"
                val roomId = backStackEntry.arguments?.getString("roomId")

                if (roomId != null){
                    ChattingScreen(CRRoomId = CRRoomId, roomId = roomId, game = game, navController = navController)
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
