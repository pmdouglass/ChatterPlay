package com.example.chatterplay

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.chatterplay.ui.theme.CRAppTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CRAppTheme {
                Surface {
                    AppNavHost(navController = rememberNavController())
                }
            }
        }
    }
}
