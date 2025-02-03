package com.example.chatterplay.repository

import android.content.SharedPreferences
import android.util.Log
import com.example.chatterplay.data_class.ChatRoom
import com.example.chatterplay.data_class.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RoomCreateRepository(private val sharedPreferences: SharedPreferences) {

    private val firestore = FirebaseFirestore.getInstance()
    // Simulated database (key: userId, value: UserProfile)
    private val userCollection = firestore.collection("Users")
    private val crGameRoomsCollection = firestore.collection("ChatriseRooms")



    fun saveUserLocalcrRoomId(userId: String, crRoomId: String){
        sharedPreferences.edit().putString("crRoomId_$userId", crRoomId).apply()
        Log.d("ChatRiseRepository", "crRoomId saving to $userId")
    }
    fun loadUserLocalcrRoomId(userId: String): String? {
        val crRoomId = sharedPreferences.getString("crRoomId_$userId", null)
        Log.d("ChatRiseRepository", "crRoomId loading as $userId")
        return crRoomId
    }


    // Fetch user profile by userId
    suspend fun fetchUserProfile(userId: String): String? {
        return try {
            val documentSnapshot = userCollection.document(userId).get().await()
            documentSnapshot.getString("pending")
        }catch (e: Exception){
            null
        }
    }

    // Update the "pending" state of a user profile
    suspend fun updateUserPendingState(userId: String, newState: String): Boolean {
        return try {
            userCollection.document(userId).update("pending", newState).await()
            true
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }
    suspend fun updateUsersGameRoomId(userIds: List<String>, roomId: String): Boolean{
        return try {
            val batch = firestore.batch()
            userIds.forEach { userId ->
                val userDocRef = userCollection.document(userId)
                batch.update(userDocRef, "gameRoomId", roomId)
            }
            batch.commit().await()
            true
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

    // Monitor users in "Pending" state
    suspend fun fetchUsersPendingState(): List<String>{
        return try {
            val querySnapshot = userCollection.whereEqualTo("pending", "Pending").get().await()
            querySnapshot.documents.mapNotNull { it.id }
        }catch (e: Exception){
            emptyList()
        }
    }

    // update multiple users to "InGame" state
    suspend fun updateUsersToInGame(userIds: List<String>): Boolean{
        return try {
            val batch = firestore.batch()
            userIds.forEach { userId ->
                val docRef = userCollection.document(userId)
                batch.update(docRef, "pending", "InGame")
            }
            batch.commit().await()
            true
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

    suspend fun createCRSelectedProfileUsers(crRoomId: String, userIds: List<String>): Boolean{
        return try {
            val batch = firestore.batch()

            userIds.forEach{ userId ->
                // Step 1: Get the primary UserProfile
                val primaryProfileRef = userCollection.document(userId)
                val primaryProfileSnapshot = primaryProfileRef.get().await()
                val selectedProfile = primaryProfileSnapshot.getString("selectedProfile") ?: "self"

                // Step 2: Determine which UserProfile to use
                val userProfile = if (selectedProfile == "self"){
                    primaryProfileSnapshot.toObject(UserProfile::class.java) // Main Profile
                }else {
                    val alternateProfileRef = primaryProfileRef.collection("Alternate").document("ChatRise")
                    val alternateProfileSnapshot = alternateProfileRef.get().await()
                    alternateProfileSnapshot.toObject(UserProfile::class.java) // Alternate Profile
                }

                // Step 3: Add to batch operation
                if (userProfile != null){
                    val userDocRef = crGameRoomsCollection.document(crRoomId)
                        .collection("Users")
                        .document(userId)
                    batch.set(userDocRef, userProfile)
                }
            }

            // Step 4: Commit the batch
            batch.commit().await()
            true
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

    suspend fun createNewCRRoom(roomName: String, members: List<String>): Boolean{
        return try {
            // create room
            val newRoom = ChatRoom(
                roomId = crGameRoomsCollection.document().id,
                roomName = roomName,
                members = members,
                createdAt = Timestamp.now()
            )
            crGameRoomsCollection.document(newRoom.roomId).set(newRoom).await()

            true
        }catch (e: Exception){
            e.printStackTrace()
            false
        }

    }

    suspend fun fetchUsersSelectedProfileStatus(userId: String): String?{
        return try {
            val documentSnapshot = userCollection.document(userId).get().await()
            documentSnapshot.getString("selectedProfile")
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
    }
    suspend fun fetchcrRoomId(userId: String): String?{
        return try {
            val documentSnapshot = userCollection.document(userId).get().await()
            documentSnapshot.getString("gameRoomId")
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
    }
    suspend fun getcrRoomId(userId: String): String? {
        return try {
            val querySnapshot = crGameRoomsCollection
                .whereArrayContains("members", userId)
                .get().await()
            if (!querySnapshot.isEmpty) {
                querySnapshot.documents.first().id // Get the first room ID
            } else {
                null // No room found
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null in case of an exception
        }
    }


}

