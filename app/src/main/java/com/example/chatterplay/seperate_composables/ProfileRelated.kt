package com.example.chatterplay.seperate_composables

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatRiseViewModel
import com.example.chatterplay.view_model.ChatRiseViewModelFactory
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AllMembersRow(
    selectedMember: ((UserProfile) -> Unit)? = null,
    chatRoomMembers: List<UserProfile>,
    game: Boolean,
    self: Boolean,
    navController: NavController = rememberNavController()
) {
    LazyRow (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        items(chatRoomMembers) { member ->
            UserProfileIcon(
                selectedMember = {selectedMember?.invoke(member)},
                chatMember = member,
                game = game,
                self = self,
                imgSize = 50,
                txtSize = 20,
                navController = navController
            )
        }

    }
}

@Composable
fun UserProfileIcon(
    selectedMember: ((UserProfile) -> Unit)? = null,
    chatMember: UserProfile,
    imgSize: Int = 30,
    txtSize: Int = 10,
    game: Boolean,
    self: Boolean,
    viewModel: ChatViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Initialize ChatRiseViewModel with the factory
    val crViewModel: ChatRiseViewModel = viewModel(
        factory = ChatRiseViewModelFactory(sharedPreferences, viewModel)
    )

    val topPlayers by crViewModel.topTwoPlayers.collectAsState()
    val blockedPlayerId by crViewModel.blockedPlayerId.collectAsState()

    val isTopPlayer by remember {
        derivedStateOf {
            topPlayers?.let { (rank1, rank2) ->
                val rank1 = rank1.first
                val rank2 = rank2.first

                chatMember.userId == rank1 || chatMember.userId == rank2
            } ?: false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(10.dp)
            .clickable {
                if (!game){
                    if (!self){
                        navController.navigate("profileScreen/${game}/${self}/${chatMember.userId}")
                    }
                } else {
                    if (!self){
                        if (blockedPlayerId != chatMember.userId){
                            selectedMember?.invoke(chatMember)
                        }
                    }
                }
            }
    ){
        Image(
            painter = rememberAsyncImagePainter(chatMember.imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(imgSize.dp)
                .clip(CircleShape)
                .then(
                    if (game)
                        when{
                            isTopPlayer -> Modifier.border(2.dp, CRAppTheme.colorScheme.highlight, CircleShape)
                            blockedPlayerId == chatMember.userId -> Modifier.border(2.dp, Color.Gray, CircleShape)
                            else -> Modifier
                        }
                    else
                        Modifier
                )
        )
        Text(
            chatMember.fname,
            fontSize = txtSize.sp,
            color =
            if (game)
                if (blockedPlayerId == chatMember.userId)
                    Color.Gray
                else
                    Color.White
            else
                Color.Black
        )

    }
}

