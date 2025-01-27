package com.example.chatterplay.view_model

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.data_class.Answers
import com.example.chatterplay.data_class.Questions
import com.example.chatterplay.data_class.SupabaseClient.client
import com.example.chatterplay.data_class.Title
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.repository.ChatRiseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.github.jan.supabase.gotrue.mfa.FactorType.TOTP.value
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




    /**
     *  User Management
     */
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile
    suspend fun getUserProfile(crRoomId: String){
        viewModelScope.launch {
            val profile = chatRepository.getUserProfile(crRoomId, userId)
            _userProfile.value = profile
        }
    }








    /**
     *  Game Management
     */
    private val _isAllDoneWithQuestions = mutableStateOf(false)
    val isAllDoneWithQuestions: State<Boolean> = _isAllDoneWithQuestions
    private val _gameInfo = MutableStateFlow<Title?>(null)
    val gameInfo: StateFlow<Title?> = _gameInfo
    private val _usersAlertStatus = MutableStateFlow<Boolean?>(null)
    val usersAlertStatus: StateFlow<Boolean?> = _usersAlertStatus
    private val _gameQuestions = MutableStateFlow<List<Questions>>(emptyList())
    val gameQuestion: StateFlow<List<Questions>> = _gameQuestions
    private val _isDoneAnswering = mutableStateOf<Boolean?>(null)
    val isDoneAnswering: State<Boolean?> = _isDoneAnswering
    private val _userAnswer = MutableStateFlow<Answers?>(null)
    val userAnswer: StateFlow<Answers?> = _userAnswer
    private val _currentQuestion = MutableStateFlow<Questions?>(null)
    val currentQuestion: StateFlow<Questions?> = _currentQuestion



    fun generateRandomGameInfo(crRoomId: String, onResult: (Title?) -> Unit){
        viewModelScope.launch {
            try {
                Log.d("ViewModel", "Generating Random Game for room $crRoomId")
                val randomId = chatRepository.getRandomGameInfo(crRoomId)
                if (randomId != null){
                    Log.d("ViewModel", "Random Game Selected: ${randomId.title}")
                }else {
                    Log.d("ViewModel", "No games available or found for selection")
                }
                onResult(randomId)
                _isAllDoneWithQuestions.value = false
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to get random title ${e.message}")
            }
        }
    }
    fun saveGame(crRoomId: String, userIds: List<String>, gameInfo: Title, allMembers: List<UserProfile>? = null){
        viewModelScope.launch {
            try {
                chatRepository.saveGameNameToAllUsers(crRoomId, userIds, gameInfo)
                chatRepository.saveOrUpdateGame(
                    crRoomId = crRoomId,
                    gameName = gameInfo.title,
                    allMembers = allMembers
                )


                /*if (gameInfo.title == "Mystery Caller" && allMembers != null){
                    val generatedPairs = createUserPairs(allMembers)
                    saveMysterCallerPairs(
                        crRoomId = crRoomId,
                        gameName = gameInfo.title,
                        pairs = generatedPairs
                    ){
                        Log.d("ViewModel", "${gameInfo.title} pairs saved successfully.")
                    }
                }*/
                if (gameInfo.title == "Twisted Truths"){
                    allMembers?.let { nonNullMembers ->
                        saveAssignQuestionsStatement(
                            crRoomId = crRoomId,
                            title = gameInfo.title,
                            members = nonNullMembers
                        )
                    }?: Log.e("ViewModel", "AllMembers is null. Cannot assign questions.")
                }
                _gameInfo.value = gameInfo
                _isAllDoneWithQuestions.value = false
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to add gameName ${e.message}")
            }
        }
    }
    fun saveAssignQuestionsStatement(crRoomId: String, title: String, members: List<UserProfile>){
        viewModelScope.launch {
            try {
                // get Questions for title in Supabase
                val supabaseQuestions = chatRepository.getAllQuestions(title)

                if (members.isEmpty() || supabaseQuestions.isEmpty()){
                    throw IllegalArgumentException("Member IDs or Supabase Questions cannot be empty")
                }

                val memberIds = members.map { it.userId }

                // filter out already assigned questions from firebase
                val assignedQuestionIds = chatRepository.getAllAssignedQuestionIds(crRoomId, title)
                val unassignedQuestions = supabaseQuestions.filter { it.id !in assignedQuestionIds }

                // Randomize the unassigned questions
                val randomizedQuestions = unassignedQuestions.shuffled()

                // Ensure there are enough questions for members
                if (randomizedQuestions.size < memberIds.size){
                    throw IllegalArgumentException("Not enough questions for all members!")
                }

                // Assign each member a unique question
                val questionToMemberMap = memberIds.mapIndexed { index, memberId ->
                    randomizedQuestions[index] to memberId
                }

                // Store in Firebase
                questionToMemberMap.forEach { (question, memberId) ->
                    chatRepository.saveQuestionsToFirebase(
                        crRoomId = crRoomId,
                        title = title,
                        questionId = question.id,
                        memberId = memberId
                    )
                }

                Log.d("ViewModelQ", "Successfully assigned questions to members.")

            }catch (e: java.lang.IllegalArgumentException){
                Log.e("ViewModelQ", "Validation Error: ${e.message}")
            } catch (e: Exception){
                Log.d("ViewModelQ", "Error assigning questionStatement ${e.message}")
            }
        }
    }
    fun savePairAnswers(crRoomId: String, answers: List<Answers>, gameInfo: Title){
        viewModelScope.launch {
            try {
                if (answers.isNotEmpty()){
                    client.postgrest["answers"].insert(answers)
                    Log.d("ViewModel", "Saved ${answers.size} answers")

                    answers.first().title
                    updateHasAnswered(crRoomId)
                    checkUserForAllCompleteAnswers(crRoomId, gameInfo.title)
                }
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to save pair answers ${e.message}")
            }
        }
    }
    fun saveQuestionStatement(crRoomId: String, answer: Answers, gameInfo: Title){
        viewModelScope.launch {
            try {
                client.postgrest["answers"].insert(answer)
                Log.d("ViewModel", "Saved $answer")

                answer.title
                // update has answered
                // check for users completed answers
                //updateHasAnswered(crRoomId)
                checkUserForAllCompleteAnswers(crRoomId, gameInfo.title)
            }catch (e: Exception){
                Log.d("ViewModel", "Error saving questionStatement")
            }
        }
    }
    private fun saveOrUpdateGame(crRoomId: String, gameName: String, userId: String? = null, hadAlert: Boolean? = null, allAnswered: Boolean? = null, allDone: Boolean? = null){
        viewModelScope.launch {
            try {
                chatRepository.saveOrUpdateGame(
                    crRoomId = crRoomId,
                    gameName = gameName,
                    userId = userId,
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
    fun fetchGameInfo(crRoomId: String){
        viewModelScope.launch {
            try {
                val retrievedGameInfo = chatRepository.getGameInfo(crRoomId, userId)
                if (retrievedGameInfo != null){
                    _gameInfo.value = retrievedGameInfo
                    Log.d("ViewModel", "Game info successfully retried: $retrievedGameInfo")
                }else {
                    Log.d("ViewModel", "No game info found for crRoomId: $crRoomId")
                }
            }catch (e: Exception){
                Log.e("ViewModel", "Error retrieving game info: ${e.message}", e)
            }
        }
    }
    fun fetchUsersGameAlert(crRoomId: String, userId: String, gameName: String){
        viewModelScope.launch {
            try {
                val status = chatRepository.getUsersGameAlert(
                    crRoomId = crRoomId,
                    userId = userId,
                    gameName = gameName
                )
                _usersAlertStatus.value = status
                when (status) {
                    true -> Log.d("ViewModel", "User $userId has been alerted")
                    false -> Log.d("ViewModel", "User $userId has not been alerted")
                }
            }catch (e: Exception){
                Log.d("ViewModel", "failed to get users game alert status ${e.message}")
            }
        }
    }
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
    fun fetchQuestionForUser(crRoomId: String, title: String){
        viewModelScope.launch {
            try {
                // get questionid from firebase
                val questionId = chatRepository.getAssignedQuestionId(crRoomId, title, userId)

                if (questionId != null){
                    // get question details from supabase
                    val question = chatRepository.getQuestionDetails(questionId)
                    _currentQuestion.value = question
                }else {
                    Log.e("ViewModelQ", "No questionId found for user $userId")
                }
            } catch (e: Exception) {
                Log.e("ViewModelQ", "Error fetching question for user: ${e.message}")
            }
        }
    }
    @SuppressLint("SuspiciousIndentation")
    fun fetchAnswers(crRoomId: String, title: String, onComplete: (List<Answers>) -> Unit){
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
    @SuppressLint("SuspiciousIndentation")
    fun fetchUsersAnswers(crRoomId: String, title: String){
        viewModelScope.launch {
            try {
                val response = client.postgrest["answers"]
                    .select(
                        filter = {
                            filter("title", FilterOperator.EQ, title)
                            filter("crRoomId", FilterOperator.EQ, crRoomId)
                            filter("userId", FilterOperator.EQ, userId)
                        }
                    )
                    .decodeSingle<Answers>()

                Log.d("ViewModel", "Fetched $response")
                _userAnswer.value = response
            }catch (e: Exception){
                Log.d("ViewModel", "Error fetching answers ${e.message}")
                null
            }
        }
    }
    suspend fun checkUserForAllCompleteAnswers(crRoomId: String, title: String): Boolean{
        return try {
            if (title.isEmpty()){
                Log.d("ViewModel", "title is empty; skipping check for completed answers.")
                _isDoneAnswering.value = null
                return false
            }

            // check if answers exist for this user in supabase
            Log.d("ViewModel", "Checking for completed answers")
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
                Log.d("ViewModel", "user has answered: $answeredOrNot - Updated firestore.")
            }else {
                Log.d("ViewModel", "User has not answered: $answeredOrNot")
            }
            _isDoneAnswering.value = answeredOrNot
            Log.d("ViewModel", "isDoneAnswering set to: $answeredOrNot")
            answeredOrNot
        }catch (e: Exception){
            Log.d("ViewModel", "Error checking user answers: ${e.message}")
            _isDoneAnswering.value = null
            false
        }
    }
    suspend fun checkForUsersCompleteAnswer(crRoomId: String, title: String): Boolean{
        return try {
            if (title.isEmpty()){
                Log.d("ViewModel", "title is empty; skipping check for completed answers.")
                _isDoneAnswering.value = null
                return false
            }

            // check if answer exist for this user in supabase
            Log.d("ViewModel", "Checking for completed answers")
            val response = client.postgrest["answers"]
                .select(
                    filter = {
                        filter("title", FilterOperator.EQ, title)
                        filter("userId", FilterOperator.EQ, userId)
                        filter("crRoomId", FilterOperator.EQ, crRoomId)
                    }
                )
                .decodeSingleOrNull<Answers>()

            val answeredOrNot = response != null

            if (answeredOrNot){
                crGameRoomsCollection
                    .document(crRoomId)
                    .collection("Users")
                    .document(userId)
                    .set(mapOf("hasAnswered" to true), SetOptions.merge())
                    .await()
                Log.d("ViewModel", "user has answered: $answeredOrNot - Updated firestore.")
            }else {
                Log.d("ViewModel", "User has not answered: $answeredOrNot")
            }
            _isDoneAnswering.value = answeredOrNot
            Log.d("ViewModel", "isDoneAnswering set to: $answeredOrNot")
            answeredOrNot
        }catch (e: Exception){
            Log.d("ViewModel", "Error checking user answers: ${e.message}")
            _isDoneAnswering.value = null
            false
        }
    }
    fun monitorUntilAllUsersDoneAnswering(crRoomId: String, title: String) {
        viewModelScope.launch {
            try {
                Log.d("ViewModel", "monitoring until all users are done anxwering")
                crGameRoomsCollection.document(crRoomId).collection("Users")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.d("ViewModel", "Error monitoring games: ${error.message}")
                            return@addSnapshotListener
                        }

                        val statuses = snapshot?.documents?.mapNotNull { it.getBoolean("hasAnswered") } ?: emptyList()
                        if (statuses.isNotEmpty() && statuses.all { it }) {
                            Log.d("ViewModel", "monitor - All users are done with all their Questions")
                            _isAllDoneWithQuestions.value = true

                        }
                        if (_isAllDoneWithQuestions.value){
                            saveOrUpdateGame(
                                crRoomId = crRoomId,
                                gameName = title,
                                allAnswered = true
                            )
                            Log.d("ViewModel", "monitor - Updated $title 'allAnswered' to true")
                        }
                    }
            } catch (e: Exception) {
                Log.d("ViewModel", "Error monitoring games: ${e.message}")
            }
        }
    }
    fun updateGameAlert(crRoomId: String, gameName: String, hadAlert: Boolean){
        viewModelScope.launch {
            try {
                val status = chatRepository.updateUserGameAlert(crRoomId, userId, gameName, hadAlert)
                _usersAlertStatus.value = status
            }catch (e: Exception){
                Log.d("ViewModel", "Error updating user $userId to gameAlert $hadAlert: ${e.message}")
            }
        }
    }
    private fun updateHasAnswered(crRoomId: String){
        viewModelScope.launch {
            try {
                val success = chatRepository.updateHasAnswered(crRoomId, userId, true)
                if (success){
                    _isDoneAnswering.value = true
                    Log.d("ViewModel", "Update hasAnswered to true successful")
                }else {
                    Log.d("ViewModel", "Failed to update hasAnswered to true")
                }
                Log.d("ViewModel", "Updated hasAnswered to true")
            }catch (e: Exception){
                Log.d("ViewModel", "failed to update game status to true: ${e.message}")
            }
        }
    }
    fun resetGames(crRoomId: String, userIds: List<String>, gameName: String){
        viewModelScope.launch {
            try {
                chatRepository.resetGameNameFromAllUsers(crRoomId, userIds)
                _isAllDoneWithQuestions.value = false
                chatRepository.saveOrUpdateGame(
                    crRoomId = crRoomId,
                    gameName = gameName,
                    allDone = true
                )

                val retrievedGameInfo = chatRepository.getGameInfo(crRoomId, userId)
                _gameInfo.value = retrievedGameInfo

            }catch (e: Exception){
                Log.d("ViewModel", "Failed to delete games ${e.message}")
            }
        }
    }








    /**
     *  Ranking Management
     */
    private val _rankingStatus = MutableStateFlow<String?>("View")
    val rankingStatus: StateFlow<String?> = _rankingStatus
    private val _rankedUsers = MutableStateFlow<List<Pair<UserProfile, Int>>>(emptyList())
    val rankedUsers: StateFlow<List<Pair<UserProfile, Int>>> = _rankedUsers
    fun saveRanking(crRoomId: String, memberId: String, userId: String, newPoints: Int){
        viewModelScope.launch {
            Log.d("ViewModel", "Saving ranking system")
            chatRepository.saveRanking(crRoomId, memberId, userId, newPoints)
            chatRepository.updateUserRankingStatus(crRoomId = crRoomId, userId = userId, updatedStatus = "Done")
            checkUserRankingStatus(crRoomId = crRoomId, userId = userId)
        }
    }
    fun fetchAndSortRankings(crRoomId: String){
        viewModelScope.launch {
            try {
                val rankingSnapshot = chatRepository.getRanks(crRoomId)
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
    fun checkUserRankingStatus(crRoomId: String, userId: String){
        viewModelScope.launch {
            val status = chatRepository.getUserRankingStatus(crRoomId, userId)
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
    fun setAllVotesToDone(crRoomId: String){
        viewModelScope.launch {
            try {
                // check all users in room
                val usersSnapshot = chatRepository.getAllUsers(crRoomId)
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


}