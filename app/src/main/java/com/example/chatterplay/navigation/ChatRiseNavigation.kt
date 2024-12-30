package com.example.chatterplay.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chatterplay.screens.CRMainChat
import com.example.chatterplay.screens.Game
import com.example.chatterplay.screens.Profile

@Composable
fun CRNavHost(navController: NavHostController){
    NavHost(navController = navController, startDestination = "CRHome"){
        composable("CRHome"){
            CRMainChat()
        }
        composable("game"){
            Game()
        }
        composable("profile"){
            Profile()
        }
    }
}