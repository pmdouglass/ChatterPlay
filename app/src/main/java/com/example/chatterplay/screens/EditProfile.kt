package com.example.chatterplay.screens

import android.os.Build
import android.widget.HorizontalScrollView
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ImageAspectRatio
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chatterplay.R
import com.example.chatterplay.seperate_composables.EditInfoDialog
import com.example.chatterplay.seperate_composables.SettingsInfoRow
import com.example.chatterplay.seperate_composables.rememberProfileState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatterplay.seperate_composables.EditFirstNameDialog
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatViewModel


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditProfileScreen(
    viewModel: ChatViewModel = viewModel(),
    navController: NavController
) {

    //val usersInfo by viewModel.userProfile.collectAsState()
    //val auth = FirebaseAuth.getInstance()
    //val currentUser = auth.currentUser

    val tabs = listOf("Personal", "Alternate")
    var selectedTabIndex by remember { mutableStateOf(0)}
    val scrollState = rememberScrollState()
    var showEditInfo by remember { mutableStateOf(false)}
    var showEdit by remember { mutableStateOf(false)}

    var titleEdit by remember{ mutableStateOf("")}
    val (personalProfile, alternateProfile) = rememberProfileState(viewModel)




    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text("Profile")
                },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier
                                .size(35.dp)
                        )
                    }
                },
                actions = {
                          IconButton( onClick = {navController.navigate("roomSelect")}){
                              Icon(
                                  Icons.Default.ImageAspectRatio,
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
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .background(
                        when (selectedTabIndex) {
                            0 -> CRAppTheme.colorScheme.background
                            1 -> CRAppTheme.colorScheme.gameBackground
                            else -> Color.White
                        }
                    )
                    .padding(paddingValues)
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = CRAppTheme.colorScheme.background,
                    contentColor = Color.Black
                ) {
                    tabs.forEachIndexed {index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {selectedTabIndex = index},
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    }
                }
                when (selectedTabIndex) {
                    0 -> {
                        SettingsInfoRow(title = "Profile Picture", body = "", Image = true, imagePic = R.drawable.cool_neon, onClick = { showEditInfo = true; titleEdit = "Picture" })
                        SettingsInfoRow(Edit = true, amount = 2, title = "Name", body = personalProfile.fname, secondBody = personalProfile.lname, onClick = { showEditInfo = true; titleEdit = "Name" })
                        SettingsInfoRow(Bio = true, title = "About", body = personalProfile.about, onClick = { showEditInfo = true; titleEdit = "About" })
                        SettingsInfoRow(Edit = true, title = "Gender", body = personalProfile.gender, onClick = { showEditInfo = true; titleEdit = "Gender" })
                        SettingsInfoRow(Edit = true, title = "Date of Birth", body = "${personalProfile.dob.month}-${personalProfile.dob.day}-${personalProfile.dob.year}" , onClick = { showEditInfo = true; titleEdit = "Date of Birth" })
                        SettingsInfoRow(Edit = true, editClick = false, title = "Age", body = personalProfile.age , onClick = { showEditInfo = true; titleEdit = "Age" })
                        SettingsInfoRow(Edit = true, title = "Location", body = personalProfile.location , onClick = { showEditInfo = true; titleEdit = "Location" })
                    }
                    1 -> {
                        if (alternateProfile.fname.isNullOrBlank()){
                            Column (
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(CRAppTheme.colorScheme.gameBackground)
                                    .padding(26.dp)
                            ) {
                                Text(
                                    "Unleash Your Other Side!",
                                    style = CRAppTheme.typography.H3,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(top = 20.dp, bottom = 50.dp)
                                )
                                Text(
                                    "Ready to step into a whole new persona? Creating an alternate profile is your chance to play the game like never before! Whether you want to be a daring adventurer, a clever strategist, or someone totally unexpected, the choice is yours.\n" +
                                            "Don’t worry—your personal profile is safe and sound. When the game is over, your friends will see the real you. But during the game, they'll only see the identity you choose to play as. Keep them guessing, surprise them, or just have a blast being someone else.",
                                    textAlign = TextAlign.Center,
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(bottom = 50.dp)
                                )
                                Button(onClick = {
                                    val email = "dummyEmail"
                                    val password = "DummyPassword"
                                    navController.navigate("signupScreen2/${email}/${password}/true")
                                }) {
                                    Text("Create your Secret Identity")
                                }

                            }
                        } else {
                            SettingsInfoRow(game = true, title = "Profile Picture", body = "", Image = true, imagePic = R.drawable.cool_neon, onClick = { showEditInfo = true; titleEdit = "Picture" })
                            SettingsInfoRow(game = true, Edit = true, amount = 1, title = "Name", body = alternateProfile.fname, onClick = { showEditInfo = true; titleEdit = "Name" })
                            SettingsInfoRow(game = true, Bio = true, title = "About", body = alternateProfile.about, onClick = { showEditInfo = true; titleEdit = "About" })
                            SettingsInfoRow(game = true, Edit = true, title = "Gender", body = alternateProfile.gender, onClick = { showEditInfo = true; titleEdit = "Gender" })
                            SettingsInfoRow(game = true, Edit = true, editClick = false, title = "Date of Birth", body = "${alternateProfile.dob.month}-${alternateProfile.dob.day}-${alternateProfile.dob.year}", onClick = { showEditInfo = true; titleEdit = "Date of Birth" })
                            SettingsInfoRow(game = true, Edit = true, title = "Age", body = alternateProfile.age , onClick = { showEditInfo = true; titleEdit = "Age" })
                            SettingsInfoRow(game = true, Edit = true, title = "Location", body = alternateProfile.location , onClick = { showEditInfo = true; titleEdit = "Location" })
                        }
                    }
                    else -> {
                        Text("Nothing Selected")
                    }
                }


            }
            if (showEditInfo){
                EditInfoDialog(
                    edit = titleEdit,
                    userProfile = personalProfile,
                    userData = "",
                    onDismiss = { showEditInfo = false }
                )
            }
            if (showEdit){
                EditFirstNameDialog(
                    userProfile = personalProfile,
                    onDismiss = {showEdit = false}
                )
            }
        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditPersonalInfo(
    viewModel: ChatViewModel = viewModel(),
    navController: NavController
) {

    //val usersInfo by viewModel.userProfile.collectAsState()
    //val auth = FirebaseAuth.getInstance()
    //val currentUser = auth.currentUser

    var examp by remember { mutableStateOf("")}
    val scrollState = rememberScrollState()
    var showEditInfo by remember { mutableStateOf(false)}

    var email by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("")}
    var confirmPassword by remember { mutableStateOf("")}
    var titleEdit by remember{ mutableStateOf("")}
    val (personalProfile, alternateProfile) = rememberProfileState(viewModel)





    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text("Personal Info")
                },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(
                            Icons.Default.ArrowBack,
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
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .background(CRAppTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                SettingsInfoRow(Edit = true, title = "Email", body = "doug@gmail.com", onClick = { showEditInfo = true; titleEdit = "Email" })
                SettingsInfoRow(Edit = true, title = "Update Password", body = "*********", onClick = { showEditInfo = true; titleEdit = "Password" })

            }
            if (showEditInfo){
                EditInfoDialog(
                    edit = titleEdit,
                    userProfile = personalProfile,
                    userData = "",
                    onDismiss = { showEditInfo = false },
                )
            }
        }
    )

}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun testeditscreen() {
    CRAppTheme {
        Surface {
            EditProfileScreen(navController = rememberNavController())
        }
    }
}