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
fun AllMembersRow(chatRoomMembers: List<UserProfile>, game: Boolean, self: Boolean, navController: NavController) {
    LazyRow (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        items(chatRoomMembers) { member ->
            UserProfileIcon(
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
fun PersonRow(userProfile: UserProfile, PicSize: Int, txtSize: Int, modifier: Modifier, game: Boolean, self: Boolean, navController: NavController, chatRoomMembers: List<UserProfile>) {
    /*Row (
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
    ){
        PersonIcon(imgSize = PicSize, firstName = "Tim", txtSize = txtSize, game = game, self = self, navController = navController)
        PersonIcon(imgSize = PicSize, firstName = "Clay", txtSize = txtSize, game = game, self = self, navController = navController)
        PersonIcon(imgSize = PicSize, firstName = "Jason", txtSize = txtSize, game = game, self = self, navController = navController)
        PersonIcon(imgSize = PicSize, firstName = "Alexandria", txtSize = txtSize, game = game, self = self, navController = navController)
        PersonIcon(imgSize = PicSize, firstName = "Mammoa", txtSize = txtSize, game = game, self = self, navController = navController)
        PersonIcon(imgSize = PicSize, firstName = "Daddy", txtSize = txtSize, game = game, self = self, navController = navController)
        PersonIcon(imgSize = PicSize, firstName = "Timothy", txtSize = txtSize, game = game, self = self, navController = navController)

    }*/
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        items(chatRoomMembers){ member ->
            PersonIcon(
                member = member,
                clickable = true,
                game = false,
                self = false,
                navController = navController
            )
        }
    }
}

@Composable
fun PersonIcon(
    member: UserProfile,
    imgSize: Int = 30,
    txtSize: Int = 10,
    clickable: Boolean = true,
    game: Boolean,
    self: Boolean,
    navController: NavController
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(10.dp)
            .clickable {
                if (clickable){
                    navController.navigate("profileScreen/${game}/${self}/${member.userId}")
                }else {

                }
            },
        verticalArrangement = Arrangement.Center
    ){
        Image(
            painter = rememberAsyncImagePainter(member.imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(imgSize.dp)
                .clip(CircleShape)
        )
        Text(
            member.fname,
            fontSize = txtSize.sp
        )

    }
}

@Composable
fun UserProfileIcon(
    chatMember: UserProfile,
    imgSize: Int = 30,
    txtSize: Int = 10,
    game: Boolean,
    self: Boolean,
    navController: NavController
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(10.dp)
            .clickable { navController.navigate("profileScreen/${game}/${self}/${chatMember.userId}") }
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

