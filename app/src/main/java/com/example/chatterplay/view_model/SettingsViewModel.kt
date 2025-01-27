package com.example.chatterplay.view_model

import android.app.Application
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import com.example.chatterplay.dataStore
import kotlinx.coroutines.flow.map


class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.dataStore

    // Flow to observe the analytics setting
    val isAnalyticsEnabled = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ANALYTICS_ENABLED] ?: true // Default value: true
        }

    // Function to update the analytics setting
    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANALYTICS_ENABLED] = enabled
            Log.d("SettingsViewModel", "Analytics enabled: $enabled")
        }
    }

    private object PreferencesKeys {
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
    }
}
