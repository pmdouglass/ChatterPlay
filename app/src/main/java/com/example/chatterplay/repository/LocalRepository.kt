package com.example.chatterplay.repository

import com.example.chatterplay.data_class.UserProfileDao
import com.example.chatterplay.data_class.UserProfileStorage

class LocalRepository(
    private val userProfileDao: UserProfileDao
) {

    suspend fun saveUserProfile(profile: UserProfileStorage){
        userProfileDao.insertUserProfile(profile)
    }

    suspend fun getUserProfile(userId: String): UserProfileStorage? {
        return userProfileDao.getUserProfile(userId)
    }
}