package com.example.chatterplay.screens

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.data_class.uriToByteArray
import com.example.chatterplay.seperate_composables.EditInfoDialog
import com.example.chatterplay.seperate_composables.SettingsInfoRow
import com.example.chatterplay.seperate_composables.rememberProfileState
import com.example.chatterplay.ui.theme.CRAppTheme
import com.example.chatterplay.view_model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditProfileScreen(
    viewModel: ChatViewModel = viewModel(),
    navController: NavController
) {

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val tabs = listOf("Personal", "Alternate")
    var selectedTabIndex by remember { mutableIntStateOf(0)}
    val scrollState = rememberScrollState()
    var showEditInfo by remember { mutableStateOf(false)}
    var showGameEditInfo by remember { mutableStateOf(false)}
    var showImageEdit by remember { mutableStateOf(false)}
    var showGameImageEdit by remember { mutableStateOf(false)}


    var titleEdit by remember{ mutableStateOf("")}
    val (personalProfile, alternateProfile) = rememberProfileState(viewModel = viewModel, userId = userId)




    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text("Profile")
                },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
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
                        SettingsInfoRow(Image = true, title = "Profile Picture", body = personalProfile.imageUrl, onClick = { showImageEdit = true; titleEdit = "Picture" })
                        SettingsInfoRow(Bio = true, title = "About", body = personalProfile.about, onClick = { showEditInfo = true; titleEdit = "About" })
                        SettingsInfoRow(Edit = true, title = "Gender", body = personalProfile.gender, onClick = { showEditInfo = true; titleEdit = "Gender" })
                        SettingsInfoRow(Edit = true, title = "Location", body = personalProfile.location , onClick = { showEditInfo = true; titleEdit = "Location" })
                        SettingsInfoRow(Edit = true, editClick = false, amount = 2, title = "Name", body = personalProfile.fname, secondBody = personalProfile.lname, onClick = { showEditInfo = true; titleEdit = "Name" })
                        SettingsInfoRow(Edit = true, editClick = false, title = "Date of Birth", body = "${personalProfile.dob.month}-${personalProfile.dob.day}-${personalProfile.dob.year}" , onClick = { showEditInfo = true; titleEdit = "Date of Birth" })
                        SettingsInfoRow(Edit = true, editClick = false, title = "Age", body = personalProfile.age , onClick = { showEditInfo = true; titleEdit = "Age" })
                    }
                    1 -> {
                        if (alternateProfile.fname.isBlank()){
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
                            SettingsInfoRow(game = true, Image = true, title = "Profile Picture", body = alternateProfile.imageUrl, onClick = { showGameImageEdit = true; titleEdit = "Picture" })
                            SettingsInfoRow(game = true, Bio = true, title = "About", body = alternateProfile.about, onClick = { showGameEditInfo = true; titleEdit = "About" })
                            SettingsInfoRow(game = true, Edit = true, amount = 1, title = "Name", body = alternateProfile.fname, onClick = { showGameEditInfo = true; titleEdit = "Name" })
                            SettingsInfoRow(game = true, Edit = true, title = "Gender", body = alternateProfile.gender, onClick = { showGameEditInfo = true; titleEdit = "Gender" })
                            SettingsInfoRow(game = true, Edit = true, editClick = false, title = "Date of Birth", body = "${alternateProfile.dob.month}-${alternateProfile.dob.day}-${alternateProfile.dob.year}", onClick = { showGameEditInfo = true; titleEdit = "Date of Birth" })
                            SettingsInfoRow(game = true, Edit = true, title = "Age", body = alternateProfile.age , onClick = { showGameEditInfo = true; titleEdit = "Age" })
                            SettingsInfoRow(game = true, Edit = true, title = "Location", body = alternateProfile.location , onClick = { showGameEditInfo = true; titleEdit = "Location" })
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
                    game = false,
                    onDismiss = { showEditInfo = false }
                )
            }
            if (showGameEditInfo){
                EditInfoDialog(
                    edit = titleEdit,
                    userProfile = alternateProfile,
                    userData = "",
                    game = true,
                    onDismiss = {showGameEditInfo = false}
                )
            }
            if (showImageEdit){
                EditImageDialog(
                    edit = titleEdit,
                    userProfile = personalProfile,
                    game = false,
                    onDismiss = {showImageEdit = false}
                )
            }
            if (showGameImageEdit){
                EditImageDialog(
                    edit = titleEdit,
                    userProfile = personalProfile,
                    game = true,
                    onDismiss = {showImageEdit = false}
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

    val scrollState = rememberScrollState()
    var showEditInfo by remember { mutableStateOf(false)}
    var titleEdit by remember{ mutableStateOf("")}
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val (personalProfile, alternateProfile) = rememberProfileState(viewModel = viewModel, userId = userId)


    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text("Personal Info")
                },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
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
                    game = false,
                    onDismiss = { showEditInfo = false },
                )
            }
        }
    )

}


@Composable
fun EditImageDialog(
    edit: String,
    userProfile: UserProfile,
    game: Boolean,
    onDismiss: () -> Unit,
    viewModel: ChatViewModel = viewModel()
){

    val context = LocalContext.current
    var ImageUri by remember { mutableStateOf<Uri?>(null) }
    var byteArray by remember { mutableStateOf<ByteArray?>(null)}

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        ImageUri = uri
        byteArray = uri?.uriToByteArray(context)
    }


    Dialog(onDismissRequest = {onDismiss()}) {
        Surface (
            shape = RoundedCornerShape(8.dp),
            color = CRAppTheme.colorScheme.background
        ){
            Column (
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(16.dp)
            ){
                Text(
                    "Edit $edit",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Box(
                    modifier = Modifier
                        .size(200.dp)
                ){
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Black, CircleShape)
                    ){
                        if (ImageUri != null){
                            // selected Image
                            Image(
                                painter = rememberAsyncImagePainter(ImageUri),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        } else {
                            // current profile Image
                            Image(
                                painter = rememberAsyncImagePainter(userProfile.imageUrl),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(75.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Black, CircleShape)
                            .align(Alignment.BottomEnd)
                            .background(CRAppTheme.colorScheme.background)
                    ){
                        IconButton(
                            onClick = {launcher.launch("image/*")},
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                        ){
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp)
                            )
                        }
                    }
                }
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ){
                    Button(
                        onClick = {onDismiss()},
                        modifier = Modifier
                            .width(100.dp)
                    ){
                        Text("Cancel")
                    }




                    Button(
                        onClick = {
                                  if (byteArray != null){
                                      viewModel.selectUploadAndGetImage(
                                          game = false,
                                          userId = userProfile.userId,
                                          byteArray = byteArray!!){ url, error ->
                                          if (url != null){
                                              val savedCopy = userProfile.copy(imageUrl = url)
                                              viewModel.saveUserProfile(userId = userProfile.userId, userProfile = savedCopy, game = game)
                                              onDismiss()
                                          }
                                      }
                                  }
                        },
                        modifier = Modifier
                            .width(100.dp)
                    ){
                        Text("OK")
                    }

                }

            }
        }
    }
}
