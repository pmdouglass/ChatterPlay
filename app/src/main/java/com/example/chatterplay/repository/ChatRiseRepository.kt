package com.example.chatterplay.repository

import com.example.chatterplay.data_class.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatRiseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val CRGameRoomsCollection = firestore.collection("ChatriseRooms")
    val alt = "Alternate"
    val chatrise = "ChatRise"
    val users = "Users"
    val game = "Games"

    suspend fun getCRRoomId(userId: String): String?{
        val querySnapshot = CRGameRoomsCollection
            .whereArrayContains("members", userId)
            .get().await()
        return querySnapshot.documents.firstOrNull()?.id
    }
    suspend fun getUserProfile(CRRoomId: String, userId: String): UserProfile?{
        val snapshot = CRGameRoomsCollection
            .document(CRRoomId)
            .collection(users)
            .document(userId)
            .get().await()
        return snapshot.toObject(UserProfile::class.java)
    }


}