package com.example.chatterplay.seperate_composables

import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.data_class.UserProfile

@Composable
fun AllMembersRow(selectedMember: ((UserProfile) -> Unit)? = null, chatRoomMembers: List<UserProfile>, game: Boolean, self: Boolean, navController: NavController) {
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
fun UserProfileIcon(selectedMember: ((UserProfile) -> Unit)? = null, chatMember: UserProfile, imgSize: Int = 30, txtSize: Int = 10, game: Boolean, self: Boolean, navController: NavController) {
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
                        selectedMember?.invoke(chatMember)
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
        )
        Text(
            chatMember.fname,
            fontSize = txtSize.sp
        )

    }
}

