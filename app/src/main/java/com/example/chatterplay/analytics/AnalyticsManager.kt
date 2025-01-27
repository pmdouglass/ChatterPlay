package com.example.chatterplay.analytics

import android.content.Context
import android.os.Bundle
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")
class AnalyticsManager private constructor(context: Context) {

    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    private val dataStore = context.applicationContext.dataStore

    companion object {
        @Volatile
        private var INSTANCE: AnalyticsManager? = null

        fun getInstance(context: Context): AnalyticsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AnalyticsManager(context).also { INSTANCE = it }
            }
        }
    }

    // Public method to enable or disable analytics collection
    fun setAnalyticsCollectionEnabled(isenabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(isenabled)
    }

    suspend fun logEvent(eventName: String, params: Bundle) {
        val isAnalyticsEnabled = dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.ANALYTICS_ENABLED] ?: true
            }
            .first()

        if (isAnalyticsEnabled) {
            firebaseAnalytics.logEvent(eventName, params)
        }
    }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANALYTICS_ENABLED] = enabled
        }
    }

    private object PreferencesKeys {
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
    }
}
