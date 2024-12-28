package com.example.chatterplay.screens.login

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chatterplay.seperate_composables.DateDropDown
import com.example.chatterplay.ui.theme.CRAppTheme
import java.time.LocalDate


@RequiresApi(Build.VERSION_CODES.O)
fun calculateAgeToDate(ageInt: Int): String{
    val today = LocalDate.now()
    val year = today.minusYears(ageInt.toLong()).year

    return year.toString()
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateBDtoAge(yearDate: String): Int {
    val today = LocalDate.now()
    return today.year - yearDate.toInt()

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SignupScreen2(email: String, password: String, navController: NavController, game: Boolean) {

    val space = 15
    val width = 150
    var showPopUp by remember { mutableStateOf(false)}
    var popupResults by remember { mutableStateOf("")}
    LocalDate.now()

    var fName by remember { mutableStateOf("")}
    var lName by remember { mutableStateOf("")}
    var month by remember { mutableStateOf("")}
    var day by remember { mutableStateOf("")}
    var year by remember { mutableStateOf("")}
    var ageTemp by remember { mutableStateOf("0")}
    var gender by remember { mutableStateOf("")}
    var location by remember { mutableStateOf("")}
    var manSelect by remember { mutableStateOf(false)}
    var womanSelect by remember { mutableStateOf(false)}
    var moreSelect by remember { mutableStateOf(false)}
    var moreOpen by remember { mutableStateOf(false)}





    val rdmLName = listOf("Wynterbane", "Thornshade", "Brackenthorn", "Frostwynd", "Shadowglen", "Ironvale", "Nightwhisper", "Stormhollow", "Emberforge", "Silverhaze", "Ashenbrook", "Glimmerstone", "Ravenshire", "Flintmoor", "Duskridge", "Hollowstride", "Blazewood", "Quicksilver", "Fogreach", "Briarstone")
    val rndMonth = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val rndDay = (1..29).map { it.toString() }
    val randomMonth = rndMonth.random()
    val randomDay = rndDay.random()
    val randomLName = rdmLName.random()




    Column (
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(if (!game) CRAppTheme.colorScheme.background else CRAppTheme.colorScheme.gameBackground)
            .padding(26.dp)
    ){
        Text(
            text = "Let's get some Information",
            textAlign = TextAlign.Center,
            style = CRAppTheme.typography.H3,
            color = if(game) Color.White else Color.Black,
            modifier = Modifier.padding(top = 20.dp, bottom = space.dp)
        )
        Text(
            "We need a little more information from you",
            textAlign = TextAlign.Center,
            color = if(game) Color.White else Color.Black,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(bottom = space.dp)
        )

        CustomField(
            label = "First Name",
            value = fName,
            onValueChange = {fName = it},
            icon = Icons.Outlined.Person,
            keyboardtypeOption = KeyboardType.Text,
            inputValidator = {true},
            capitalizefirstLetter = true,
            game = game
        )
        if (!game){
            CustomField(
                label = "Last Name",
                value = lName,
                onValueChange = {lName = it},
                icon = Icons.Outlined.Person,
                keyboardtypeOption = KeyboardType.Text,
                inputValidator ={true},
                capitalizefirstLetter = true,
                game = false
            )
        }

        CustomField(
            label = "Location",
            value = location,
            onValueChange = {location = it},
            icon = Icons.Outlined.Place,
            placeholder = "State",
            keyboardtypeOption = KeyboardType.Text,
            inputValidator = {true},
            capitalizeAllLetters = true,
            game = game
        )

        if (game){
            DateDropDown(age = true, game = true){selected -> ageTemp = selected}
        } else {

            Text(
                "Date of Birth",
                style = CRAppTheme.typography.H0
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ){
                DateDropDown(month = true, game = false) { selectedOption -> month = selectedOption}
                DateDropDown(day = true, game = false) { selectedOption -> day = selectedOption}
                DateDropDown(year = true, game = false) { selectedOption -> year = selectedOption}
            }
        }


        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ){
            Text(
                "I Identify as",
                style = CRAppTheme.typography.H0,
                color = if(game) Color.White else Color.Black,
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )
            Text(
                "Other",
                style = CRAppTheme.typography.H0,
                color = if(game) Color.White else Color.Black,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clickable {
                        moreOpen = true
                    }
            )

        }
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ){
            Text(
                "Man",
                style = CRAppTheme.typography.headingLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(width.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .border(2.dp, Color.Black, RoundedCornerShape(25.dp))
                    .background(if (manSelect) CRAppTheme.colorScheme.primary else CRAppTheme.colorScheme.background)
                    .padding(8.dp)
                    .clickable {
                        manSelect = true
                        womanSelect = false
                        moreSelect = false
                        gender = "Man"
                    }
            )

            Text(
                "Woman",
                style = CRAppTheme.typography.headingLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(width.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .border(2.dp, Color.Black, RoundedCornerShape(25.dp))
                    .background(if (womanSelect) CRAppTheme.colorScheme.primary else CRAppTheme.colorScheme.background)
                    .padding(8.dp)
                    .clickable {
                        manSelect = false
                        womanSelect = true
                        moreSelect = false
                        gender = "Woman"
                    }
            )

        }
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            popupResults.ifBlank { "Custom Description" },
            style = CRAppTheme.typography.headingLarge,
            color = if (popupResults.isBlank()) Color.Gray else Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(310.dp)
                .clip(RoundedCornerShape(25.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(25.dp))
                .background(if (moreSelect) CRAppTheme.colorScheme.primary else CRAppTheme.colorScheme.background)
                .padding(8.dp)
                .clickable {
                    if (popupResults == "") {
                        moreOpen = true
                    } else {
                        manSelect = false
                        womanSelect = false
                        moreSelect = true
                        gender = popupResults
                    }
                }
        )


        Spacer(modifier = Modifier.weight(1f))

        Button(onClick =  {
            if (fName.isNotBlank() && location.isNotBlank() && gender.isNotBlank()){
                val ageInt = ageTemp.toIntOrNull()


                if (!game) {
                    if (year.isBlank() || month.isBlank() || day.isBlank()){
                        Log.d("Test Message", "year, month, day is blank")
                        showPopUp = true
                        return@Button
                    }
                    val age = calculateBDtoAge(year).toString()
                    navController.navigate("signupScreen3/${email}/${password}/${fName}/${lName}/${month}/${day}/${year}/${age}/${gender}/${location}/false")
                } else {







                    ageInt?.let {
                        val calYear = calculateAgeToDate(ageInt)
                        if (calYear.isBlank()){
                            Log.d("Test Message", "calYear is null or blank")
                            showPopUp = true
                            return@Button
                        }

                        val age = ageInt.toString()
                        lName = randomLName
                        month = randomMonth
                        day = randomDay
                        year = calYear


                        navController.navigate("signupScreen3/${email}/${password}/${fName}/${lName}/${month}/${day}/${year}/${age}/${gender}/${location}/true")
                    }

                }
            } else{
                showPopUp = true
            }
        },
            modifier = Modifier
                .padding(bottom = 50.dp)
                .fillMaxWidth()) {
            Text(
                "Next",
                style = CRAppTheme.typography.titleMedium
            )
        }
        Text(
            "Go Back",
            color = Color.Blue,
            modifier = Modifier
                .clickable { navController.popBackStack() }
        )

        if (showPopUp){
            SimplePopupScreen(
                text = "Please fill out all the fields",
                showPopup = false,
                onDismissRequest = { showPopUp = false }
            )
        }
        if (moreOpen){
            SimplePopupScreen(
                showPopup = false,
                textfield = true,
                onDismissRequest = {input ->
                    moreOpen = false
                    manSelect = false
                    womanSelect = false
                    moreSelect = input.isNotBlank()
                    popupResults = input
                    gender = input
                }
            )
        }


    }



}


