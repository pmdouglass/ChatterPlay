package com.example.chatterplay

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.Surface
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.navigation.AppNavHost
import com.example.chatterplay.ui.theme.CRAppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Application.dataStore by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
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
        val dataStore = this@MainActivity.application.dataStore
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.ANALYTICS_ENABLED] ?: true
        }.first()
    }

    // PreferencesKeys object for DataStore key access
    private object PreferencesKeys {
        val ANALYTICS_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("analytics_enabled")
    }
}
