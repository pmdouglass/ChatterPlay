package com.example.chatterplay.view_model

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatterplay.data_class.Answers
import com.example.chatterplay.data_class.Questions
import com.example.chatterplay.data_class.SupabaseClient.client
import com.example.chatterplay.data_class.Title
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.repository.ChatRiseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.FilterOperator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ChatRiseViewModel: ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val chatRepository = ChatRiseRepository()
    private val crGameRoomsCollection = firestore.collection("ChatriseRooms")


    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile
    private val _rankingStatus = MutableStateFlow<String?>("View")
    val rankingStatus: StateFlow<String?> = _rankingStatus



    suspend fun getUserProfile(crRoomId: String){
        viewModelScope.launch {
            val profile = chatRepository.getUserProfile(crRoomId, userId)
            _userProfile.value = profile
        }
    }
    fun checkUserRankingStatus(crRoomId: String, userId: String){
        viewModelScope.launch {
            val status = chatRepository.fetchUserRankingStatus(crRoomId, userId)
            _rankingStatus.value = status ?: "View"
        }
    }

    fun updateToRanking(crRoomId: String, userId: String){
        viewModelScope.launch {
            chatRepository.updateUserRankingStatus(crRoomId, userId, "Ranking")
            chatRepository.resetRanking(crRoomId)
            checkUserRankingStatus(crRoomId, userId)
        }
    }
    fun saveRanking(crRoomId: String, memberId: String, userId: String, newPoints: Int){
        viewModelScope.launch {
            Log.d("ViewModel", "Saving ranking system")
            chatRepository.saveRanking(crRoomId, memberId, userId, newPoints)
            chatRepository.updateUserRankingStatus(crRoomId = crRoomId, userId = userId, updatedStatus = "Done")
            checkUserRankingStatus(crRoomId = crRoomId, userId = userId)
        }
    }
    private val _isAllDoneWithQuestions = mutableStateOf(false)
    val isAllDoneWithQuestions: State<Boolean> = _isAllDoneWithQuestions
    fun monitorUntilAllUsersDoneAnsweringQuestions(crRoomId: String, title: String) {
        viewModelScope.launch {
            try {
                crGameRoomsCollection.document(crRoomId).collection("Users")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.d("ViewModel", "Error monitoring games: ${error.message}")
                            return@addSnapshotListener
                        }

                        val statuses = snapshot?.documents?.mapNotNull { it.getBoolean("hasAnswered") } ?: emptyList()
                        if (statuses.isNotEmpty() && statuses.all { it }) {
                            Log.d("ViewModel", "All users are done with all their Questions")
                            _isAllDoneWithQuestions.value = true

                        }
                        if (_isAllDoneWithQuestions.value){
                            addOrUpdateGame(
                                crRoomId = crRoomId,
                                gameName = title,
                                allAnswered = true
                            )
                            Log.d("ViewModel", "Updated $title 'allAnswered' to true")
                        }
                    }
            } catch (e: Exception) {
                Log.d("ViewModel", "Error monitoring games: ${e.message}")
            }
        }
    }

    fun setAllVotesToDone(crRoomId: String){
        viewModelScope.launch {
            try {
                // check all users in room
                val usersSnapshot = chatRepository.getAllUsersInRoom(crRoomId)
                val usersNotDone = mutableListOf<String>()
                val usersDone = mutableListOf<String>()


                /*
                every user who has not voted to vote
                    who has not voted?
                        usersNotDone
                who are they voting on
                    who is every user?
                        allUsers

                 */
                val allUsers = usersSnapshot.documents.map { it.id }
                val currentUserId = this@ChatRiseViewModel.userId

                // Classify all users based on status
                for (document in usersSnapshot.documents){
                    val userId = document.id
                    val rankingStatus = document.getString("rankingStatus") ?: "Ranking"

                    if (rankingStatus != "Done"){
                        usersNotDone.add(userId)

                    }else {
                        usersDone.add(userId)
                    }

                }



                // reward extra points if they voted
                usersDone.forEach { doneUserId ->
                    chatRepository.updatePointsBasedOnVote(
                        crRoomId = crRoomId,
                        userId = doneUserId,
                        hasVoted = true
                    )
                }

                // deduct points if they have not voted
                usersNotDone.forEach { notDoneUserId ->
                    val allOtherUsers = allUsers.filter { it != notDoneUserId }
                    chatRepository.updatePointsBasedOnVote(
                        crRoomId = crRoomId,
                        userId = notDoneUserId,
                        hasVoted = false
                    )
                    // Finishes their votes
                    allOtherUsers.forEach { otherUserId ->
                        chatRepository.saveRanking(
                            crRoomId = crRoomId,
                            memberId = otherUserId,
                            userId = notDoneUserId,
                            newPoints = 0
                        )
                    }

                }
                // Set everyones rankingStatus to 'View"
                allUsers.forEach { userId ->
                    chatRepository.updateUserRankingStatus(
                        crRoomId = crRoomId,
                        userId = userId,
                        updatedStatus = "View"
                    )
                }

                // Update rankingStatus
                _rankingStatus.value = "View"
                checkUserRankingStatus(crRoomId, currentUserId)

                // Handle Ties by adding 1 point
                /*val tiedUsers = userPoints.filterValues { points -> points == userPoints.values.maxOrNull() }
            if (tiedUsers.size > 1){
                val randomUserId = tiedUsers.keys.random()
                chatRepository.addBonusPoint(crRoomId, randomUserId, 1)
                Log.d("ViewModel", "Tie resolved by adding 1 point to $randomUserId")
            }*/
                Log.d("ViewModel", "Final ranking completed successfully")
            }catch (e: Exception){
                Log.d("Ranking", "Error final rank")
            }
        }
    }
    private val _rankedUsers = MutableStateFlow<List<Pair<UserProfile, Int>>>(emptyList())
    val rankedUsers: StateFlow<List<Pair<UserProfile, Int>>> = _rankedUsers
    fun fetchAndSortRankings(crRoomId: String){
        viewModelScope.launch {
            try {
                val rankingSnapshot = chatRepository.getAllRankingsInRoom(crRoomId)
                val userPointsList = rankingSnapshot.documents.mapNotNull { document ->
                    val userId = document.id
                    val totalPoints = document.getLong("totalPoints")?.toInt() ?: 0
                    val userProfile = chatRepository.getUserProfile(crRoomId, userId)
                    userProfile?.let { Pair(it, totalPoints) }
                }
                // Sort by decending
                val sortedUserPointsList = userPointsList.sortedByDescending { it.second }

                _rankedUsers.value = sortedUserPointsList
            }catch (e: Exception){
                Log.d("ViewModel", "Error fetching and sorting")
            }
        }
    }






    //                        Supabase Games
    fun generateRandomGameInfo(crRoomId: String, userIds: List<String>, onResult: (Title?) -> Unit){
        viewModelScope.launch {
            try {
                Log.d("ViewModel", "Generating Random Game for room $crRoomId")
                val randomId = chatRepository.fetchRandomGameInfo(crRoomId)
                if (randomId != null){
                    Log.d("ViewModel", "Random Game Selected: ${randomId.title}")
                }else {
                    deleteGames(crRoomId, userIds)
                    Log.d("ViewModel", "No games available or found for selection")
                }
                onResult(randomId)
                _isAllDoneWithQuestions.value = false
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to get random title ${e.message}")
            }
        }
    }
    fun addGame(crRoomId: String, userIds: List<String>, gameInfo: Title){
        viewModelScope.launch {
            try {
                chatRepository.addGameNameToAllUserProfile(crRoomId, userIds, gameInfo)
                chatRepository.addOrUpdateGame(
                    crRoomId = crRoomId,
                    gameName = gameInfo.title
                )
                _gameInfo.value = gameInfo
                _isAllDoneWithQuestions.value = false
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to add gameName ${e.message}")
            }
        }
    }
    fun deleteGames(crRoomId: String, userIds: List<String>){
        viewModelScope.launch {
            try {
                chatRepository.deleteGameNameFromAllUsers(crRoomId, userIds)
                _isAllDoneWithQuestions.value = false
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to delete games ${e.message}")
            }
        }
    }
    private val _gameInfo = MutableStateFlow<Title?>(null)
    val gameInfo: StateFlow<Title?> = _gameInfo
    fun getGameInfo(crRoomId: String){
        viewModelScope.launch {
            val retrievedGameInfo = chatRepository.fetchGameInfo(crRoomId, userId)
            _gameInfo.value = retrievedGameInfo
        }
    }

    fun addOrUpdateGame(
        crRoomId: String,
        gameName: String,
        hadAlert: Boolean? = null,
        allAnswered: Boolean? = null,
        allDone: Boolean? = null
    ){
        viewModelScope.launch {
            try {
                chatRepository.addOrUpdateGame(
                    crRoomId = crRoomId,
                    gameName = gameName,
                    hadAlert = hadAlert,
                    allAnswered = allAnswered,
                    allDone = allDone
                )
                Log.d("ViewModel", "Updated $gameName")
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to addOrUpdateGame ${e.message}")
            }
        }
    }
    private fun updateHasAnswered(crRoomId: String, questionsComplete: Boolean){
        viewModelScope.launch {
            try {
                chatRepository.updateHasAnswered(crRoomId, userId, questionsComplete)
                Log.d("ViewModel", "Updated hasAnswered to $questionsComplete")
            }catch (e: Exception){
                Log.d("ViewModel", "failed to update game status to $questionsComplete")
            }
        }
    }
    private val _gameQuestions = MutableStateFlow<List<Questions>>(emptyList())
    val gameQuestion: StateFlow<List<Questions>> = _gameQuestions
    fun fetchQuestions(title: String){
        viewModelScope.launch {
            try {
                val query = chatRepository.getAllQuestions(title)
                Log.d("ViewModel", "Fetched ${query.size} questions")
                _gameQuestions.value = query
            }catch (e: Exception){
                Log.d("ViewModel", "failed to fetch questions ${e.message}")
                _gameQuestions.value = emptyList()
            }
        }
    }
    private val _isDoneAnswering = mutableStateOf(false)
    val isDoneAnswering: State<Boolean> = _isDoneAnswering
    suspend fun checkForUsersCompleteAnswers(crRoomId: String, title: String, userId: String): Boolean{
        return try {
            // check if answers exist for this user in supabase
            val response = client.postgrest["answers"]
                .select(
                    filter = {
                        filter("title", FilterOperator.EQ, title)
                        filter("userId", FilterOperator.EQ, userId)
                        filter("crRoomId", FilterOperator.EQ, crRoomId)
                    }
                )
                .decodeList<Answers>()

            val answeredOrNot = response.isNotEmpty()

            if (answeredOrNot){
                crGameRoomsCollection
                    .document(crRoomId)
                    .collection("Users")
                    .document(userId)
                    .set(mapOf("hasAnswered" to true), SetOptions.merge())
                    .await()
                _isDoneAnswering.value = true
            }else {
                _isDoneAnswering.value = false
            }
            answeredOrNot

        }catch (e: Exception){
            Log.d("ViewModel", "Error checking user answers: ${e.message}")
            false
        }
    }
    fun savePairAnswers(crRoomId: String, answers: List<Answers>, gameInfo: Title){
        viewModelScope.launch {
            try {
                if (answers.isNotEmpty()){
                    client.postgrest["answers"].insert(answers)
                    Log.d("ViewModel", "Saved ${answers.size} answers")

                    answers.first().title
                    updateHasAnswered(crRoomId, true)
                    checkForUsersCompleteAnswers(crRoomId, gameInfo.title, userId)

                }

            }catch (e: Exception){
                Log.d("ViewModel", "Failed to save answers ${e.message}")
            }
        }
    }
    @SuppressLint("SuspiciousIndentation")
    fun fetchPairAnswers(crRoomId: String, title: String, onComplete: (List<Answers>) -> Unit){
        viewModelScope.launch {
            try {
            val response = client.postgrest["answers"]
                .select(
                    filter = {
                        filter("title", FilterOperator.EQ, title)
                        filter("crRoomId", FilterOperator.EQ, crRoomId)
                    }
                )
                .decodeList<Answers>()

                Log.d("ViewModel", "Fetched ${response.size}")
                onComplete(response)
            }catch (e: Exception){
                Log.d("ViewModel", "Error fetching answers ${e.message}")
                onComplete(emptyList())
            }
        }
    }


}