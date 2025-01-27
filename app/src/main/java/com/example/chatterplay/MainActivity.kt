package com.example.chatterplay

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.analytics.UserPresenceObserver
import com.example.chatterplay.analytics.dataStore
import com.example.chatterplay.navigation.AppNavHost
import com.example.chatterplay.ui.theme.CRAppTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var currentScreenName: String = "unknown_screen"

    fun setCurrentScreen(name: String){
        currentScreenName = name
    }

    fun getCurrentScreen(): String {
        return currentScreenName
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Retrieve user settings before loading app.
        lifecycleScope.launch {
            val isAnalyticsEnabled = getAnalyticsEnabled()
            // Apply the preferences
            AnalyticsManager.getInstance(this@MainActivity)
                .setAnalyticsCollectionEnabled(isAnalyticsEnabled)
        }
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        lifecycle.addObserver(UserPresenceObserver(this, userId))

        logRetentionEventAfterLogin(this, userId)

        setContent {
            CRAppTheme {
                Surface {
                    AppNavHost(navController = rememberNavController())
                }
            }
        }
    }

    // Helper function to get the analytics setting from DataStore
    private suspend fun getAnalyticsEnabled(): Boolean {
        return try {
            val dataStore = this@MainActivity.application.dataStore
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.ANALYTICS_ENABLED] ?: true
            }.first()
        } catch (e: Exception) {
            e.printStackTrace()
            true // Default to true if something goes wrong
        }
    }


    // PreferencesKeys object for DataStore key access
    private object PreferencesKeys {
        val ANALYTICS_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("analytics_enabled")
    }

    // Logs the retention event after login or at app launch
    private fun logRetentionEventAfterLogin(context: Context, userId: String) {
        lifecycleScope.launch {
            val retentionDay = calculateRetentionDay(context)

            val params = Bundle().apply {
                putString("user_id", userId)
                putString("retention_day", retentionDay.toString())
            }
            AnalyticsManager.getInstance(context).logEvent("retention_day", params)
        }
    }

    // Calculates the retention day based on the first launch date
    private fun calculateRetentionDay(context: Context): Int {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val firstLaunchDate = prefs.getLong("first_launch_date", System.currentTimeMillis())

        // Save the first launch date if it's not already set
        if (!prefs.contains("first_launch_date")) {
            prefs.edit().putLong("first_launch_date", firstLaunchDate).apply()
        }

        val currentDate = System.currentTimeMillis()
        return ((currentDate - firstLaunchDate) / (1000 * 60 * 60 * 24)).toInt()
    }

    // Dummy function to get the current user's ID
    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

}

