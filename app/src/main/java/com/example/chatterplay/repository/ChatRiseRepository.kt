package com.example.chatterplay.repository

import android.content.SharedPreferences
import android.util.Log
import com.example.chatterplay.data_class.AlertType
import com.example.chatterplay.data_class.Questions
import com.example.chatterplay.data_class.SupabaseClient.client
import com.example.chatterplay.data_class.Title
import com.example.chatterplay.data_class.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.FilterOperator
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString


class ChatRiseRepository(private val sharedPreferences: SharedPreferences) {
    private val firestore = FirebaseFirestore.getInstance()
    private val crGameRoomsCollection = firestore.collection("ChatriseRooms")

    private val users = "Users"
    private val games = "Games"
    private val ranking = "Ranksings"


    /**
     *  Shared Preferences
     */
    fun saveUserLocalAlertType(userId: String, alertType: String){
        sharedPreferences.edit().putString("AlertType_$userId", alertType).apply()
        Log.d("ChatRiseRepository", "AlertType saving to $alertType")
    }
    fun loadUserLocalAlertType(userId: String): String? {
        val alertType = sharedPreferences.getString("AlertType_$userId", null)
        Log.d("ChatRiseRepository", "AlertType loading as: $alertType")
        return alertType
    }

    /**
     *  User Management
     */
    suspend fun getUserProfile(crRoomId: String, userId: String): UserProfile?{
        val snapshot = crGameRoomsCollection
            .document(crRoomId)
            .collection(users)
            .document(userId)
            .get().await()
        return snapshot.toObject(UserProfile::class.java)
    }
    suspend fun getAllUsers(crRoomId: String) =
        crGameRoomsCollection.document(crRoomId).collection(users).get().await()!!
    suspend fun getCRRoomMembers(crRoomId: String): List<UserProfile>{
        return try {
            val snapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)
                .get().await()
            val members = snapshot.toObjects(UserProfile::class.java)
            Log.d("ChatRiseRepository", "Got members: ${members.map { it.userId }}")
            members
        }catch (e: Exception) {
            Log.e("ChatRiseRepository", "Error getting members: ${e.message}")
            emptyList()
        }
    }






    /**
     *  Game Management
     */
    fun saveQuestionsToFirebase(crRoomId: String, title: String, questionId: Int, memberId: String){
        val collection = crGameRoomsCollection
            .document(crRoomId)
            .collection("Games")
            .document(title)

        val questions = mapOf(
            questionId.toString() to memberId
        )
        collection.set(questions, SetOptions.merge())

    }
    suspend fun saveOrUpdateGame(crRoomId: String, gameName: String, userId: String? = null, allMembers: List<UserProfile>? = null, hadAlert: Boolean? = null, allAnswered: Boolean? = null, allDone: Boolean? = null): Boolean {
        return try {
            val gameDocRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("Games")
                .document(gameName)

            Log.d("ChatRiseRepository", "Accessing document: $crRoomId -> Games -> $gameName")

            // Check if the document exists
            val gameSnapshot = gameDocRef.get().await()
            if (gameSnapshot.exists()) {
                Log.d("ChatRiseRepository", "Game document exists for $gameName. Preparing to update.")

                val updates = mutableMapOf<String, Any>()
                allAnswered?.let {
                    updates["allAnswered"] = it
                    Log.d("ChatRiseRepository", "Updating allAnswered to: $it")
                }
                allDone?.let {
                    updates["allDone"] = it
                    Log.d("ChatRiseRepository", "Updating allDone to: $it")
                }

                if (userId != null && hadAlert != null) {
                    val currentHadAlertMap = gameSnapshot.get("hadAlert") as? MutableMap<String, Boolean> ?: mutableMapOf()
                    currentHadAlertMap[userId] = hadAlert
                    updates["hadAlert"] = currentHadAlertMap
                    Log.d("ChatRiseRepository", "Updating hadAlert for user $userId to: $hadAlert")
                }

                if (updates.isNotEmpty()) {
                    gameDocRef.update(updates).await()
                    Log.d("ChatRiseRepository", "Game updated successfully for $gameName.")
                } else {
                    Log.d("ChatRiseRepository", "No fields to update for $gameName.")
                }
            } else {
                Log.d("ChatRiseRepository", "Game document does not exist for $gameName. Creating new document.")

                val hadAlertMap = allMembers?.associate { it.userId to false } ?: emptyMap()

                val gameData = mapOf(
                    "gameName" to gameName,
                    "hadAlert" to hadAlertMap,
                    "allAnswered" to false,
                    "allDone" to false
                )

                gameDocRef.set(gameData).await()
                Log.d("ChatRiseRepository", "Game document created successfully for $gameName.")
            }

            true
        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Error in saveOrUpdateGame: ${e.message}", e)
            false
        }
    }

    suspend fun saveGameNameToAllUsers(crRoomId: String, members: List<String>, gameInfo: Title) {
        try {
            // Serialize the Title object to JSON
            val gameInfoJson = kotlinx.serialization.json.Json.encodeToString(gameInfo)
            Log.d("ChatRiseRepository", "Serialized gameInfo: $gameInfoJson")

            val roomRef = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)

            firestore.runTransaction { transaction ->
                members.forEach { userId ->
                    val userDocRef = roomRef.document(userId)

                    try {
                        transaction.update(
                            userDocRef,
                            mapOf(
                                "gameInfo" to gameInfoJson,
                                "hasAnswered" to false
                            )
                        )
                        Log.d("ChatRiseRepository", "Updated gameInfo for user: $userId")
                    } catch (e: Exception) {
                        Log.e(
                            "ChatRiseRepository",
                            "Error updating gameInfo for user: $userId, ${e.message}",
                            e
                        )
                    }
                }
            }.await()
            Log.d("ChatRiseRepository", "Successfully updated gameInfo for all users.")
        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Failed to save gameName to all users: ${e.message}", e)
        }
    }

    suspend fun getRandomGameInfo(crRoomId: String): Title?{
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
            val response = client.postgrest["title"]
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
    suspend fun getAlertStatus(crRoomId: String, userId: String): Boolean {
        return try {
            val snapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)
                .document(userId)
                .get()
                .await()

            if (snapshot.exists()) {
                val alert = snapshot.getBoolean("hadAlert")
                alert ?: false // Return false if AlertStatus is null
            } else {
                Log.d("ChatRiseRepository", "Document for user $userId does not exist in room $crRoomId.")
                false
            }
        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Failed to check user alert status: ${e.message}", e)
            false
        }
    }

    suspend fun getUsersGameAlert(crRoomId: String, userId: String, gameName: String): Boolean{
        return try {
            val gameDocRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("Games")
                .document(gameName)

            val gameSnapshot = gameDocRef.get().await()
            if (gameSnapshot.exists()){
                val hadAlertMap = gameSnapshot.get("hadAlert") as? Map<String, Boolean> ?: emptyMap()
                val userAlertStatus = hadAlertMap?.get(userId) ?: false
                Log.d("Repository", "User $userId alert status: $userAlertStatus")
                userAlertStatus

            }else {
                Log.d("Repository", "Game document does not exist for $gameName")
                false
            }
        }catch (e: Exception){
            Log.e("Repository", "Failed to check user alert game status ${e.message}", e)
            false
        }
    }
    suspend fun getGameInfo(crRoomId: String, userId: String): Title? {
        return try {
            val collection = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)
                .document(userId)
                .get().await()

            val gameInfoJson = collection.getString("gameInfo")

            // Deserialize the JSON string into a Title object
            gameInfoJson?.let {
                try {
                    val gameInfo = kotlinx.serialization.json.Json.decodeFromString<Title>(it)
                    Log.d("Repository", "Success decoding gameInfo: $gameInfoJson")
                    gameInfo
                }catch (e: Exception) {
                    Log.e("Repository", "Failed to decode gameInfo: ${e.message}", e)
                    null
                }
            }
        }catch (e: Exception){
            Log.e("Repository", "Failed to fetch game Info ${e.message}")
            null
        }
    }

    private var cachedQuestions: List<Questions>? = null
    suspend fun getAllQuestions(title: String): List<Questions>{
        cachedQuestions?.let {
            return it
        }

        val response = client.postgrest["questions"]
            .select(
                filter = {
                    filter("title", FilterOperator.EQ, title)
                }
            )
        val questions = response.decodeList<Questions>()
        cachedQuestions = questions
        return questions
    }
    suspend fun getAllAssignedQuestionIds(crRoomId: String, title: String): List<Int>{
        return try {
            val collection = crGameRoomsCollection
                .document(crRoomId)
                .collection("Games")
                .document(title)
                .get().await()

            // extract all question Ids
            collection.data?.keys?.mapNotNull { it.toIntOrNull() } ?: emptyList()

        }catch (e: Exception){
            Log.d("RepositoryQ", "Error getting assigned questions from firebase")
            emptyList()
        }
    }
    suspend fun getAssignedQuestionId(crRoomId: String, title: String, userId: String): Int?{
        return try {
            val collection = crGameRoomsCollection
                .document(crRoomId)
                .collection("Games")
                .document(title)
                .get().await()

            collection.data?.filterValues { it == userId }?.keys?.firstOrNull()?.toIntOrNull()
        }catch (e: Exception) {
            Log.e("RepositoryQ", "Error fetching questionId for user: ${e.message}")
            null
        }
    }
    suspend fun getQuestionDetails(questionId: Int): Questions?{
        return try {
            val response = client.postgrest["questions"]
                .select(
                    filter = {
                        filter("id", FilterOperator.EQ, questionId)
                    }
                )
                .decodeSingle<Questions>()
            response
        }catch (e: Exception) {
            Log.e("RepositoryQ", "Error fetching question details: ${e.message}")
            null
        }
    }
    suspend fun getSystemAlertType(crRoomId: String): String?{
        return try {
            val collection = crGameRoomsCollection
                .document(crRoomId)
                .get().await()

            val type = collection.getString("AlertType")

            type?.let {
                try {
                    AlertType.valueOf(it).toString()
                }catch (e: IllegalArgumentException){
                    Log.d("ChatRiseRepository", "Invalid System AlertType value: $it")
                    null
                }

            }
        }catch (e: Exception){
            Log.d("ChatRiseRepository", "Error fetching AlertType: ${e.message}")
            null
        }
    }
    suspend fun getUsersAlertType(crRoomId: String, userId: String): AlertType?{
        return try {
            val collection = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)
                .document(userId)
                .get().await()

            val type = collection.getString("AlertType")

            type?.let {
                try {
                    AlertType.valueOf(it)
                }catch (e: IllegalArgumentException){
                    Log.d("ChatRiseRepository", "Invalid User AlertType value: $it")
                    null
                }
            }
        }catch (e: Exception){
            Log.d("ChatRiseRepository", "Error fetching AlertType: ${e.message}")
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
    suspend fun updateUserGameAlert(crRoomId: String, userId: String, gameName: String): Boolean?{
        return try {
            val gameDocRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("Games")
                .document(gameName)

            val gameSnapshot = gameDocRef.get().await()
            if (gameSnapshot.exists()){
                val currentHadAlertMap = gameSnapshot.get("hadAlert") as? MutableMap<String, Boolean> ?: mutableMapOf()
                currentHadAlertMap[userId] = true

                val update = mapOf("hadAlert" to currentHadAlertMap)
                gameDocRef.update(update).await()
                Log.d("Repository", "Successfully updated hadAlert for user $userId in game $gameName to true")
                true
            }else {
                Log.d("Repository", "Game document does not exist for $gameName")
                false
            }
        }catch (e: Exception){
            Log.e("Repository", "Error updating game alert ${e.message}", e)
            false
        }
    }
    fun updateSystemAlertType(crRoomId: String, alertType: AlertType): Boolean{
        return try {
            val collection = crGameRoomsCollection
                .document(crRoomId)

            collection.set(mapOf("AlertType" to alertType.string), SetOptions.merge())
            Log.d("Repository", "user profile updated AlertType: ${alertType.string}")
            true
        }catch (e: Exception){
            Log.d("Repository", "Error updating AlertType ${e.message}")
            false
        }
    }
    fun updateUsersAlertType(crRoomId: String, userId: String, alertType: AlertType): Boolean{
        return try {
            val collection = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)
                .document(userId)

            collection.set(mapOf("AlertType" to alertType.string), SetOptions.merge())
            Log.d("Repository", "user profile updated AlertType: ${alertType.string}")
            true
        }catch (e: Exception){
            Log.d("Repository", "Error updating AlertType ${e.message}")
            false
        }
    }
    fun updateAlertStatus(crRoomId: String, userId: String, alertStatus: Boolean): Boolean{
        return try {
            val collection = crGameRoomsCollection
                .document(crRoomId)
                .collection("Users")
                .document(userId)

            collection.set(mapOf("hadAlert" to alertStatus), SetOptions.merge())
            Log.d("Repository", "user profile updated AlertStatus: $alertStatus")
            true
        }catch (e: Exception){
            Log.d("Repository", "Error updating AlertStatus ${e.message}")
            false
        }
    }
    suspend fun resetGameNameFromAllUsers(crRoomId: String, members: List<String>){
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









    /**
     * Rankings Management
     */
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
    fun saveBonusPoint(crRoomId: String, userId: String, bonus: Int){
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
    suspend fun getRanks(crRoomId: String) =
        crGameRoomsCollection.document(crRoomId).collection("Rankings").get().await()!!
    suspend fun getUserRankingStatus(crRoomId: String, userId: String): String? {
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
}