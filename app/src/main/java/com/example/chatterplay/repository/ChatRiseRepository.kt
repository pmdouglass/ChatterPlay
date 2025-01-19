package com.example.chatterplay.repository

import android.util.Log
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
        }.addOnSuccessListener {
            Log.d("Repository", "Bonus point added successfully")
        }.addOnFailureListener { e ->
            Log.e("Repository", "Failed to add bonus point: ${e.message}")
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
                if (!snapshot.isEmpty){
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
    suspend fun fetchRandomGameInfo(crRoomId: String): Title?{
        return try {
            // fetch all documents from "Games" in firestore
            val gamesSnapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection("Games")
                .get().await()

            // collect all games marked as "allDone"
            val completedGames = gamesSnapshot.documents
                .filter { it.getBoolean("allDone") == true }
                .map { it.id }
            Log.d("Repository", "Games Found $completedGames")

            // fetch all available games from supabase
            val response = SupabaseClient.client.postgrest["title"]
                .select()
                .decodeList<Title>()

            // allDone Games - title games
            val eligibleGames = response.filterNot { completedGames.contains(it.title) }
            Log.d("Repository", "Eligible games $eligibleGames")

            // return a random eligible game
            if (eligibleGames.isNotEmpty()) eligibleGames.random() else null
        }catch (e: Exception){
            Log.e("ViewModel", "Failed to fetch random gameInfo ${e.message}",e)
            null
        }
    }
    suspend fun addOrUpdateGame(
        crRoomId: String,
        gameName: String,
        hadAlert: Boolean? = null,
        allAnswered: Boolean? = null,
        allDone: Boolean? = null
    ): Boolean{
        return try {
            val gameDocRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("Games")
                .document(gameName)

            // check if document exists
            val gameSnapshot = gameDocRef.get().await()
            if (gameSnapshot.exists()){
                val updates = mutableMapOf<String, Any>()
                allAnswered?.let { updates["allAnswered"] = it }
                allDone?.let { updates["allDone"] = it }
                hadAlert?.let { updates["hadAlert"] = it }

                if (updates.isNotEmpty()){
                    gameDocRef.update(updates).await()
                    Log.d("Repository", "Game updated successfully")
                }else {
                    Log.d("Repository", "no fields to update")
                }
            } else {

                val gameData = mapOf(
                    "gameName" to gameName,
                    "hadAlert" to false,
                    "allAnswered" to false,
                    "allDone" to false
                )

                gameDocRef.set(gameData).await()
            }

            true
        }catch (e: Exception){
            Log.d("Repository", "Failed to add game ${e.message}")
            false
        }
    }
    suspend fun addGameNameToAllUserProfile(crRoomId: String, members: List<String>, gameInfo: Title){
        try {
            // Serialize the Title object to JSON
            val gameInfoJson = kotlinx.serialization.json.Json.encodeToString(gameInfo)
            val roomRef = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)


            firestore.runTransaction { transaction ->
                members.forEach { userId ->
                    val userDocRef = roomRef.document(userId)
                    transaction.update(
                        userDocRef,
                        mapOf(
                            "gameInfo" to gameInfoJson,
                            "hasAnswered" to false
                        )
                    )
                }
            }.await()
            Log.d("Repository", "UserProfiles updated with gameName $gameInfoJson")
        }catch (e: Exception){
            Log.e("Repository", "Failed to add gamename to userProfile ${e.message}")
        }
    }
    suspend fun deleteGameNameFromAllUsers(crRoomId: String, members: List<String>){
        try {
            val roomRef = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)

            firestore.runTransaction { transaction ->
                members.forEach { userId ->
                    val userDocRef = roomRef.document(userId)
                    transaction.update(
                        userDocRef,
                        mapOf(
                            "gameInfo" to com.google.firebase.firestore.FieldValue.delete(),
                            "hasAnswered" to com.google.firebase.firestore.FieldValue.delete()
                        )
                    )
                }
            }.await()
            Log.d("Repository", "Fields deleted sucessfully from all userprofiles")
        }catch (e: Exception){
            Log.e("Repsoitory", "Failed to delete fields from userprofiles: ${e.message}", e)
        }
    }
    suspend fun fetchGameInfo(crRoomId: String, userId: String): Title? {
        return try {
            val collection = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)
                .document(userId)
                .get().await()

            val gameInfoJson = collection.getString("gameInfo")

            // Deserialize the JSON string into a Title object
            gameInfoJson?.let {
                runCatching {
                    kotlinx.serialization.json.Json.decodeFromString<Title>(it)
                }.getOrElse { e ->
                    Log.e("Repository", "Failed to decode gameInfo: ${e.message}", e)
                    null
                }
            }
        }catch (e: Exception){
            Log.e("Repository", "Failed to fetch game Info ${e.message}")
            null
        }
    }
    fun updateHasAnswered(crRoomId: String, userId: String, questionsComplete: Boolean): Boolean{
        return try {
            val collection = crGameRoomsCollection
                .document(crRoomId)
                .collection("Users")
                .document(userId)

            collection.set(mapOf("hasAnswered" to questionsComplete), SetOptions.merge())
            Log.d("Repository", "user profile updated/created with hasAnswered: $questionsComplete")
            true
        }catch (e: Exception){
            Log.d("Repository", "Error updating game status ${e.message}")
            false
        }
    }

    private var cachedQuestions: List<Questions>? = null
    suspend fun getAllQuestions(title: String): List<Questions>{
        cachedQuestions?.let {
            return it
        }

        val response = SupabaseClient.client.postgrest["questions"]
            .select(
                filter = {
                    filter("title", FilterOperator.EQ, title)
                }
            )
        val questions = response.decodeList<Questions>()
        cachedQuestions = questions
        return questions
    }



}