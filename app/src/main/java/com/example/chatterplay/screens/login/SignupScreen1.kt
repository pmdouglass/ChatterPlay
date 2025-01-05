package com.example.chatterplay.screens.login

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.chatterplay.ui.theme.CRAppTheme
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SignupScreen1(navController: NavController) {

    var showPopUp by remember { mutableStateOf(false)}
    var email by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("")}
    var confirmPassword by remember { mutableStateOf("")}
    var terms by remember { mutableStateOf(false)}
    val annotatedString = buildAnnotatedString {
        append("By checking here you agree to the ")

        pushStringAnnotation(tag = "TERMS", annotation = "terms_and_conditions")
        withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)){
            append("Terms and Conditions")
        }
        pop()
    }

    Column (
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(CRAppTheme.colorScheme.background)
            .padding(26.dp)
    ){
        Text(
            text = "Let's get Started.",
            style = CRAppTheme.typography.H3,
            modifier = Modifier.padding(top = 20.dp, bottom = 50.dp)
        )
        Text(
            "We need a little information from you. Let's get started with your account.",
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(bottom = 50.dp)
        )

        CustomField(
            label = "Email Address",
            value = email,
            onValueChange = {email = it},
            icon = Icons.Outlined.Email,
            keyboardtypeOption = KeyboardType.Email,
            inputValidator = { android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() },
            game = false
        )
        CustomField(
            label = "Password",
            value = password,
            onValueChange =  {password = it},
            icon = Icons.Outlined.Lock,
            keyboardtypeOption = KeyboardType.Password,
            inputValidator = {true},
            game = false
        )
        CustomField(
            label = "Confirm Password",
            value = confirmPassword,
            onValueChange = {confirmPassword = it},
            icon = Icons.Outlined.Lock,
            keyboardtypeOption = KeyboardType.Password,
            inputValidator = {confirmPassword == password},
            errorText = "Passwords must match!",
            game = false,
        )





        Row (
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
        ){
            Checkbox(
                checked = terms,
                onCheckedChange = {terms = it},
                colors = CheckboxDefaults.colors(
                    uncheckedColor = Color.Black,
                    checkedColor = Color.Black,
                    checkmarkColor = Color.White

                )
            )
            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                          annotatedString.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                              .firstOrNull()?.let {
                                  navController.navigate("termsAndConditions")
                              }
                },
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick =  {
                if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword && terms) {
                    navController.navigate("signupScreen2/${email}/${confirmPassword}/false")
                } else{
                    showPopUp = true
                }
                       },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 50.dp)) {
            Text(
                "Next",
                style = CRAppTheme.typography.titleMedium
            )
        }
        Text(
            "Maybe Later",
            color = Color.Blue,
            modifier = Modifier
                //.padding(top = 10.dp, bottom = 50.dp)
                .clickable { navController.navigate("loginScreen") }
        )

        if (showPopUp) {
            SimplePopupScreen(
                text = "You must fill out all the fields",
                showPopup = false,
                onDismissRequest = {showPopUp = false}
            )
        }

    }



}

@Composable
fun SimplePopupScreen(
    text: String = "",
    textfield: Boolean = false,
    showPopup: Boolean,
    onDismissRequest: (String) -> Unit
) {
    var input by remember { mutableStateOf("")}

    if (showPopup) {
        Dialog(onDismissRequest = { onDismissRequest("") }) {
            // Popup content
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Set the width of the popup
                    .wrapContentHeight()
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (text.isNotBlank()){
                        Text(
                            text = text,
                            style = CRAppTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (textfield){
                        TextField(
                            value = input,
                            onValueChange = {input = it}
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onDismissRequest(input) }) {
                        Text(text = "OK")
                    }
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector? = null,
    placeholder: String = "",
    keyboardtypeOption: KeyboardType,
    inputValidator: (String) -> Boolean,
    errorText: String = "Invalid input",
    settings: Boolean = false,
    capitalizefirstLetter: Boolean = false,
    capitalizeAllLetters: Boolean = false,
    game: Boolean = false
) {
    var isFieldValid by remember { mutableStateOf(true) } // Validate input based on provided logic
    var passwordVisible by remember { mutableStateOf(false) } // Password visibility toggle

    val dateDialogState = rememberMaterialDialogState()

    Column(modifier = modifier) {
        // Label and Validation Icon Row
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                color = if (game) Color.White else Color.Black,
                modifier = Modifier
                    .weight(1f)
                    ,
                style = CRAppTheme.typography.H0
            )
            if (value.isNotEmpty() && isFieldValid) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Green
                )
            }
        }

        // TextField
        TextField(
            value = value,
            onValueChange = { input ->
                            val formattedInput = when (keyboardtypeOption) {
                                KeyboardType.Number -> {
                                    if (input.all { it.isDigit()}) {
                                        input
                                    } else {
                                        value
                                    }
                                }
                                else -> {
                                    when {
                                        capitalizefirstLetter -> {
                                            input.replaceFirstChar {
                                                if (it.isLowerCase()) it.titlecase() else it.toString()
                                            }
                                        }
                                        capitalizeAllLetters -> {
                                            input.uppercase()
                                        }
                                        else -> input
                                    }
                                }
                            }
                onValueChange(formattedInput)
                isFieldValid = inputValidator(formattedInput)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = if (settings) Color.Transparent else Color.White,
                unfocusedContainerColor = if (settings) Color.Transparent else Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            leadingIcon = {
                icon?.let {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier
                            .then(if (icon == Icons.Outlined.DateRange) Modifier.clickable { dateDialogState.show() } else Modifier)
                    )
                }
            },
            trailingIcon = {
                if (keyboardtypeOption == KeyboardType.Password) {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            },
            placeholder = {
                Text(text = placeholder)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (keyboardtypeOption == KeyboardType.Password && passwordVisible) KeyboardType.Text else keyboardtypeOption
            ),
            visualTransformation = if (keyboardtypeOption == KeyboardType.Password && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            isError = !isFieldValid, // Highlight field if invalid
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(
                    2.dp,
                    when {
                        settings -> Color.LightGray
                        !isFieldValid -> Color.Red
                        else -> Color.Black
                    },
                    RoundedCornerShape(8.dp)
                )
                .clip(RoundedCornerShape(8.dp))
        )

        // Error Message
        if (!isFieldValid) {
            Text(
                text = errorText,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        MaterialDialog (
            dialogState = dateDialogState,
            buttons = {
                positiveButton(text = "OK"){

                }
                negativeButton(text = "Cancel") {

                }
            }
        ){
            datepicker(
                initialDate = LocalDate.now(),
                title = "Pick a date",
            ){
                val formattedDateStamp = DateTimeFormatter.ofPattern("MMM dd yyyy").format(it)
                onValueChange(formattedDateStamp)
            }
        }
    }
}



