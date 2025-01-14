package com.example.chatterplay.repository

import android.util.Log
import com.example.chatterplay.data_class.GameData
import com.example.chatterplay.data_class.Questions
import com.example.chatterplay.data_class.SupabaseClient
import com.example.chatterplay.data_class.Title
import com.example.chatterplay.data_class.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.FilterOperator
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString


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
    suspend fun getAllUsersInRoom(crRoomId: String) =
        crGameRoomsCollection.document(crRoomId).collection(users).get().await()!!
    suspend fun getAllRankingsInRoom(crRoomId: String) =
        crGameRoomsCollection.document(crRoomId).collection("Rankings").get().await()!!
    fun addBonusPoint(crRoomId: String, userId: String, bonus: Int){
        val rankingRef = crGameRoomsCollection
            .document(crRoomId)
            .collection("Rankings")
            .document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(rankingRef)
            val currentPoints = snapshot.getLong("totalPoints")?.toInt() ?: 0
            transaction.update(rankingRef, "totalPoints", currentPoints + bonus)
        }
    }
    suspend fun fetchUserRankingStatus(crRoomId: String, userId: String): String? {
        return try {
            val documentSnapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)
                .document(userId)
                .get().await()
            documentSnapshot.getString("rankingStatus")
        }catch (e: Exception){
            null
        }
    }
    suspend fun fetchAllUsersGameStatus(crRoomId: String): List<Boolean>{
        return try {
            val documentSnapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)
                .get().await()

            val statuses = documentSnapshot.documents.mapNotNull { it.getBoolean("gameStatus") }
            Log.d("Repository", "Fetched game statuses: $statuses")
            statuses
        }catch (e: Exception){
            Log.d("Repository", "Error fetching users game status ${e.message}")
            emptyList()
        }
    }
    fun updateUserRankingStatus(crRoomId: String, userId: String, updatedStatus: String){
        val data = mapOf(
            "rankingStatus" to updatedStatus
        )
        crGameRoomsCollection
            .document(crRoomId)
            .collection(users)
            .document(userId)
            .set(data, SetOptions.merge())
    }

    suspend fun resetRanking(crRoomId: String){
        val rankingRef = crGameRoomsCollection
            .document(crRoomId)
            .collection("Rankings")
        try {
            val snapshot = rankingRef.get().await()

            firestore.runTransaction { transaction ->
                if (snapshot.isEmpty){

                }else {
                    snapshot.documents.forEach { document ->
                        val memberId = document.id
                        val memberRef = rankingRef.document(memberId)

                        transaction.update(memberRef, mapOf(
                            "totalPoints" to 0,
                            "votes" to emptyMap<String, Any>()
                        ))
                    }
                }

            }
        }catch (e: Exception){
            Log.d("Repository", "Failed to reset Rank")
        }

    }

    fun saveRanking(crRoomId: String, memberId: String, userId: String, newPoints: Int) {
        Log.d("Repository", "Saving ranking system")
        val rankingRef = crGameRoomsCollection
            .document(crRoomId)
            .collection("Rankings")
            .document(memberId)
        Log.d("Repository", "roomRef = $rankingRef")

        firestore.runTransaction { transaction ->
            Log.d("Repository", "Running Transaction")

            val snapshot = transaction.get(rankingRef)

            if (!snapshot.exists()) {
                // Document does not exist, create it with initial values
                val initialData = mapOf(
                    "totalPoints" to newPoints,
                    "bonusPoints" to 0,
                    "votes" to mapOf(userId to mapOf("pointsGiven" to newPoints))
                )
                transaction.set(rankingRef, initialData)
            } else {
                // Document exists, update it
                val currentBonusPoints = snapshot.getLong("bonusPoints")?.toInt() ?: 0
                val totalPoints = snapshot.getLong("totalPoints")?.toInt() ?: 0
                val votes = snapshot.get("votes") as? Map<String, Map<String, Any>> ?: emptyMap()

                val previousPoints = (votes[userId]?.get("pointsGiven") as? Long)?.toInt() ?: 0
                val updatedTotalPoints = totalPoints - previousPoints + newPoints + currentBonusPoints

                val updatedVotes = votes.toMutableMap()
                updatedVotes[userId] = mapOf("pointsGiven" to newPoints)

                transaction.update(rankingRef, mapOf(
                    "memberId" to memberId,
                    "totalPoints" to updatedTotalPoints,
                    "votes" to updatedVotes
                ))
            }
        }.addOnSuccessListener {
            Log.d("Repository", "Transaction success: Ranking updated successfully.")
        }.addOnFailureListener { e ->
            Log.e("Repository", "Transaction failure: Failed to update ranking", e)
        }
    }
    fun updatePointsBasedOnVote(crRoomId: String, userId: String, hasVoted: Boolean) {
        val rankingRef = crGameRoomsCollection
            .document(crRoomId)
            .collection("Rankings")
            .document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(rankingRef)
            val currentPoints = snapshot.getLong("totalPoints")?.toInt() ?: 0
            val updatedPoints = if (hasVoted){
                currentPoints + 10
            } else {
                currentPoints - 10
            }
            transaction.update(rankingRef, "totalPoints", updatedPoints)
        }.addOnSuccessListener {
            Log.d("Repository", "Deducted 10 points from $userId successfully.")
        }.addOnFailureListener { e ->
            Log.e("Repository", "Failed to deduct points from $userId", e)
        }
    }

    //                   supabase
    suspend fun fetchRandomGameInfo(): Title?{
        return try {
            // fetch all title's
            val response = SupabaseClient.client.postgrest["title"]
                .select()
                .decodeList<Title>()

            // extract id's to list
            //val ids = response.map { it.id }

            if (response.isNotEmpty()) response.random() else null
        }catch (e: Exception){
            Log.d("ViewModel", "Failed to get random titleId ${e.message}")
            null
        }
    }
    suspend fun addOrUpdateGame(crRoomId: String, gameName: String, userId: String? = null, gamePlayed: Boolean? = null, doneStatus: Boolean? = null): Boolean{
        return try {
            val gameDocRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("Games")
                .document(gameName)

            // check if document exists
            val gameSnapshot = gameDocRef.get().await()
            if (gameSnapshot.exists()){
                val updates = mutableMapOf<String, Any>()
                gamePlayed?.let { updates["gamePlayed"] = it }
                doneStatus?.let { updates["doneStatus"] = it }

                if (updates.isNotEmpty()){
                    gameDocRef.update(updates).await()
                    Log.d("Repository", "Game updated successfully")
                }else {
                    Log.d("Repository", "no fields to update")
                }
            } else {
                // if not create one
                val gameData = GameData(
                    gameName = gameName,
                    gamePlayed = false,
                    doneStatus = false
                )

                gameDocRef.set(gameData).await()

                // update UserProfile
                if (userId != null){
                    val collection = crGameRoomsCollection
                        .document(crRoomId)
                        .collection("Users")
                        .document(userId)

                    val userSnapshot = collection.get().await()
                    if (userSnapshot.exists()){
                        collection.update("gameName", gameName).await()
                        Log.d("Repository", "user profile updated with gamename: $gameName")
                    }else {
                        collection.set(mapOf("gameName" to gameName)).await()
                        Log.d("Repository", "user profile created and gameName: $gameName added")
                    }
                }
            }

            true
        }catch (e: Exception){
            Log.d("Repository", "Failed to add game ${e.message}")
            false
        }
    }
    suspend fun addGameNameToAllUserProfile(crRoomId: String, members: List<String>, gameInfo: Title){
        try {
            val collection = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)

            // Serialize the Title object to JSON
            val gameInfoJson = kotlinx.serialization.json.Json.encodeToString(gameInfo)

            members.forEach { userId ->
                val userDocRef = collection.document(userId)
                val usersSnapshot = userDocRef.get().await()

                if (usersSnapshot.exists()){
                    userDocRef.update(
                        mapOf(
                            "gameInfo" to gameInfoJson,
                            "gameStatus" to false
                        )
                    ).await()

                    Log.d("Repository", "UserProfile updated with gameName: $gameInfoJson")
                }
            }

        }catch (e: Exception){
            Log.d("Repository", "Failed to add gamename to userProfile ${e.message}")
        }
    }
    suspend fun updateGameStatus(crRoomId: String, userId: String, questionsComplete: Boolean): Boolean{
        return try {
            val collection = crGameRoomsCollection
                .document(crRoomId)
                .collection("Users")
                .document(userId)

            collection.set(mapOf("gameStatus" to questionsComplete), SetOptions.merge()).await()
            Log.d("Repository", "user profile updated/created with gameStatus: $questionsComplete")
            true
        }catch (e: Exception){
            Log.d("Repository", "Error updating game status ${e.message}")
            false
        }
    }
    /*suspend fun checkGameStatus(crRoomId: String){
        try {
            val snpashot = crGameRoomsCollection
                .document(crRoomId)
                .collection("")
        }catch (e: Exception){
            Log.d("Repository", "Error checking game status ${e.message}")
        }
    }*/

    suspend fun getAllQuestions(titleId: Int): List<Questions>{
        val response = SupabaseClient.client.postgrest["questions"]
            .select(
                filter = {
                    filter("titleId", FilterOperator.EQ, titleId)
                }
            )
        return response.decodeList()
    }



}