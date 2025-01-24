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
import com.example.chatterplay.data_class.askQuestion
import com.example.chatterplay.repository.ChatRiseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.FilterOperator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    fun generateRandomGameInfo(crRoomId: String, onResult: (Title?) -> Unit){
        viewModelScope.launch {
            try {
                Log.d("ViewModel", "Generating Random Game for room $crRoomId")
                val randomId = chatRepository.fetchRandomGameInfo(crRoomId)
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






    fun addGame(crRoomId: String, userIds: List<String>, gameInfo: Title, allMembers: List<UserProfile>? = null){
        viewModelScope.launch {
            try {
                chatRepository.addGameNameToAllUserProfile(crRoomId, userIds, gameInfo)
                chatRepository.addOrUpdateGame(
                    crRoomId = crRoomId,
                    gameName = gameInfo.title,
                    allMembers = allMembers
                )
                if (gameInfo.title == "Mystery Caller" && allMembers != null){
                    val generatedPairs = createUserPairs(allMembers)
                    saveMysterCallerPairs(
                        crRoomId = crRoomId,
                        gameName = gameInfo.title,
                        pairs = generatedPairs
                    ){
                        Log.d("ViewModel", "${gameInfo.title} pairs saved successfully.")
                    }
                }
                _gameInfo.value = gameInfo
                _isAllDoneWithQuestions.value = false
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to add gameName ${e.message}")
            }
        }
    }
















    fun deleteGames(crRoomId: String, userIds: List<String>, gameName: String){
        viewModelScope.launch {
            try {
                chatRepository.deleteGameNameFromAllUsers(crRoomId, userIds)
                _isAllDoneWithQuestions.value = false
                chatRepository.addOrUpdateGame(
                    crRoomId = crRoomId,
                    gameName = gameName,
                    allDone = true
                )

                val retrievedGameInfo = chatRepository.fetchGameInfo(crRoomId, userId)
                _gameInfo.value = retrievedGameInfo

            }catch (e: Exception){
                Log.d("ViewModel", "Failed to delete games ${e.message}")
            }
        }
    }
    fun updateGameAlertStatus(crRoomId: String, gameName: String, hadAlert: Boolean){
        viewModelScope.launch {
            try {
                val status = chatRepository.updateGameAlertStatus(crRoomId, userId, gameName, hadAlert)
                _usersAlertStatus.value = status
            }catch (e: Exception){
                Log.d("ViewModel", "Error updating user $userId to gameAlert $hadAlert: ${e.message}")
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
    private val _usersAlertStatus = MutableStateFlow<Boolean?>(null)
    val usersAlertStatus: StateFlow<Boolean?> = _usersAlertStatus
    fun getUsersGameAlert(crRoomId: String, userId: String, gameName: String){
        viewModelScope.launch {
            try {
                val status = chatRepository.checkUsersGameAlert(
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
    private fun addOrUpdateGame(crRoomId: String, gameName: String, userId: String? = null, hadAlert: Boolean? = null, allAnswered: Boolean? = null, allDone: Boolean? = null){
        viewModelScope.launch {
            try {
                chatRepository.addOrUpdateGame(
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
    suspend fun checkForUsersCompleteAnswers(crRoomId: String, title: String): Boolean{
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
                    updateHasAnswered(crRoomId)
                    checkForUsersCompleteAnswers(crRoomId, gameInfo.title)
                }
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to save pair answers ${e.message}")
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



    //               supabase anon questions
    private val _theyHaveAsked = MutableStateFlow(false)
    val theyHaveAsked: StateFlow<Boolean> = _theyHaveAsked.asStateFlow()
    private val _youHaveAsked = MutableStateFlow(false)
    val youHaveAsked: StateFlow<Boolean> = _youHaveAsked.asStateFlow()
    private val _theyHaveAnswered = MutableStateFlow(false)
    val theyHaveAnswered: StateFlow<Boolean> = _theyHaveAnswered.asStateFlow()
    private val _youHaveAnswered = MutableStateFlow(false)
    val youHaveAnswered: StateFlow<Boolean> = _youHaveAnswered.asStateFlow()

    fun saveTargetQuestion(crRoomId: String, title: String, question: askQuestion, userId: String, toUserId: String){
        viewModelScope.launch {
            try {
                client.postgrest["askQuestion"].insert(question)
                Log.d("ViewModel", "Saved 1 Question from $userId")

                chatRepository.updateHasAsked(
                    crRoomId = crRoomId,
                    gameName = title,
                    userId = userId,
                    onSuccess = {
                        fetchToUserQuestion(crRoomId, userId = userId, toUserId = toUserId)
                        Log.d("ViewModel", "Successfully updated hasAsked for user $userId")
                    },
                    onError = {exception ->
                        Log.e("ViewModel", "error updating hasAsked for user $userId: ${exception.message}")
                    }
                )

                //updateHasAnswered(crRoomId)
                //checkForUsersCompleteAnswers(crRoomId, gameInfo.title)
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to save pair answers ${e.message}")
            }
        }
    }
    private val _yourAnonQuestion = mutableStateOf<askQuestion?>(null)
    val yourAnonQuestion: State<askQuestion?> = _yourAnonQuestion
    private val _yourQuestion= mutableStateOf<askQuestion?>(null)
    val yourQuestion: State<askQuestion?> = _yourQuestion
    fun fetchToUserQuestion(crRoomId: String, userId: String, toUserId: String){
        viewModelScope.launch {
            Log.d("ViewModel", "Launching fetchToUserQuestion")
            // you are targeting
            try {
                val response = client.postgrest["askQuestion"]
                    .select(
                        filter = {
                            filter("userId", FilterOperator.EQ, userId)
                            filter("toUserId", FilterOperator.EQ, toUserId)
                            filter("crRoomId", FilterOperator.EQ, crRoomId)
                        }
                    )
                    .decodeSingleOrNull<askQuestion>()

                _yourAnonQuestion.value = response
                Log.d("ViewModel", "Successfully got question: $response from $userId to $toUserId")
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to check Users Question ${e.message}")
            }

        }
    }
    fun fetchTargetQuestion(crRoomId: String, userId: String){
        viewModelScope.launch {
            Log.d("ViewModel", "Launching fetchTargetQuestion")
            // you are targeted
            try {
                val response = client.postgrest["askQuestion"]
                    .select(
                        filter = {
                            filter("toUserId", FilterOperator.EQ, userId)
                            filter("crRoomId", FilterOperator.EQ, crRoomId)
                        }
                    )
                    .decodeSingleOrNull<askQuestion>()

                _yourQuestion.value = response
                Log.d("ViewModel", "Successfully got target question: $response for $userId")
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to get Users targeted Anon Question ${e.message}")
            }
        }
    }
    fun createUserPairs(users: List<UserProfile>): List<Pair<String, String>>{
        val shuffledUsers = users.shuffled()
        val pairs = mutableListOf<Pair<String, String>>()

        for (i in shuffledUsers.indices){
            val currentUser = shuffledUsers[i].userId
            val nextUser = shuffledUsers[(i + 1) % shuffledUsers.size].userId
            pairs.add(currentUser to nextUser)
        }
        return pairs
    }
    fun saveMysterCallerPairs(
        crRoomId: String,
        gameName: String,
        pairs: List<Pair<String, String>>,
        onSuccess: () -> Unit
    ){
        chatRepository.saveMysterCallerPairs(
            crRoomId = crRoomId,
            gameName = gameName,
            pairs = pairs,
            onSuccess = onSuccess,
            onError = { exception ->
                Log.d("ViewModel", "Error saving Mystery Caller pairs: ${exception.message}")
            }
        )
    }
    private val _userPairs = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val userPairs: StateFlow<List<Pair<String, String>>> = _userPairs
    fun fetchMysteryCallerPairs(
        crRoomId: String,
        gameName: String
    ){
        chatRepository.fetchMysteryCallerPairs(
            crRoomId = crRoomId,
            onComplete = { pairs ->
                         _userPairs.value = pairs
                Log.d("ViewModel", "Fetched $gameName pairs successfully: $pairs")
            },
            gameName = gameName,
            onError = {exception ->
                Log.e("ViewModel", "Error fetchign Myster Caller pairs: ${exception.message}")
            }
        )
    }

    fun fetchUserProfile(crRoomId: String, userId: String, onComplete: (UserProfile?) -> Unit){
        viewModelScope.launch {
            val userProfile = chatRepository.getSelectedUserProfile(crRoomId, userId)
            onComplete(userProfile)
        }
    }








    fun saveAnonAnswer(question: Answers){
        viewModelScope.launch {
            try {
                client.postgrest["answers"].insert(question)

                Log.d("ViewModel", "Anonymous answer saved succesffylly: $question")
            }catch (e: Exception){
                Log.d("ViewModel", "Error saving anon answer ${e.message}")
            }
        }
    }
    private val _hadAnsweredAnonQuestion = mutableStateOf(false)
    val hasAnsweredAnonQuestion: State<Boolean> = _hadAnsweredAnonQuestion
    fun fetchAnonAnswer(crRoomId: String, question: String){
        viewModelScope.launch {
            try {
                val response = client.postgrest["answers"]
                    .select(
                        filter = {
                            filter("userId", FilterOperator.EQ, userId)
                            filter("crRoomId", FilterOperator.EQ, crRoomId)
                            filter("question", FilterOperator.EQ, question)
                        }
                    )
                    .decodeList<Answers>()

                _hadAnsweredAnonQuestion.value = response.isNotEmpty()

            }catch (e: Exception){
                Log.d("ViewModel", "Error fetching anon answer ${e.message}")
            }
        }
    }











    private val _QnAState = MutableStateFlow<String>("")
    val QnAState: StateFlow<String> = _QnAState








}