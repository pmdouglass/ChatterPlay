package com.example.chatterplay.screens.subscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chatterplay.ui.theme.CRAppTheme

@Composable
fun TermsAndConditionsScreen(navController: NavController) {
    
    Column (
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(CRAppTheme.colorScheme.background)
    ){
        Text(
            text = "Terms and Conditions",
            style = CRAppTheme.typography.headingLarge
        )

        Text(
            "Go Back",
            color = Color.Blue,
            modifier = Modifier
                .padding(top = 50.dp)
                .clickable { navController.popBackStack()}
        )
        
    }
}

@Preview
@Composable
fun TestTerms() {
    CRAppTheme {
        Surface {
            TermsAndConditionsScreen(navController = rememberNavController())
        }
    }
}