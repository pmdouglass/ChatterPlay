package com.example.chatterplay.seperate_composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chatterplay.R
import com.example.chatterplay.ui.theme.CRAppTheme

enum class screenName (val string: String){
    home(""),
    chat(""),
    profile("Profile"),
    settings("Settings")
}

@Composable
fun BaseTopBar(
    title: screenName,
    onImageClick: () -> Unit
){

    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
    ){
        // Leading Icon
        if (title == screenName.home){
            Image(
                painter = painterResource(R.drawable.anonymous),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(35.dp)
                    .clip(CircleShape)
                    .clickable { onImageClick() }
            )
        }else {
            IconButton(onClick = {}){
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier
                        .size(35.dp)
                )
            }
        }



        // Title
        Text(text =
        when (title) {
            screenName.home -> screenName.home.string
            screenName.chat -> screenName.chat.string
            screenName.settings -> screenName.settings.string
            screenName.profile -> screenName.profile.string
            else -> "Nothing Selected"
                                 },
            style = CRAppTheme.typography.H3,
            modifier = Modifier
                .padding(start = 10.dp)

        )


        // Trailing Icon
        when (title) {
            screenName.chat, screenName.settings -> {}
            screenName.profile -> {
                IconButton(onClick = {}){
                    Icon(
                        Icons.Default.ManageAccounts,
                        contentDescription = null,
                        modifier = Modifier
                            .size(35.dp)
                    )
                }
            }
            else -> {
                IconButton(onClick = {}){
                    Icon(
                        Icons.AutoMirrored.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier
                            .size(35.dp)
                    )
                }
            }
        }

    }
}

@Preview
@Composable
fun prevtopba(){
    CRAppTheme {
        Surface {
            Column{
                BaseTopBar(title = screenName.home, onImageClick = {})
                BaseTopBar(title = screenName.chat, onImageClick = {})
                BaseTopBar(title = screenName.profile, onImageClick = {})
                BaseTopBar(title = screenName.settings, onImageClick = {})
            }
        }
    }
}