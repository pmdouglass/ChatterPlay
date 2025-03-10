package com.example.chatterplay.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.data_class.AlertType
import com.example.chatterplay.data_class.ChatMessage
import com.example.chatterplay.data_class.ChatRoom
import com.example.chatterplay.data_class.Questions
import com.example.chatterplay.data_class.SupabaseClient.client
import com.example.chatterplay.data_class.Title
import com.example.chatterplay.data_class.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.FilterOperator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString


class ChatRiseRepository(private val sharedPreferences: SharedPreferences) {
    private val firestore = FirebaseFirestore.getInstance()
    private val userCollection = firestore.collection("Users")
    private val crGameRoomsCollection = firestore.collection("ChatriseRooms")

    private val users = "Users"
    private val games = "Games"
    private val ranking = "Rankings"
    private val private = "PrivateChats"
    private val influencer = "InfluencerChats"


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
    fun saveLocalcrRoomId(userId: String, crRoomId: String){
        sharedPreferences.edit().putString("crRoomId_$userId", crRoomId).apply()
        Log.d("ChatRiseRepository", "crRoomId saving to $userId")
    }


    /**
     *  Odds N' Ends
     */
    suspend fun getMainRoomInfo(crRoomId: String): ChatRoom?{
        return try {
            val snapshot = crGameRoomsCollection
                .document(crRoomId).get().await()
            val chatRoom = snapshot.toObject(ChatRoom::class.java)
            chatRoom
        }catch (e: Exception){
            null
        }
    }

    /**
     *  User Management
     */
    suspend fun getcrUserProfile(crRoomId: String, userId: String): UserProfile?{
        val snapshot = crGameRoomsCollection
            .document(crRoomId)
            .collection("AllPlayers")
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
    suspend fun RemoveSelectedPlayer(crRoomId: String, userId: String) {
        try {
            Log.d("Firestore", "Starting process to block user $userId from room $crRoomId")


            // Reference to the "users" collection inside the chat room
            val roomCollectionRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("Users")

            val roomRef = crGameRoomsCollection
                .document(crRoomId)



            Log.d("Firestore", "Fetching documents inside users collection for room: $crRoomId")

            // Fetch all documents inside the "users" collection
            val documents = roomCollectionRef.get().await()

            var deleted = false
            for (document in documents.documents) {
                if (document.id == userId) {
                    Log.d("Firestore", "Found user document with ID $userId. Deleting now...")

                    roomCollectionRef.document(document.id).delete().await()
                    Log.d("Firestore", "Successfully deleted user document: $userId from room $crRoomId")
                    deleted = true
                    break
                }
            }

            if (!deleted) {
                Log.w("Firestore", "User document $userId not found in room $crRoomId. Skipping deletion.")
            }




            // remove userId from 'members' field in the room document
            Log.d("Firestore", "Removing userId $userId from members list in room $crRoomId")
            roomRef.update("members", FieldValue.arrayRemove(userId)).await()
            Log.d("Firestore", "Successfully removed userId $userId from members list in room $crRoomId")


            // Reference to the user document in userCollection
            val userDocRef = userCollection.document(userId)

            Log.d("Firestore", "Updating user $userId's room status...")

            // Update the user's status in Firestore
            userDocRef.update(
                mapOf(
                    "gameRoomId" to "0",
                    "pending" to "NotPending"
                )
            ).await()
            saveLocalcrRoomId(userId, "0")

            Log.d("Firestore", "Successfully updated user $userId: gameRoomId -> 0, pending -> NotPending")

        } catch (e: Exception) {
            Log.e("Firestore", "Error blocking user $userId from room $crRoomId", e)
        }
    }
    suspend fun updateCurrentRank(crRoomId: String, userId: String, newRank: Int) {
        try {
            Log.d("Firestore", "Updating rank for user $userId in room $crRoomId to $newRank")

            val userDocRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("Users")
                .document(userId)

            val userSnapshot = userDocRef.get().await()

            if (userSnapshot.exists()) {
                Log.d("Firestore", "User document found. Updating rank...")

                userDocRef.update("currentRank", newRank).await()

                Log.d("Firestore", "Successfully updated currentRank for user $userId to $newRank")
            } else {
                Log.w("Firestore", "User document not found in room $crRoomId. Rank update skipped.")
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error updating currentRank for user $userId in room $crRoomId", e)
        }
    }
    suspend fun getCurrentRank(crRoomId: String, userId: String): Int? {
        return try {
            Log.d("Firestore", "Fetching current rank for user $userId in room $crRoomId")

            val userDocRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("Users")
                .document(userId)

            val userSnapshot = userDocRef.get().await()

            if (userSnapshot.exists()) {
                val currentRank = userSnapshot.getLong("currentRank")?.toInt()
                Log.d("Firestore", "User $userId has currentRank: $currentRank")
                currentRank
            } else {
                Log.w("Firestore", "User document not found in room $crRoomId. Returning null.")
                null
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching currentRank for user $userId in room $crRoomId", e)
            null
        }
    }
    suspend fun getRoomCreatedTimestamp(crRoomId: String){
        val docRef = crGameRoomsCollection
            .document(crRoomId)


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
    suspend fun checkIfAllMembersAnswered(crRoomId: String, gameName: String): Boolean {
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
    fun listenForAllMembersAnswered(crRoomId: String, gameName: String, trigger: () -> Unit): Flow<Boolean> = callbackFlow {
        val gameDocRef = firestore
            .collection("ChatriseRooms")
            .document(crRoomId)
            .collection("Games")
            .document(gameName)

        val listener = gameDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ChatRiseRepository", "Error listening for updates: ${error.message}", error)
                close(error) // Stop flow on error
            }

            if (snapshot != null && snapshot.exists()) {
                val hasAnsweredMap = snapshot.get("hasAnswered") as? Map<String, Boolean> ?: emptyMap()
                val allAnswered = hasAnsweredMap.isNotEmpty() && hasAnsweredMap.all { (_, answered) -> answered }
                val previousAllAnswered = snapshot.getBoolean("allAnswered") ?: false

                Log.d("ChatRiseRepository", "Listening for all members answered. Status: $allAnswered (Previous: $previousAllAnswered)")

                trySend(allAnswered) // ✅ Emit current status (True/False)

                // ✅ Only update Firestore if the value changed from false → true
                if (!previousAllAnswered && allAnswered) {
                    trigger()

                    gameDocRef.update("allAnswered", true)
                        .addOnSuccessListener {
                            Log.d("ChatRiseRepository", "✅ Successfully updated 'allAnswered' to true (Only once)")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ChatRiseRepository", "Error updating 'allAnswered': ${e.message}", e)
                        }
                }
            }
        }

        awaitClose { listener.remove() } // ✅ Ensure cleanup on ViewModel destruction
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
    suspend fun getUserGivenVotes(crRoomId: String, userId: String, getUserProfile: suspend (String) -> UserProfile?): List<Pair<UserProfile, Int>>? {
        return try {
            val rankingsRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("Rankings")
                .get()
                .await()

            val userVotes = mutableListOf<Pair<UserProfile, Int>>()

            for (document in rankingsRef.documents) {
                val memberId = document.id // The user who received the vote
                val votes = document.get("votes") as? Map<String, Map<String, Any>> ?: emptyMap()
                val pointsGiven = (votes[userId]?.get("pointsGiven") as? Long)?.toInt() ?: 0

                if (pointsGiven > 0) {
                    val userProfile = getUserProfile(memberId) // Fetch UserProfile using memberId
                    userProfile?.let {
                        userVotes.add(it to pointsGiven) // Pair(UserProfile, pointsGiven)
                    }
                }
            }

            // Sort in descending order based on points given
            val sortedVotes = userVotes.sortedByDescending { it.second }

            Log.d("Repository", "User $userId has given votes (sorted): $sortedVotes")
            sortedVotes
        } catch (e: Exception) {
            Log.e("Repository", "Failed to fetch votes given by user $userId", e)
            null // Return null if an error occurs
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
    suspend fun getAllRankDocuments(crRoomId: String): QuerySnapshot? {
        return try {
            val snapshot = crGameRoomsCollection.document(crRoomId)
                .collection("Rankings")
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot // Return the QuerySnapshot
            } else {
                Log.w("Firestore", "No rankings found for crRoomId: $crRoomId")
                null // Return null if the collection is empty
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching rankings for crRoomId: $crRoomId - ${e.message}", e)
            null // Return null if an exception occurs
        }
    }
    suspend fun assignRanks(crRoomId: String) {
        try {
            val rankingSnapshot = getAllRankDocuments(crRoomId) // Fetch all rank documents

            if (rankingSnapshot != null) {
                val userPointsList = rankingSnapshot.documents.mapNotNull { document ->
                    val userId = document.id
                    val totalPoints = document.getLong("totalPoints")?.toInt() ?: 0
                    val userProfile = getcrUserProfile(crRoomId, userId)
                    userProfile?.let { Pair(it, totalPoints) }
                }

                // Sort users in descending order based on totalPoints
                val sortedUserPointsList = userPointsList.sortedByDescending { it.second }

                // Assign ranks and update Firestore
                sortedUserPointsList.forEachIndexed { index, (userProfile, _) ->
                    val rank = index + 1
                    updateCurrentRank(crRoomId, userProfile.userId, rank)
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error updating rankings: ${e.message}")
        }
    }



    suspend fun getUserVotes(crRoomId: String, userId: String){
        val documentSnapshot = crGameRoomsCollection
            .document(crRoomId)
            .collection("Rankings")
            .document()
    }
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
    suspend fun getUserRankingStatusFlow(crRoomId: String, userId: String): Flow<String?>{
        return flow {
            val snapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)
                .document(userId)
                .snapshots()

            snapshot.collect{ document ->
                emit(document.getString("rankingStatus"))
            }
        }.flowOn(Dispatchers.IO)
    }
    suspend fun getUserSeenRankingsFlow(crRoomId: String, userId: String): Flow<Boolean?>{
        return flow {
            val snapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)
                .document(userId)
                .snapshots()

            snapshot.collect{ document ->
                emit(document.getBoolean("seenRankResult"))
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateUsersSeenRankResults(crRoomId: String, userId: String, seenResult: Boolean){
        val documentRef = crGameRoomsCollection
            .document(crRoomId)
            .collection(users)
            .document(userId)

        documentRef.set(mapOf("seenRankResult" to seenResult), SetOptions.merge())
    }
    suspend fun getSeenRankResult(crRoomId: String, userId: String): Boolean{
        val documentRef = crGameRoomsCollection
            .document(crRoomId)
            .collection(users)
            .document(userId)

        return try {
            val snapshot = documentRef.get().await()
            snapshot.getBoolean("seenRankResult") ?: false
        }catch (e: Exception){
            false
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
    suspend fun getCurrentRanks(crRoomId: String): List<Pair<UserProfile, Int>> {
        val collectionRef = crGameRoomsCollection.document(crRoomId).collection(users)

        return try {
            val snapshot = collectionRef.get().await()
            val rankList = snapshot.documents.mapNotNull { document ->
                val userId = document.id
                val currentRank = document.getLong("currentRank")?.toInt() ?: return@mapNotNull null

                val userProfile = UserProfile(
                    userId = userId,
                    fname = document.getString("fname") ?: "Unknown",
                    imageUrl = document.getString("imageUrl") ?: ""
                )

                if (currentRank == 0) null else userProfile to currentRank
            }.sortedBy { it.second } // ✅ Ensures sorting from "1" → "2" → "3" → "4"

            Log.d("ChatRiseRepository", "Fetched Ranks: $rankList") // ✅ Debugging

            rankList
        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Error fetching ranks: ${e.message}", e)
            emptyList()
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














    /**
     *  Blocking Management
     */
    suspend fun saveSelectedBlockedPlayer(crRoomId: String, userId: String) {
        val documentRef = crGameRoomsCollection.document(crRoomId).collection("Users").document(userId)
        val documentAllRef = crGameRoomsCollection.document(crRoomId).collection("AllPlayers").document(userId)
        val documentRankRef = crGameRoomsCollection.document(crRoomId).collection("Rankings").document(userId)


        val batch = Firebase.firestore.batch()

        try {
            // Delete the user's ranking document
            batch.delete(documentRankRef)

            // Update "pending" field in both collections atomically
            batch.set(documentRef, mapOf("pending" to "Blocked"), SetOptions.merge())
            batch.set(documentAllRef, mapOf("pending" to "Done"), SetOptions.merge())

            batch.commit().await() // Execute batch operation

            Log.d("ChatRiseRepository", "Successfully updated BlockedPlayer for crRoomId: $crRoomId and marked as Done in AllPlayers.")
        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Error updating BlockedPlayer: ${e.message}", e)
        }
    }
    suspend fun getCurrentBlockedPlayer(crRoomId: String): String?{
        return try {
            val querySnapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection("Users")
                .whereEqualTo("pending", "Blocked")
                .get().await()

            querySnapshot.documents.firstOrNull()?.id
        }catch (e: Exception){
            Log.e("EntryRepository", "Error fetching blocked user", e)
            null
        }
    }
    suspend fun getAllDonePlayers(crRoomId: String): List<String> {
        val userCollection = crGameRoomsCollection.document(crRoomId).collection("AllPlayers")

        return try {
            val snapshot = userCollection
                .whereEqualTo("pending", "Done") // Query for documents where "pending" == "Done"
                .get()
                .await()

            val userIds = snapshot.documents.mapNotNull { it.id } // Extract document IDs (User IDs)

            Log.d("ChatRiseRepository", "Found ${userIds.size} users marked as 'Done' in crRoomId: $crRoomId")

            userIds // Return list of user IDs
        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Error fetching 'Done' players: ${e.message}", e)
            emptyList() // Return empty list in case of an error
        }
    }
    suspend fun removeBlockedPlayerFromPrivateAndGroupChats(crRoomId: String, userId: String) {
        try {
            val docRef = crGameRoomsCollection
                .document(crRoomId)
                .collection(private)

            // Fetch all chat documents
            val snapshot = docRef.get().await()

            for (document in snapshot.documents) {
                val membersList = document.get("members") as? List<String> ?: emptyList()

                // Check if the user is part of the chat
                if (userId in membersList) {
                    if (membersList.size == 2) {
                        // If only two members (private chat), delete the entire document
                        document.reference.delete().await()
                        Log.d("Firestore", "Deleted private chat for user: $userId")
                    } else {
                        // If more than two members (group chat), remove the user from the members list
                        val updatedMembers = membersList.filterNot { it == userId }
                        document.reference.update("members", updatedMembers).await()
                        Log.d("Firestore", "Removed user $userId from group chat in $crRoomId")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error removing user $userId from chats", e)
        }
    }

    suspend fun removeBlockedPlayer(crRoomId: String, userId: String) {
        try {
            val userDocRef = userCollection.document(userId)
            val userRef = crGameRoomsCollection.document(crRoomId).collection("Users").document(userId)
            val roomRef = crGameRoomsCollection.document(crRoomId)

            // Fetch the current room details
            val snapshot = roomRef.get().await()
            if (snapshot.exists()) {
                val currentMembers = snapshot.get("members") as? List<String> ?: emptyList()

                // Remove user from members list if present
                if (userId in currentMembers) {
                    val updatedMembers = currentMembers - userId
                    roomRef.update("members", updatedMembers).await()
                    Log.d("ChatRiseRepository", "User $userId removed from members list in room $crRoomId.")
                }
            }

            // Batch operation for efficiency
            val batch = FirebaseFirestore.getInstance().batch()

            // Update user pending status to "NotPending"
            batch.update(userDocRef, mapOf("gameRoomId" to "0", "pending" to "NotPending"))

            // Remove user document from chat room
            batch.delete(userRef)

            batch.commit().await()

            Log.d("ChatRiseRepository", "Successfully removed blocked user $userId from room $crRoomId.")

        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Error removing blocked player $userId from room $crRoomId: ${e.message}", e)
        }
    }
    suspend fun sendBlockedMessage(crRoomId: String, userId: String, message: ChatMessage) {
        val roomRef = crGameRoomsCollection
            .document(crRoomId)
            .collection("BlockedMessage")

        val docId = roomRef.document().id

        roomRef.document(docId).set(message).await()
    }
    suspend fun getAllBlockedMessages(crRoomId: String): List<ChatMessage> {
        val roomRef = crGameRoomsCollection.document(crRoomId).collection("BlockedMessage")

        return try {
            val snapshot = roomRef.get().await()
            if (!snapshot.isEmpty) {
                snapshot.documents.mapNotNull { it.toObject(ChatMessage::class.java) }
            } else {
                Log.d("ChatRepository", "No blocked messages found for crRoomId: $crRoomId")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching blocked messages: ${e.message}", e)
            emptyList()
        }
    }
    suspend fun getBloccfdgkedMessage(crRoomId: String, blockedUserId: String): ChatMessage? {
        return try {
            val roomRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("BlockedMessage")

            // Query to find the message where "BlockedMessage" is equal to userId
            val snapshot = roomRef
                .whereEqualTo("BlockedMessage", blockedUserId) // Only fetch messages for the blocked user
                .limit(1) // We expect only one result
                .get()
                .await()

            val document = snapshot.documents.firstOrNull() // Get the first document if it exists
            val blockedMessage = document?.toObject(ChatMessage::class.java) // Convert to ChatMessage object

            Log.d("Firestore", "Blocked message fetched: ${blockedMessage?.message}")

            blockedMessage // Return the found ChatMessage object

        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching blocked message for $blockedUserId", e)
            null
        }
    }
    suspend fun sendSystemMessage(
        crRoomId: String,
        chatMessage: ChatMessage
    ) {
        try {
            Log.d("ChatRepository", "Sending message to crRoomId: $crRoomId")

            // Determine the appropriate room reference
            val roomRef = crGameRoomsCollection
                .document(crRoomId)

            // Add timestamp to the chat message
            val messageWithTimestamp = chatMessage.copy(timestamp = Timestamp.now())

            // Execute Firestore transaction to send the message and update the room metadata
            firestore.runTransaction { transaction ->
                // Add the message to the room's messages collection
                transaction.set(roomRef.collection("messages").document(), messageWithTimestamp)

                // Update the room's metadata
                transaction.update(
                    roomRef, mapOf(
                        "lastMessage" to chatMessage.message,
                        "lastMessageTimestamp" to messageWithTimestamp.timestamp,
                        "lastProfile" to chatMessage.image,
                        "hiddenFor" to emptyList<String>(),
                        "hiddenTimestamp" to emptyMap<String, Timestamp>()
                    )
                )
            }.await()

            Log.d("ChatRepository", "Message successfully sent to roomId: $crRoomId")
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending message to roomId: $crRoomId and crRoomId: $crRoomId - ${e.message}", e)
        }
    }











    /**
     *  Top Player Management
     */

    suspend fun createTopPlayerChatRoom(CRRoomId: String, members: List<String>, roomName: String): String {
        val sortedMembers = members.sorted()
        val roomId = crGameRoomsCollection.document().collection("TopPlayers").document().id
        val chatRoom = ChatRoom(roomId = roomId, members = sortedMembers, roomName = roomName)
        crGameRoomsCollection.document(CRRoomId)
            .collection("TopPlayers")
            .document(roomId)
            .set(chatRoom).await()
        return roomId
    }
    suspend fun checkIfTopPlayerRoomExist(CRRoomId: String, members: List<String>): String? {
        val sortedMembers = members.sorted()
        val querySnapshot = crGameRoomsCollection
            .document(CRRoomId)
            .collection("TopPlayers")
            .get().await()

        for(document in querySnapshot.documents){
            val chatRoom = document.toObject(ChatRoom::class.java)
            if (chatRoom != null && chatRoom.members.sorted() == sortedMembers) {
                return document.id
            }
        }
        return null
    }
    suspend fun checkTopPlayers(crRoomId: String): Pair<String?, String?> {
        return try {
            val documentSnapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection("TopPlayers")
                .document("TopPlayers")
                .get().await()

            if (documentSnapshot.exists()) {
                val userId1 = documentSnapshot.getString("Rank1")
                val userId2 = documentSnapshot.getString("Rank2")
                Pair(userId1, userId2)
            } else {
                Pair(null, null) // Document doesn't exist
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(null, null) // Handle errors gracefully
        }
    }

    suspend fun addMemberToTopPlayerRoom(CRRoomId: String, roomId: String, memberId: String){
        val roomRef = crGameRoomsCollection
            .document(CRRoomId)
            .collection("TopPlayers")
            .document(roomId)
        roomRef.update("members", FieldValue.arrayUnion(memberId)).await()
    }
    suspend fun sendTopPlayerMessage(CRRoomId: String, roomId: String, chatMessage: ChatMessage) {
        val roomRef = crGameRoomsCollection.document(CRRoomId).collection("TopPlayers").document(roomId)
        val messageWithTimestamp = chatMessage.copy(timestamp = Timestamp.now())
        firestore.runTransaction { transaction ->
            transaction.set(roomRef.collection("messages").document(), messageWithTimestamp)
            transaction.update(roomRef, mapOf(
                "lastMessage" to chatMessage.message,
                "lastMessageTimestamp" to messageWithTimestamp.timestamp,
                "lastProfile" to chatMessage.image,
                "hiddenFor" to emptyList<String>(),
                "hiddenTimestamp" to emptyMap<String, Timestamp>()
            ))
        }.await()
    }
    suspend fun sendGoodbyeMessage(CRRoomId: String, roomId: String, chatMessage: ChatMessage, remove: String){
        val roomRef = crGameRoomsCollection.document(CRRoomId).collection("TopPlayers").document(roomId)
        val messageWithTimestamp = chatMessage.copy(timestamp = Timestamp.now())
        val chatMessageMap = mapOf(
            "message" to messageWithTimestamp.message,
            "timestamp" to messageWithTimestamp.timestamp,
            "image" to messageWithTimestamp.image,
            "senderId" to messageWithTimestamp.senderId,
            "senderName" to messageWithTimestamp.senderName
        )
        val updatedFields = chatMessageMap + ("remove" to remove)
        firestore.runTransaction { transaction ->
            transaction.set(roomRef.collection("Goodbye").document(), updatedFields)
        }.await()

        val snapshot = crGameRoomsCollection.document(CRRoomId).collection("TopPlayers").document("TopPlayers")
        snapshot.update("Removed", remove).await()
    }
    suspend fun onHoldStatus(CRRoomId: String){
        val roomRef = crGameRoomsCollection
            .document(CRRoomId)
            .collection("TopPlayers")
            .document("TopPlayers")
            .get().await()


    }
    suspend fun isRemovedSet(CRRoomId: String): Boolean{
        val roomRef = crGameRoomsCollection
            .document(CRRoomId)
            .collection("TopPlayers")
            .document("TopPlayers")

        return try {
            val documentSnapshot = roomRef.get().await()
            if (documentSnapshot.exists()){
                val removedField = documentSnapshot.getString("Removed")
                !removedField.isNullOrBlank()
            } else {
                false
            }
        } catch (e: Exception){
            false
        }
    }
    suspend fun getTopPlayerChatMessages(CRRoomId: String, roomId: String, userId: String): List<ChatMessage> {
        val roomSnapshot = crGameRoomsCollection.document(CRRoomId)
            .collection("TopPlayers")
            .document(roomId)
            .get()
            .await()
        val chatRoom = roomSnapshot.toObject(ChatRoom::class.java) ?: return emptyList()
        val hiddenTimestamp = chatRoom.hiddenTimestamp[userId] ?: Timestamp(0, 0)

        val querySnapshot = crGameRoomsCollection
            .document(CRRoomId)
            .collection("TopPlayers")
            .document(roomId)
            .collection("messages")
            .whereGreaterThan("timestamp", hiddenTimestamp)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .await()
        return querySnapshot.documents.map { document ->
            document.toObject(ChatMessage::class.java)!!
        }
    }
    fun getUsersSelection(crRoomId: String, userId: String): Flow<UserProfile?> = callbackFlow {
        try {
            val documentRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("TopPlayers")
                .document(userId)

            val listener = documentRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRiseRepository", "Error fetching selected player: ${error.message}", error)
                    trySend(null) // Send null if there's an error
                    return@addSnapshotListener
                }

                try {
                    val selectedPlayerId = snapshot?.getString("Selected Player")

                    // Handle case where selectedPlayerId is an empty string
                    if (selectedPlayerId.isNullOrBlank()) {
                        Log.w("ChatRiseRepository", "Selected player ID is empty for userId: $userId")
                        trySend(null) // Send null if selected player is empty
                        return@addSnapshotListener
                    }

                    val userProfileRef = crGameRoomsCollection
                        .document(crRoomId)
                        .collection("Users")
                        .document(selectedPlayerId)

                    userProfileRef.get().addOnSuccessListener { userProfileSnapshot ->
                        val userProfile = userProfileSnapshot.toObject(UserProfile::class.java)
                        Log.d("ChatRiseRepository", "Fetched UserProfile: $userProfile for selected player ID: $selectedPlayerId")
                        trySend(userProfile)
                    }.addOnFailureListener { fetchError ->
                        Log.e("ChatRiseRepository", "Error fetching user profile for ID: $selectedPlayerId", fetchError)
                        trySend(null)
                    }

                } catch (e: Exception) {
                    Log.e("ChatRiseRepository", "Error processing selected player ID", e)
                    trySend(null) // Send null if an unexpected error occurs
                }
            }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Error initializing getUsersSelection", e)
            trySend(null)
        }
    }

    suspend fun checkTradeStatus(CRRoomId: String, userId: String): String {
        val topPlayerRef = crGameRoomsCollection
            .document(CRRoomId)
            .collection("TopPlayers")
            .document("TopPlayers")

        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(topPlayerRef)

            val currentStatus = snapshot.getString(userId) ?: ""

            currentStatus
        }.await()
    }
    suspend fun UpdateTradeStatus(CRRoomId: String, userId: String, otherUser: String) {
        val topPlayerRef = crGameRoomsCollection
            .document(CRRoomId)
            .collection("TopPlayers")
            .document("TopPlayers")

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(topPlayerRef)

            val currentStatus = snapshot.getString(userId) ?: ""
            val otherUserOnHold = snapshot.getString(otherUser)

            if(currentStatus.isBlank() || currentStatus == "Canceled"){
                transaction.update(topPlayerRef, userId, "onHold")
            }
            if(otherUserOnHold == "onHold"){
                transaction.update(topPlayerRef, userId, "Confirmed")
                transaction.update(topPlayerRef, otherUser, "Confirmed")
            }
        }.await()
    }
    suspend fun updateUserTradeStatus(crRoomId: String, userId: String, otherUserId: String, status: String){
        val topPlayerRef = crGameRoomsCollection
            .document(crRoomId)
            .collection("TopPlayers")
            .document("TopPlayers")

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(topPlayerRef)
            val currentUserStatus = snapshot.getString(userId) ?: ""
            val otherUserStatus = snapshot.getString(otherUserId) ?: ""



            transaction.update(topPlayerRef, userId, status)
        }
    }
    suspend fun updateTradeStatus(crRoomId: String, userId: String, status: String){
        try {
            val roomRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("TopPlayers")
                .document("TopPlayers")

            roomRef.update("TradeStatus.$userId", status).await()
            Log.d("ChatRiseRepository", "Trade status updated for $userId: $status")
        }catch (e: Exception){
            Log.e("ChatRiseRepository", "error updating trade status: ${e.message}", e)
        }
    }
    suspend fun getTradeStatus(crRoomId: String, userId: String): String?{
        return try {
            val snapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection("TopPlayers")
                .document("TopPlayers")
                .get().await()

            val tradeStatusMap = snapshot.get("TradeStatus") as? Map<String, String>
            tradeStatusMap?.get(userId)
        }catch (e: Exception){
            Log.e("ChatRiseRepository", "Error fetching trade status: ${e.message}", e)
            null
        }
    }
    fun listenForTradeStatusUpdates(crRoomId: String, userId: String, callback: (String?) -> Unit){
        crGameRoomsCollection
            .document(crRoomId)
            .collection("TopPlayers")
            .document("TopPlayers")
            .addSnapshotListener { snapshot, error ->
                if (error != null){
                    Log.e("ChatRiseRepository", "Error listening for updates: ${error.message}", error)
                    return@addSnapshotListener
                }
                val tradeStatusMap = snapshot?.get("TradeStatus") as? Map<String, String>
                val newStatus = tradeStatusMap?.get(userId)
                callback(newStatus)
            }
    }
    suspend fun saveCurrentUsersSelection(CRRoomId: String, currentUserId: String, selectedPlayerId: String?){
        try {
            val topPlayersSelection = hashMapOf(
                "Selected Player" to (selectedPlayerId ?: "")
            )

            crGameRoomsCollection
                .document(CRRoomId)
                .collection("TopPlayers")
                .document(currentUserId)
                .set(topPlayersSelection)
                .await()

            Log.d("ChatRiseRepository", "Saved selection: _ $selectedPlayerId _ ")
        }catch (e: Exception){
            Log.d("ChatRiseRepository", "Error saving current user Selection")
        }
    }
    suspend fun cancelTradeStatus(CRRoomId: String, userId: String) {
        val topPlayerRef = crGameRoomsCollection
            .document(CRRoomId)
            .collection("TopPlayers")
            .document("TopPlayers")

        firestore.runTransaction { transaction ->
            transaction.update(topPlayerRef, userId, "Canceled")
        }
    }
    suspend fun saveTopTwoPlayers(crRoomId: String, rank1: String, rank2: String) {
        val topPlayersData = hashMapOf(
            "Rank1" to rank1,
            "Rank2" to rank2
        )

        crGameRoomsCollection
            .document(crRoomId)
            .collection("TopPlayers")
            .document("TopPlayers")
            .set(topPlayersData)
            .await() // ✅ Ensures Firestore write completes before returning
    }
    suspend fun getTopPlayers(crRoomId: String): Pair<String?, String?>? {
        return try {
            val topPlayerSnapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection("TopPlayers")
                .document("TopPlayers")
                .get().await()

            val rank1 = topPlayerSnapshot.getString("Rank1")
            val rank2 = topPlayerSnapshot.getString("Rank2")

            Pair(rank1, rank2)
        } catch (e: Exception){
            null
        }
    }
    suspend fun getTopTwoPlayers(crRoomId: String): Pair<Pair<String?, Int?>, Pair<String?, Int?>>? {
        return try {
            val documentRef = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)

            val snapshot = documentRef.get().await()
            if (snapshot.isEmpty) return null

            val userRankList = snapshot.documents.mapNotNull { document ->
                val userId = document.id
                val currentRank = document.getLong("currentRank")?.toInt() ?: return@mapNotNull null
                if (currentRank == 0) null else userId to currentRank
            }

            val sortedRanks = userRankList.sortedBy { it.second }

            val rank1 = sortedRanks.getOrNull(0)
            val rank2 = sortedRanks.getOrNull(1)

            Log.d("ChatRiseRepository", "Top two players -> rank1: $rank1, rank2: $rank2")
            Pair(rank1 ?: (null to null), rank2 ?: (null to null))
        }catch (e: Exception){
            null
        }
    }
    suspend fun getTopPlayerRoomId(crRoomId: String, userId: String): String? {
        return try {
            Log.d("ChatRepository", "Fetching roomId from crGameRoomsCollection in crRoomId: $crRoomId for userId: $userId")

            // Get all documents in the "TopPlayers" collection
            val documents = crGameRoomsCollection
                .document(crRoomId)
                .collection("TopPlayers")
                .get()
                .await()

            // Find the first document where the userId is a member
            val roomDocument = documents.documents.firstOrNull { document ->
                val membersList = document.get("members") as? List<String> ?: emptyList()
                userId in membersList // Check if userId exists in the members list
            }

            return roomDocument?.id?.also {
                Log.d("ChatRepository", "Found roomId: $it for userId: $userId")
            } ?: run {
                Log.w("ChatRepository", "No valid roomId found for userId: $userId in crRoomId: $crRoomId")
                null
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching roomId from TopPlayers collection", e)
            null
        }
    }
    suspend fun userSelectionPick(crRoomId: String, userId: String, pick: String) {
        try {
            Log.d("ChatRiseRepository", "Saving user pick for crRoomId: $crRoomId, userId: $userId, pick: $pick")

            val docRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("TopPlayers")
                .document("TopPlayers")

            val snapshot = docRef.get().await()

            if (snapshot.exists()){
                val pickMap = mapOf(
                    "pick.$userId" to pick
                )

                docRef.update(pickMap).await()
                Log.d("ChatRiseRepository", "Successfully updated pick for userId: $userId in TopPlayers")
            }else {
                Log.w("ChatRiseRepository", "topPlayers socument does not exist, not updating.")
            }

        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Error saving user pick for userId: $userId in crRoomId: $crRoomId", e)
        }
    }
    suspend fun getUserPickSelection(crRoomId: String): Map<String, String>? {
        return try {
            Log.d("ChatRiseRepository", "Fetching user picks for crRoomId: $crRoomId")

            val docRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("TopPlayers")
                .document("TopPlayers")

            val snapshot = docRef.get().await()

            if (snapshot.exists()) {
                val pickMap = snapshot.get("pick") as? Map<String, String>
                pickMap?.also {
                    Log.d("ChatRiseRepository", "Retrieved user picks: $it")
                }
            } else {
                Log.d("ChatRiseRepository", "No pick data found for crRoomId: $crRoomId")
                null
            }
        } catch (e: Exception) {
            Log.e("ChatRiseRepository", "Error fetching user picks for crRoomId: $crRoomId", e)
            null
        }
    }
    suspend fun updatecrUserGameStatus(crRoomId: String, userId: String, status: String){

        val userDocRef = crGameRoomsCollection
            .document(crRoomId)
            .collection("Users")
            .document(userId)

        userDocRef.update(
            mapOf(
                "pending" to status
            )
        ).await()

    }
    suspend fun fetchUsersPendingState(): List<String>{
        return try {
            val querySnapshot = userCollection.whereEqualTo("pending", "Pending").get().await()
            querySnapshot.documents.mapNotNull { it.id }
        }catch (e: Exception){
            emptyList()
        }
    }
    suspend fun getGoodbyeMessage(crRoomId: String, roomId: String): ChatMessage?{
        return try {
            val snapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection("TopPlayers")
                .document(roomId)
                .collection("Goodbye")
                .get().await()

            val document = snapshot.documents.firstOrNull()

            document?.toObject(ChatMessage::class.java)
        }catch (e: Exception) {
            Log.e("Firestore", "Error fetching goodbye message", e)
            null
        }

    }
    suspend fun deleteTopPlayerCollection(crRoomId: String) {
        try {
            val collectionRef = crGameRoomsCollection
                .document(crRoomId)
                .collection("TopPlayers")

            val documents = collectionRef.get().await()

            // Use a batch operation to delete all documents inside "TopPlayers"
            val batch = firestore.batch()
            for (document in documents) {
                batch.delete(document.reference)
            }
            batch.commit().await() // Execute the batch deletion

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }






}


fun calculateRoomElapsedTime(createdAt: Timestamp): Map<String, Int> {
    val creationDateMillis = createdAt.toDate().time // Convert Firestore Timestamp to milliseconds
    val currentDateMillis = System.currentTimeMillis()

    val totalDays = ((currentDateMillis - creationDateMillis) / (1000 * 60 * 60 * 24)).toInt() // Ensures day 1 starts at creation
    val Week = (totalDays / 8) + 1 // Starts at Week 1
    val Day = (totalDays % 7).let { if (it == 0) 7 else it } // Ensures Day 1–7 cycle

    return mapOf(
        "total" to totalDays,
        "week" to Week,
        "day" to Day
    )
}

