package com.example.chatterplay.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.R
import com.example.chatterplay.seperate_composables.MainTopAppBar
import com.example.chatterplay.seperate_composables.rememberProfileState
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    game: Boolean,
    self: Boolean,
    viewModel: ChatViewModel = viewModel(),
    navController: NavController
) {

    val (personalProfile, alternateProfile) = rememberProfileState(userId = userId, viewModel)
    val tabs = listOf("Personal", "Alternate")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var expand by remember { mutableStateOf(true)}
    val Clicked = if (expand) 550.dp else 250.dp
    var notes by remember { mutableStateOf("")}


    
    LaunchedEffect(Unit) {
        viewModel.getUserProfile(userId = userId)
    }

    Scaffold(
        topBar = {
            if (!game){
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Profile",
                            style = CRAppTheme.typography.headingLarge,
                        ) },
                    navigationIcon = {
                        IconButton(onClick = {navController.popBackStack()}) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = null,
                                Modifier
                                    .size(35.dp)
                            )
                        }
                    },
                    actions = {
                        if (self){
                            IconButton(onClick = {navController.navigate("editProfile")}) {
                                Icon(
                                    Icons.Default.ManageAccounts,
                                    contentDescription = null,
                                    modifier = Modifier.size(35.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CRAppTheme.colorScheme.onBackground
                    )

                )
            } else {
                MainTopAppBar(
                    title = "Profile",
                    action = true,
                    actionIcon = Icons.Default.MoreVert,
                    onAction = {

                    },
                    navController = navController
                )
            }

        },
        content = {paddingValues ->
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (!game && self){
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = CRAppTheme.colorScheme.background,
                        contentColor = Color.Black
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                        }
                    }
                }
                

                when (selectedTabIndex) {
                    0 -> {
                        Image(
                            painter = rememberAsyncImagePainter(personalProfile.imageUrl),
                            contentDescription =null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clickable { expand = false }
                        )
                    }
                    1 -> {
                        if (alternateProfile.fname.isBlank()){
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxSize()
                            ){

                                Button(onClick = {
                                    val email = "dummyEmail"
                                    val password = "DummyPassword"
                                    navController.navigate("signupScreen2/${email}/${password}/true")
                                }) {
                                    Text("Create Alternate Profile")
                                }
                            }
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(alternateProfile.imageUrl),
                                contentDescription =null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clickable { expand = false }
                            )
                        }
                    }
                    else -> {
                        Image(
                            painter = painterResource(id = R.drawable.person_sillouette),
                            contentDescription =null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clickable { expand = false }
                        )
                    }
                }


                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Clicked)
                        .background(
                            if (!game)
                                when (selectedTabIndex) {
                                    0 -> CRAppTheme.colorScheme.background
                                    1 -> CRAppTheme.colorScheme.gameBackground
                                    else -> Color.White
                                }
                            else
                                CRAppTheme.colorScheme.gameBackground
                        )
                        .padding(15.dp)
                            then(
                            if (expand) {
                                Modifier.verticalScroll(rememberScrollState())
                            } else {
                                Modifier
                            }
                            )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .clickable { expand = !expand }
                    ){
                        HorizontalDivider(
                            modifier = Modifier
                                .width(100.dp),
                            color = if (game) CRAppTheme.colorScheme.textOnGameBackground else CRAppTheme.colorScheme.textOnBackground
                        )
                    }

                    Text(
                        text = when (selectedTabIndex) {
                            0 -> "${personalProfile.fname} ${personalProfile.lname}, ${personalProfile.age}"
                            1 -> "${alternateProfile.fname}, ${alternateProfile.age}"
                            else -> "Nothing Selected"
                        },
                        style = CRAppTheme.typography.headingLarge,
                        color = if (game)
                            Color.White
                        else
                            when (selectedTabIndex){
                                0 -> Color.Black
                                1 -> Color.White
                                else -> Color.Black
                            },
                        modifier = Modifier
                            .then (
                                if (expand) {
                                    Modifier.padding(top = 10.dp, bottom = 10.dp)
                                } else { Modifier
                                }
                            )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier
                            .fillMaxWidth()
                    ){
                        Text(
                            text = when (selectedTabIndex) {
                                0 -> personalProfile.gender
                                1 -> alternateProfile.gender
                                else -> "Nothing Selected"
                            },
                            style = CRAppTheme.typography.infoLarge,
                            color = if (game)
                                Color.White
                            else
                                when (selectedTabIndex){
                                    0 -> Color.Black
                                    1 -> Color.White
                                    else -> Color.Black
                                },
                            modifier = Modifier
                                .then(
                                    if (expand) {
                                        Modifier.padding(top = 10.dp, bottom = 10.dp)
                                    } else {
                                        Modifier
                                    }
                                )
                                .weight(1f)

                        )
                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            when (selectedTabIndex){
                                0 -> {
                                    Text(
                                        "Friends",
                                        style = CRAppTheme.typography.bodyLarge,
                                        color = if (game)
                                            Color.White
                                        else
                                            when (selectedTabIndex){
                                                0 -> Color.Black
                                                1 -> Color.White
                                                else -> Color.Black
                                            },
                                    )
                                    Text(
                                        "1345",
                                        color = if (game)
                                            Color.White
                                        else
                                            when (selectedTabIndex){
                                                0 -> Color.Black
                                                1 -> Color.White
                                                else -> Color.Black
                                            },
                                    )
                                }
                                1 -> {
                                    Button(onClick = {
                                        val email = "dummyEmail"
                                        val password = "DummyPassword"
                                        navController.navigate("signupScreen2/${email}/${password}/true")
                                    }) {
                                        Text("Create Another Profile")
                                    }
                                }
                                else -> {}
                            }

                        }
                        if (self && !game) {
                            when (selectedTabIndex) {
                                0 -> {
                                    IconButton(onClick = {navController.navigate("friendsScreen")}) {
                                        Icon(
                                            Icons.Default.GroupAdd,
                                            contentDescription = null,
                                            tint = when (selectedTabIndex){
                                                0 -> Color.Black
                                                1 -> Color.White
                                                else -> Color.Black
                                            },

                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                                1 -> {}
                                else -> {}
                            }

                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier
                            .padding(top = 15.dp, bottom = 15.dp), color = if (game) CRAppTheme.colorScheme.textOnGameBackground else CRAppTheme.colorScheme.textOnBackground
                    )

                    Text(
                        text = "LOCATION",
                        style = CRAppTheme.typography.infoLarge,
                        color = if (game)
                            Color.White
                        else
                            when (selectedTabIndex){
                                0 -> Color.Black
                                1 -> Color.White
                                else -> Color.Black
                            },
                        modifier = Modifier
                            .padding(top = 25.dp, bottom = 15.dp)
                    )

                    Text(
                        text = when (selectedTabIndex) {
                            0 -> personalProfile.location
                            1 -> alternateProfile.location
                            else -> "Nothing Selected"
                        },
                        style = CRAppTheme.typography.bodyLarge,
                        color = if (game)
                            Color.White
                        else
                            when (selectedTabIndex){
                                0 -> Color.Black
                                1 -> Color.White
                                else -> Color.Black
                            },
                        modifier = Modifier
                            .padding(start = 25.dp, bottom = 30.dp)
                    )

                    Text(
                        text = "ABOUT",
                        style = CRAppTheme.typography.infoLarge,
                        color = if (game)
                            Color.White
                        else
                            when (selectedTabIndex){
                                0 -> Color.Black
                                1 -> Color.White
                                else -> Color.Black
                            },
                        modifier = Modifier
                            .padding(top = 15.dp, bottom = 15.dp)
                    )

                    Text(
                        text = when (selectedTabIndex) {
                            0 -> personalProfile.about
                            1 -> alternateProfile.about
                            else -> "Nothing Selected"
                        },
                        style = CRAppTheme.typography.bodyLarge,
                        color = if (game)
                            Color.White
                        else
                            when (selectedTabIndex){
                                0 -> Color.Black
                                1 -> Color.White
                                else -> Color.Black
                            },
                        modifier = Modifier
                            .padding(start = 15.dp, bottom = 30.dp)
                    )

                    if (!self){
                        Text(
                            text = "NOTES",
                            style = CRAppTheme.typography.infoLarge,
                            color = if (game) CRAppTheme.colorScheme.textOnGameBackground else CRAppTheme.colorScheme.textOnBackground,
                            modifier = Modifier
                                .padding(top = 15.dp, bottom = 15.dp)
                        )
                        TextField(
                            value = notes,
                            onValueChange = {notes = it},
                            textStyle = CRAppTheme.typography.bodyLarge,
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 15.dp, bottom = 30.dp)
                                .border(1.dp, Color.Black)

                        )
                    }


                }

            }
        }
    )
}

