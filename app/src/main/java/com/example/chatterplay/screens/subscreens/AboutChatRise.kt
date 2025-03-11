package com.example.chatterplay.screens.subscreens

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatterplay.MainActivity
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.ui.theme.CRAppTheme
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutChatRise(navController: NavController) {

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current
    LaunchedEffect(Unit){
        // Log the event in Firebase Analytics
        val params = Bundle().apply {
            putString("screen_name", "AboutChatRiseScreen")
            putString("user_id", userId)
            putString("timestamp", System.currentTimeMillis().toString())
        }
        AnalyticsManager.getInstance(context).logEvent("screen_view", params)
    }
    (context as? MainActivity)?.setCurrentScreen(("AboutChatRiseScreen"))


    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text("About ChatRise")
                },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier
                                .size(35.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CRAppTheme.colorScheme.background
                )
            )
        },
        content = {paddingValues ->
            Column (
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ){
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                ){
                    Text("How It Works:",
                        style = CRAppTheme.typography.H3,
                        modifier = Modifier
                            .padding(top = 15.dp, bottom = 35.dp)
                    )

                    Text("Social Media Profiles:",
                        style = CRAppTheme.typography.T5,
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                    )
                    Text("Contestants choose to play as themself or an alternate profile (catfish, pretending to be someone else). You can choose to present yourself honestly, showcasing your real personality, or craft an entirely fabricated identity, adopting a 'catfish' persona to gain an advantage.",
                        modifier = Modifier
                            .padding(bottom = 20.dp))

                    Text("Game Dynamics:",
                        style = CRAppTheme.typography.T5,
                        modifier = Modifier
                            .padding(bottom = 10.dp))
                    Text("Players communicate solely through text-based chats, which include group conversations, private messages, and special events. Players must rely on intuition and their social skills to forge alliances, build trust, and stay in the game.",
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                    )

                    Text("Rankings and Eliminations:",
                        style = CRAppTheme.typography.T5,
                        modifier = Modifier
                            .padding(bottom = 10.dp))
                    Text("Periodically, players rank each other based on their interactions and impressions. Those ranked highest become 'influencers' earing the power to decide which contestant gets eliminated, or 'blocked'. Blocked players leave the game but may have one final chance to meet another player in person, revealing their true identity.",
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                    )

                    Text("Twist and Challenges:",
                        style = CRAppTheme.typography.T5,
                        modifier = Modifier
                            .padding(bottom = 10.dp))
                    Text("Throughout the game, unexpected developments and creative tasks keep players on their toes, forcing them to adapt their startegies and question their alliances.",
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                    )

                    HorizontalDivider()

                    Text("Goal:",
                        style = CRAppTheme.typography.T5,
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 10.dp))
                    Text("The ultimate goal is to become the most liked, trusted, and influential player by the end of the game. Contestants must skillfully navigate friendships, rivalries, and deception while managing how they are perceived within ChatRise. Success requires a careful balance of authenticity, strategic gameplay, and the ability to adapt to evolving dynamics and unforeseen challenges.",
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                    )

                }


            }
        }
    )

}
