package com.example.chatterplay.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.data_class.AlertType
import com.example.chatterplay.data_class.Questions
import com.example.chatterplay.data_class.SupabaseClient.client
import com.example.chatterplay.data_class.Title
import com.example.chatterplay.data_class.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.FilterOperator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    suspend fun saveGame(
        crRoomId: String,
        gameName: String,
        allMembers: List<UserProfile>,
        context: Context
    ): Boolean {
        return try {
            val gameDocRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("Games")
                .document(gameName)

            Log.d("ChatRiseRepository", "Creating game document: $crRoomId -> Games -> $gameName")

            // Initialize hasAnswered map for all members
            val hasAnsweredMap = allMembers.associate { it.userId to false }

            val gameData = mapOf(
                "gameName" to gameName,
                "hasAnswered" to hasAnsweredMap,
                "allAnswered" to false,
                "allDone" to false
            )

            // Save the new game document
            gameDocRef.set(gameData).await()

            // Log the event in Firebase Analytics
            CoroutineScope(Dispatchers.IO).launch {
                val params = Bundle().apply {
                    putString("game_name", gameName)
                    putString("timestamp", System.currentTimeMillis().toString())
                }
                AnalyticsManager.getInstance(context).logEvent("game_created", params)
            }

            Log.d("ChatRiseRepository", "Game document created successfully for $gameName.")
            true
        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Error in saveGame: ${e.message}", e)
            false
        }
    }

    suspend fun updateUsersHasAnswered(crRoomId: String, gameName: String, userId: String, context: Context): Boolean {
        return try {
            val gameDocRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("Games")
                .document(gameName)

            Log.d("ChatRiseRepository", "Accessing document: $crRoomId -> Games -> $gameName")

            // Check if the document exists
            val gameSnapshot = gameDocRef.get().await()
            if (gameSnapshot.exists()) {
                Log.d("ChatRiseRepository", "Game document exists for $gameName. Preparing to update hasAnswered.")

                // Fetch existing hasAnswered map or initialize a new one
                val currentHasAnsweredMap = gameSnapshot.get("hasAnswered") as? MutableMap<String, Boolean> ?: mutableMapOf()
                currentHasAnsweredMap[userId] = true

                // Update the hasAnswered field in Firestore
                gameDocRef.update("hasAnswered", currentHasAnsweredMap).await()
                Log.d("ChatRiseRepository", "Updated hasAnswered for user $userId to true in game $gameName.")

            }

            // Log the update in Firebase Analytics
            CoroutineScope(Dispatchers.IO).launch {
                val params = Bundle().apply {
                    putString("cr_room_id", crRoomId)
                    putString("game_name", gameName)
                    putString("user_id", userId)
                    putString("timestamp", System.currentTimeMillis().toString())
                }
                AnalyticsManager.getInstance(context).logEvent("done_answering_questions", params)
            }

            true
        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Error in updateHasAnswered: ${e.message}", e)
            false
        }
    }
    suspend fun checkUsersHasAnswered(crRoomId: String, gameName: String, userId: String): Boolean? {
        return try {
            val gameDocRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("Games")
                .document(gameName)

            Log.d("ChatRiseRepository", "Fetching hasAnswered for user $userId in $gameName.")

            // Fetch the document
            val gameSnapshot = gameDocRef.get().await()

            // Check if the document exists
            if (gameSnapshot.exists()) {
                // Retrieve the hasAnswered map
                val hasAnsweredMap = gameSnapshot.get("hasAnswered") as? Map<String, Boolean>

                // Return the value for the specific userId
                val hasAnswered = hasAnsweredMap?.get(userId)
                Log.d("ChatRiseRepository", "User $userId hasAnswered: $hasAnswered")
                hasAnswered
            } else {
                Log.d("ChatRiseRepository", "Game document does not exist for $gameName.")
                null
            }
        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Error checking hasAnswered for user $userId: ${e.message}", e)
            null
        }
    }

    fun monitorAllMembersHasAnswered(
        crRoomId: String,
        gameName: String,
        allMembers: List<UserProfile>,
        onCheck: (Boolean) -> Unit, // Return a boolean indicating if all members have answered
        onError: (Exception) -> Unit
    ) {
        try {
            val gameDocRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("Games")
                .document(gameName)

            Log.d("ChatRiseRepository", "Starting monitorAllMembersHasAnswered for $gameName in room: $crRoomId.")

            // Listen for real-time updates on the game document
            gameDocRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRiseRepository", "Error monitoring hasAnswered: ${error.message}", error)
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    // Retrieve the hasAnswered map
                    val hasAnsweredMap = snapshot.get("hasAnswered") as? Map<String, Boolean>

                    if (hasAnsweredMap != null) {
                        Log.d("ChatRiseRepository", "Retrieved hasAnswered map: $hasAnsweredMap")

                        // Check if all members' userIds in the hasAnswered map are explicitly true
                        val allAnswered = allMembers.all { member ->
                            val hasAnswered = hasAnsweredMap[member.userId] ?: false // Default to false if null
                            Log.d("ChatRiseRepository", "UserId: ${member.userId}, hasAnswered: $hasAnswered")
                            hasAnswered
                        }

                        Log.d("ChatRiseRepository", "All members have answered: $allAnswered")
                        onCheck(allAnswered)
                    } else {
                        Log.d("ChatRiseRepository", "No hasAnswered map found in the document.")
                        onCheck(false) // If the map is missing, treat it as not all answered
                    }
                } else {
                    Log.d("ChatRiseRepository", "Game document does not exist or has been deleted.")
                    onCheck(false) // If the document doesn't exist, treat it as not all answered
                }
            }
        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Error setting up monitorAllMembersHasAnswered: ${e.message}", e)
            onError(e)
        }
    }
    suspend fun areAllMembersAnswered(crRoomId: String, gameName: String): Boolean {
        return try {
            // Access the document in Firestore
            val gameDocRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("Games")
                .document(gameName)

            Log.d("ChatRiseRepository", "Checking if all members have answered for $gameName in room: $crRoomId.")

            // Fetch the document
            val gameSnapshot = gameDocRef.get().await()

            if (gameSnapshot.exists()) {
                // Retrieve the hasAnswered map
                val hasAnsweredMap = gameSnapshot.get("hasAnswered") as? Map<String, Boolean>

                if (hasAnsweredMap != null) {
                    // Check if all values in the map are true
                    val allAnswered = hasAnsweredMap.all { (_, hasAnswered) ->
                        hasAnswered == true // Ensure each value is true
                    }

                    Log.d("ChatRiseRepository", "All members have answered: $allAnswered")
                    allAnswered
                } else {
                    Log.d("ChatRiseRepository", "No hasAnswered map found in the document.")
                    false // If the map is missing, return false
                }
            } else {
                Log.d("ChatRiseRepository", "Game document does not exist for $gameName.")
                false // If the document doesn't exist, return false
            }
        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Error checking if all members have answered: ${e.message}", e)
            false // On exception, return false
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
    suspend fun saveGameNameToRoom(crRoomId: String, gameInfo: Title) {
        try {
            // Serialize the Title object to JSON
            val gameInfoJson = kotlinx.serialization.json.Json.encodeToString(gameInfo)
            Log.d("ChatRiseRepository", "Serialized gameInfo: $gameInfoJson")

            val roomRef = crGameRoomsCollection.document(crRoomId)

            firestore.runTransaction { transaction ->
                try {
                    transaction.update(
                        roomRef,
                        mapOf(
                            "gameInfo" to gameInfoJson
                        )
                    )
                    Log.d("ChatRiseRepository", "Updated gameInfo for room: $crRoomId")
                } catch (e: Exception) {
                    Log.e(
                        "ChatRiseRepository",
                        "Error updating gameInfo for room: $crRoomId, ${e.message}",
                        e
                    )
                }
            }.await()
            Log.d("ChatRiseRepository", "Successfully updated gameInfo for the room.")
        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Failed to save gameName to room: ${e.message}", e)
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
    suspend fun getGameInfo(crRoomId: String): Title? {
        return try {
            val collection = crGameRoomsCollection
                .document(crRoomId)
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
            Log.d("ChatRiseRepository", "Fetched AlertType: $type")

            type?.let {
                try {
                    val alertType = AlertType.valueOf(it).toString()
                    Log.d("ChatRiseRepository", "Valid AlertType: $alertType")
                    alertType
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
    suspend fun updateUsersHadAnswered(crRoomId: String, userId: String, title: String){
        val collection = crGameRoomsCollection
            .document(crRoomId)
            .collection("Games")
            .document(title)

        val snapshot = collection.get().await()
        if (snapshot.exists()){

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
    suspend fun removeGameNameFromRoom(crRoomId: String) {
        try {
            val roomDocRef = crGameRoomsCollection.document(crRoomId)

            firestore.runTransaction { transaction ->
                transaction.update(
                    roomDocRef,
                    mapOf(
                        "gameInfo" to com.google.firebase.firestore.FieldValue.delete()
                    )
                )
            }.await()

            Log.d("Repository", "Successfully deleted game info from room: $crRoomId")
        } catch (e: Exception) {
            Log.e("Repository", "Failed to delete game info from room: ${e.message}", e)
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