package com.example.chatterplay.screens.subscreens

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.ui.theme.CRAppTheme
import com.google.firebase.auth.FirebaseAuth

@Composable
fun TermsAndConditionsScreen(navController: NavController) {

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current
    LaunchedEffect(Unit){
        // Log the event in Firebase Analytics
        val params = Bundle().apply {
            putString("screen_name", "AboutChatRise")
            putString("user_id", userId)
        }
        AnalyticsManager.getInstance(context).logEvent("screen_view", params)
    }

    Column (
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(CRAppTheme.colorScheme.background)
    ){
        Text(
            text = "Terms and Conditions",
            style = CRAppTheme.typography.headingLarge
        )

        Text(
            "Go Back",
            color = Color.Blue,
            modifier = Modifier
                .padding(top = 50.dp)
                .clickable { navController.popBackStack()}
        )
        
    }
}
