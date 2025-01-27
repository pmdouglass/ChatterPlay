package com.example.chatterplay.view_model

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.example.chatterplay.data_class.AppDatabase

class LocalViewModel: ViewModel() {

    val context = LocalContext.current

    private val db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "chat_app_database"
    ).build()

/*
    private val repository = LocalRepository()

    fun saveUserProfile(profile: UserProfileStorage){
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun fetchUserProfile(userId: String, callback: (UserProfileStorage?) -> Unit){
        viewModelScope.launch {
            val profile = withContext(Dispatchers.IO){
                repository.getUserProfile(userId)
            }
            callback(profile)
        }
    }

 */
}