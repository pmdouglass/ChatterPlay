package com.example.chatterplay.view_model

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatterplay.analytics.dataStore
import com.example.chatterplay.data_class.SupabaseClient.client
import com.example.chatterplay.data_class.userPreferences
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.FilterOperator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsViewModelFactory(
    private val application: Application,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T{
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(application, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SettingsViewModel(
    application: Application,
    private val sharedPreferences: SharedPreferences
) : AndroidViewModel(application) {
    private val dataStore = application.dataStore
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _startOnMainScreen = MutableStateFlow(false)
    val startOnMainScreen: StateFlow<Boolean> = _startOnMainScreen

    init {
        if (userId.isNotEmpty()){
            viewModelScope.launch {
                fetchStartScreen()
            }
        }else {
            Log.w("SettingsViewModel", "User is not logged in, skipping fetchStartScreen()")
        }
    }

    fun saveLocalStartScreenPref(startInGame: Boolean){
        viewModelScope.launch {
            try {
                sharedPreferences.edit()
                    .putBoolean("startScreen_$userId", startInGame)
                    .apply()
                Log.d("SettingsViewModel", "Successfully Saved start screen preference: $startInGame")
            }catch (e: Exception){
                Log.e("SettingsViewModel", "Error saving start screen preference", e)
            }
        }
    }
    fun loadLocalStartScreenPref(): Boolean? {
        return try {
            if (sharedPreferences.contains("startScreen_$userId")) {
                val prefValue = sharedPreferences.getBoolean("startScreen_$userId", false)
                Log.d("SettingsViewModel", "Loaded start screen preference: $prefValue")
                prefValue
            } else {
                Log.w("SettingsViewModel", "No saved start screen preference found for user: $userId")
                null
            }
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Error loading start screen preference", e)
            null
        }
    }

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



    fun fetchStartScreen(){
        viewModelScope.launch {
            try {
                val startScreen = loadLocalStartScreenPref()

                if (_startOnMainScreen == null){
                    Log.e("SettingsViewModel", "Error: _startOnMainScreen is null before assignment")
                    return@launch
                }

                if (startScreen != null){
                    _startOnMainScreen.value = startScreen
                }else {
                    Log.d("SettingsViewModel", "Fetching start screen preference from Suapbase")
                    val response = client.postgrest["userPreferences"]
                        .select(
                            filter = {
                                filter("userId", FilterOperator.EQ, userId)
                            }
                        )
                        .decodeSingleOrNull<userPreferences>()

                    if (response != null){
                        _startOnMainScreen.value = response.startOnMainScreen
                        saveLocalStartScreenPref(response.startOnMainScreen)
                        Log.d("SettingsViewModel", "startOnMainScreen set to ${response.startOnMainScreen}")
                    }else {
                        _startOnMainScreen.value = false
                        Log.d("SettingsViewModel", "startOnMainScreen default to false")
                    }
                }
            }catch (e: NullPointerException){
                Log.e("SettingsViewModel", "Error fetching preference: _startOnMainScreen is null", e)
            }catch (e: Exception){
                Log.e("SettingsViewModel", "Error fetching preference", e)
            }
        }
    }
    fun updateStartScreen(userId: String, startOnMain: Boolean){
        viewModelScope.launch {
            try {
                Log.d("SettingsViewModel", "Attempting to save startScreen to: $startOnMain")
                val existingRecords = client.postgrest["userPreferences"]
                    .select(
                        filter = {
                            filter("userId", FilterOperator.EQ, userId)
                        }
                    )
                    .decodeList<userPreferences>()

                if (existingRecords.isNotEmpty()){
                    Log.d("SettingsViewModel", "UserId found, updating startScreen..")
                    client.postgrest["userPreferences"]
                        .update(
                            value = mapOf("startOnMainScreen" to startOnMain),
                            filter = {
                                filter("userId", FilterOperator.EQ, userId)
                            }
                        )
                    Log.d("SettingsViewModel", "StartOnMainScreen successfully updated to $startOnMain")
                }else {
                    Log.d("SettingsViewModel", "UserId not found, inserting new record...")

                    client.postgrest["userPreferences"]
                        .insert(
                            value = mapOf(
                                "userId" to userId,
                                "startOnMainScreen" to startOnMain
                            )
                        )
                    Log.d("SettingsViewModel", "New record inserted with startOnMainScreen: $startOnMain")
                }

                _startOnMainScreen.value = startOnMain
                saveLocalStartScreenPref(startOnMain)

            }catch (e: Exception){
                Log.e("SettingsViewModel", "Error updating preference", e)
            }
        }
    }
}
