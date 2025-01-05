package com.example.chatterplay.repository

import com.example.chatterplay.data_class.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChatRiseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val crGameRoomsCollection = firestore.collection("ChatriseRooms")
    private val users = "Users"

    suspend fun getUserProfile(crRoomId: String, userId: String): UserProfile?{
        val snapshot = crGameRoomsCollection
            .document(crRoomId)
            .collection(users)
            .document(userId)
            .get().await()
        return snapshot.toObject(UserProfile::class.java)
    }


}