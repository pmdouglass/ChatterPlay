package com.example.chatterplay.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chatterplay.screens.CRMainChat
import com.example.chatterplay.screens.Game
import com.example.chatterplay.screens.Profile

@Composable
fun CRNavHost(navController: NavHostController, CRRoomId: String){
    NavHost(navController = navController, startDestination = "CRHome/$CRRoomId"){
        composable("CRHome/{CRRoomId}"){
            CRMainChat(CRRoomId = CRRoomId)
        }
        composable("game/{CRRoomId}"){
            Game(CRRoomId = CRRoomId, contentNavController = navController)
        }
        composable("profile"){
            Profile()
        }
    }
}