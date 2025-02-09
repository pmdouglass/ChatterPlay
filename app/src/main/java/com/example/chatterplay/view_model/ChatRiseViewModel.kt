package com.example.chatterplay.view_model

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.data_class.AlertType
import com.example.chatterplay.data_class.Answers
import com.example.chatterplay.data_class.ChatMessage
import com.example.chatterplay.data_class.Questions
import com.example.chatterplay.data_class.SupabaseClient.client
import com.example.chatterplay.data_class.Title
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.repository.ChatRiseRepository
import com.example.chatterplay.repository.RoomCreateRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.FilterOperator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatRiseViewModelFactory(
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatRiseViewModel::class.java)) {
            return ChatRiseViewModel(sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ChatRiseViewModel(private val sharedPreferences: SharedPreferences): ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val chatRepository = ChatRiseRepository(sharedPreferences)
    val entryRepository = RoomCreateRepository(sharedPreferences)
    private val crGameRoomsCollection = firestore.collection("ChatriseRooms")
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""


    /**
     *  Alert Management
     */
    private val _usersAlertType = MutableStateFlow<String?>(null)
    val usersAlertType: StateFlow<String?> = _usersAlertType



    fun loadUserLocalAlertType(userId: String){
        viewModelScope.launch {
            val alert = chatRepository.loadUserLocalAlertType(userId)
            _usersAlertType.value = alert
            Log.d("ChatRiseViewModel", "Loading $alert")
        }
    }
    fun checkforUserAlert(crRoomId: String){
        viewModelScope.launch {
            val usersAlertType = chatRepository.loadUserLocalAlertType(userId)
            val roomAlertType = chatRepository.getSystemAlertType(crRoomId)
            Log.d("ChatRiseViewModel", "currently: $usersAlertType new: $roomAlertType")
            if (usersAlertType != roomAlertType){
                // action
                _showAlert.value = true
                roomAlertType?.let { chatRepository.saveUserLocalAlertType(userId, it) }
                val alert = chatRepository.loadUserLocalAlertType(userId)
                _usersAlertType.value = alert
                Log.d("ChatRiseViewModel", "changed to new player")
            } else {
                Log.d("ChatRiseviewModel", "Same AlertType no action taken.")
            }

        }
    }
    fun checkForAlertChange(crRoomId: String) {
        viewModelScope.launch {
            try {
                val usersAlertType = chatRepository.loadUserLocalAlertType(userId)
                val roomAlertType = chatRepository.getSystemAlertType(crRoomId)

                Log.d("ChatRiseViewModel", "Checking for alert change...")
                Log.d("ChatRiseViewModel", "User's Alert Type: $usersAlertType")
                Log.d("ChatRiseViewModel", "Room's Alert Type: $roomAlertType")

                val hasAlert = usersAlertType != roomAlertType

                _alertChange.value = hasAlert

            } catch (e: Exception) {
                Log.e("ChatRiseViewModel", "Error in checkForAlertChange: ${e.message}", e)
            }
        }
    }
    fun updateAlertChangeToFalse(){
        _alertChange.value = false
    }




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
    fun blockSelectedMember(crRoomId: String, userId: String){
        viewModelScope.launch {
            chatRepository.blockSelectedPlayer(crRoomId, userId)
        }
    }

    private val _currentRank = MutableStateFlow<Int?>(null)
    val currentRank: StateFlow<Int?> = _currentRank

    fun getUserRank(crRoomId: String) {
        viewModelScope.launch {
            Log.d("ViewModel", "Fetching rank for user $userId in room $crRoomId")

            val rank = chatRepository.getCurrentRank(crRoomId, userId)

            if (rank != null) {
                Log.d("ViewModel", "User $userId current rank: $rank")
                _currentRank.value = rank // ✅ Update StateFlow with new rank
            } else {
                Log.d("ViewModel", "No rank found for user $userId")
                _currentRank.value = null // ✅ Ensure it's set to null if not found
            }
        }
    }









    /**
     *  Game Management
     */
    private val _isAllDoneWithQuestions = mutableStateOf(false)
    val isAllDoneWithQuestions: State<Boolean> = _isAllDoneWithQuestions
    private val _gameInfo = MutableStateFlow<Title?>(null)
    val gameInfo: StateFlow<Title?> = _gameInfo
    private val _alertChange = MutableStateFlow(false)
    val alertChange: StateFlow<Boolean> = _alertChange
    private val _showAlert = MutableStateFlow<Boolean?>(false)
    val showAlert: StateFlow<Boolean?> = _showAlert
    private val _gameQuestions = MutableStateFlow<List<Questions>>(emptyList())
    val gameQuestion: StateFlow<List<Questions>> = _gameQuestions
    private val _isDoneAnswering = mutableStateOf<Boolean?>(null)
    val isDoneAnswering: State<Boolean?> = _isDoneAnswering
    private val _isAllDoneAnswering = mutableStateOf<Boolean?>(null)
    val isAllDoneAnswering: State<Boolean?> = _isAllDoneAnswering

    private val _userAnswer = MutableStateFlow<Answers?>(null)
    val userAnswer: StateFlow<Answers?> = _userAnswer
    private val _currentQuestion = MutableStateFlow<Questions?>(null)
    val currentQuestion: StateFlow<Questions?> = _currentQuestion
    private val _systemAlertType = MutableStateFlow<String?>(null)
    val systemAlertType: StateFlow<String?> = _systemAlertType




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
    fun saveGame(crRoomId: String, userIds: List<String>, gameInfo: Title, allMembers: List<UserProfile>, context: Context){
        viewModelScope.launch {
            try {
                chatRepository.saveGameNameToRoom(crRoomId, gameInfo)
                chatRepository.saveGame(
                    crRoomId = crRoomId,
                    gameName = gameInfo.title,
                    allMembers = allMembers,
                    context = context
                )

                if (gameInfo.title == "Twisted Truths"){
                    saveAssignQuestionsStatement(
                        crRoomId = crRoomId,
                        title = gameInfo.title,
                        members = allMembers
                    )
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
    fun savePairAnswers(crRoomId: String, answers: List<Answers>, gameInfo: Title, context: Context){
        viewModelScope.launch {
            try {
                if (answers.isNotEmpty()){
                    client.postgrest["answers"].insert(answers)
                    Log.d("ViewModel", "Saved ${answers.size} answers")

                    answers.first().title
                    updateHasAnswered(crRoomId)
                    updateUsersHasAnswered(
                        crRoomId = crRoomId,
                        title = gameInfo.title,
                        context = context)
                    checkUserForAllCompleteAnswers(crRoomId, gameInfo.title, context)
                }
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to save pair answers ${e.message}")
            }
        }
    }
    fun saveQuestionStatement(crRoomId: String, answer: Answers, gameInfo: Title, context: Context){
        viewModelScope.launch {
            try {
                client.postgrest["answers"].insert(answer)
                Log.d("ViewModel", "Saved $answer")

                answer.title
                // update has answered
                // check for users completed answers
                //updateHasAnswered(crRoomId)
                checkUserForAllCompleteAnswers(crRoomId, gameInfo.title, context)
            }catch (e: Exception){
                Log.d("ViewModel", "Error saving questionStatement")
            }
        }
    }
    fun fetchGameInfo(crRoomId: String){
        viewModelScope.launch {
            try {
                val retrievedGameInfo = chatRepository.getGameInfo(crRoomId)
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
                //_usersGameAlertStatus.value = status
                when (status) {
                    true -> Log.d("ViewModel", "User $userId has been alerted")
                    false -> Log.d("ViewModel", "User $userId has not been alerted")
                }
            }catch (e: Exception){
                Log.d("ViewModel", "failed to get users game alert status ${e.message}")
            }
        }
    }
    fun fetchShowAlert(crRoomId: String){
        viewModelScope.launch {
            val status = chatRepository.getAlertStatus(crRoomId = crRoomId, userId = userId)
            _showAlert.value = status
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






    /*
    done answering - check and confirm then updatate has answered
    monitor all firebase users hasanswered
    if all true then
     */


    private val _userDoneAnswering = mutableStateOf<Boolean?>(null)
    val userDoneAnswering: State<Boolean?> = _userDoneAnswering
    private val _allMembersHasAnswered = mutableStateOf<Boolean?>(null)
    val allMembersHasAnswered: State<Boolean?> = _allMembersHasAnswered


    fun updateUsersHasAnswered(crRoomId: String, title: String, context: Context) {
        viewModelScope.launch {
            try {
                Log.d("ViewModel", "Starting updateUsersHasAnswered for crRoomId: $crRoomId, title: $title, userId: $userId")

                val response = if (title == "Twisted Truths") {
                    try {
                        Log.d("ViewModel", "Decoding single answer for title: $title")
                        client.postgrest["answers"]
                            .select(
                                filter = {
                                    filter("crRoomId", FilterOperator.EQ, crRoomId)
                                    filter("userId", FilterOperator.EQ, userId)
                                    filter("title", FilterOperator.EQ, title)
                                }
                            )
                            .decodeSingleOrNull<Answers>()?.let { listOf(it) } ?: emptyList()
                    } catch (e: Exception) {
                        Log.e("ViewModel", "Error decoding single answer: ${e.message}", e)
                        emptyList()
                    }
                } else {
                    try {
                        Log.d("ViewModel", "Decoding list of answers for title: $title")
                        client.postgrest["answers"]
                            .select(
                                filter = {
                                    filter("crRoomId", FilterOperator.EQ, crRoomId)
                                    filter("userId", FilterOperator.EQ, userId)
                                    filter("title", FilterOperator.EQ, title)
                                }
                            )
                            .decodeList<Answers>()
                    } catch (e: Exception) {
                        Log.e("ViewModel", "Error decoding list of answers: ${e.message}", e)
                        emptyList()
                    }
                }

                when {
                    response.isNotEmpty() -> {
                        Log.d("ViewModel", "Fetched ${response.size} answers for userId: $userId")
                        chatRepository.updateUsersHasAnswered(
                            crRoomId = crRoomId,
                            gameName = title,
                            userId = userId,
                            context = context
                        )
                        Log.d("ViewModel", "Successfully updated hasAnswered for userId: $userId in game: $title")
                        checkUsersHasAnswered(crRoomId, title, context)
                    }
                    else -> {
                        Log.d("ViewModel", "No answers found for userId: $userId in game: $title")
                    }
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching answers: ${e.message}", e)
            }
        }
    }

    fun checkUsersHasAnswered(crRoomId: String, title: String, context: Context) {
        viewModelScope.launch {
            try {
                Log.d("ViewModel", "Starting checkUsersHasAnswered for crRoomId: $crRoomId, title: $title, userId: $userId")

                val hasAnswered = chatRepository.checkUsersHasAnswered(
                    crRoomId = crRoomId,
                    gameName = title,
                    userId = userId
                )

                if (hasAnswered == true) {
                    _userDoneAnswering.value = hasAnswered
                    Log.d("ViewModel", "User $userId has already answered for game: $title in room: $crRoomId")
                } else {
                    _userDoneAnswering.value = hasAnswered
                    Log.d("ViewModel", "User $userId has not answered yet for game: $title in room: $crRoomId")
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error checking if user has answered: ${e.message}", e)
            }
        }
    }

    fun monitoringAllMembersHasAnswered(crRoomId: String, title: String, allMembers: List<UserProfile>, context: Context) {
        chatRepository.monitorAllMembersHasAnswered(
            crRoomId = crRoomId,
            gameName = title,
            allMembers = allMembers,
            onCheck = { allAnswered ->
                Log.d("ViewModel", "HasAnswered updates received: $allAnswered")

                if (allAnswered){
                    _allMembersHasAnswered.value = allAnswered
                    //updateSystemAlertType(crRoomId, AlertType.game_results, context)
                    Log.d("ChatRiseViewModel", "allAnswered: $allAnswered")
                    Log.d("ChatRiseViewModel", "All members have answered! Proceeding to the next step")
                } else {
                    Log.d("ChatRiseViewModel", "Not alll members have answered yet.")
                }
            },
            onError = { error ->
                Log.e("ViewModel", "Error monitoring all members' hasAnswered: ${error.message}", error)
                _allMembersHasAnswered.value = null // Reset to null on error
            }
        )

    }
    fun areAllMembersAnswered(crRoomId: String, title: String, context: Context){
        viewModelScope.launch {
            val response = chatRepository.areAllMembersAnswered(crRoomId, title)
            _allMembersHasAnswered.value = response
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
            }
        }
    }
    suspend fun checkUserForAllCompleteAnswers(crRoomId: String, title: String, context: Context): Boolean{
        return try {
            if (title.isEmpty()){
                Log.d("ViewModel", "title is empty; skipping check for completed answers.")
                _isAllDoneAnswering.value = null
                return false
            }

            // check if answers exist for this user in supabase
            Log.d("ViewModel", "Checking for completed answers")
            val response = client.postgrest["answers"]
                .select(
                    filter = {
                        filter("title", FilterOperator.EQ, title)
                        filter("crRoomId", FilterOperator.EQ, crRoomId)
                    }
                )
                .decodeList<Answers>()

            val answeredOrNot = response.isNotEmpty()

            if (answeredOrNot){
                //updateSystemAlertType(crRoomId = crRoomId, alertType = AlertType.game_results, context = context)
                Log.d("ViewModel", "all users has answered: $answeredOrNot - Updated firebase.")
            }else {
                Log.d("ViewModel", "All User has not answered: $answeredOrNot")
            }
            _isAllDoneAnswering.value = answeredOrNot
            Log.d("ViewModel", "isDoneAnswering set to: $answeredOrNot")
            answeredOrNot
        }catch (e: Exception){
            Log.d("ViewModel", "Error checking user answers: ${e.message}")
            _isAllDoneAnswering.value = null
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
                crGameRoomsCollection
                    .document(crRoomId)
                    .collection("Games")
                    .document(title)
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



    private fun updateHasAnswered(crRoomId: String){
        viewModelScope.launch {
            try {
                val success = chatRepository.updateHasAnswered(crRoomId, userId, true)
                if (success){
                    _isAllDoneAnswering.value = true
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
    fun updateSystemAlertType(crRoomId: String, alertType: AlertType, allMembers: List<UserProfile>, userId: String, context: Context){
        viewModelScope.launch {
            Log.d("ChatRiseViewModel", "Attempting to update AlertType to $alertType")
            try {
                chatRepository.updateSystemAlertType(crRoomId, alertType)
                Log.d("ChatriseViewModel", "Updating AlertType successfully changed to: $alertType")
                val newType = chatRepository.getSystemAlertType(crRoomId)
                when (newType){
                    //iytgbhkjlm
                    AlertType.new_player.string -> {
                        newPlayerInvite(crRoomId)
                    }
                    AlertType.game.string -> {
                        generateRandomGameInfo(crRoomId = crRoomId) {game ->
                            if (game != null){
                                CoroutineScope(Dispatchers.IO).launch {
                                    // collect information needed to save game
                                    val selectedGame = game
                                    val allMembers = chatRepository.getCRRoomMembers(crRoomId)
                                    val userIds: List<String> = allMembers.map { it.userId }
                                    Log.d("ChatriseViewModel", "All members: ${allMembers.map { it.userId }}")
                                    // saving game
                                    saveGame(
                                        crRoomId = crRoomId,
                                        userIds = userIds,
                                        gameInfo = selectedGame,
                                        allMembers = allMembers,
                                        context = context
                                    )
                                    Log.d("ChatriseViewModel", "game saved successfully.")

                                    /*
                                    chatRepository.updateAlertStatus(
                                        crRoomId = crRoomId,
                                        userId = userId,
                                        alertStatus = false
                                    )

                                     */

                                    try {
                                        // Log the event in Firebase Analytics
                                        val params = Bundle().apply {
                                            putString("cr_room_id", crRoomId)
                                            putString("game_name", game.title)
                                            putString("game_mode", game.mode)
                                        }
                                        AnalyticsManager.getInstance(context).logEvent("game_started", params)
                                        Log.d("ChatriseViewModel", "Game started event logged in Firebase Analytics.")
                                    }catch (e: Exception){
                                        Log.e("ChatriseViewModel", "Error logging game started event: ${e.message}")
                                    }
                                }
                            } else {
                                Log.d("ChatRiseViewModel", "No game was returned for generateRandomGameInfo, skipping addGame")
                            }
                        }
                    }
                    AlertType.ranking.string -> {
                        updateToRanking(crRoomId, userId, allMembers)
                    }
                    AlertType.rank_results.string -> {
                        setAllVotesToDone(crRoomId)
                        checkUserRankingStatus(crRoomId, userId)
                        val pair = chatRepository.getTopPlayers(crRoomId)
                        val topPlayers = listOfNotNull(pair?.first, pair?.second)
                        topPlayerDiscuss(
                            crRoomId = crRoomId,
                            memberIds = topPlayers
                        )
                    }
                    AlertType.blocking.string -> {
                        chatRepository.blockSelectedPlayer(crRoomId, userId)
                    }
                }
                _systemAlertType.value = newType
            }catch (e: Exception){
                Log.d("ViewModel", "failed to update AlertType: ${e.message}")
            }
        }
    }
    fun updateUsersAlertType(crRoomId: String, alertType: AlertType){
        viewModelScope.launch {
            Log.d("ChatRiseViewModel", "Attempting to update AlertType to $alertType")
            try {
                chatRepository.updateUsersAlertType(crRoomId, userId, alertType)
                Log.d("ChatriseViewModel", "Updating AlertType successfully changed to: $alertType")
            }catch (e: Exception){
                Log.d("ViewModel", "failed to update AlertType: ${e.message}")
            }
        }
    }

    fun updateShowAlert(crRoomId: String, alertStatus: Boolean){
        viewModelScope.launch {
            Log.d("ChatRiseViewModel", "Attempting to update AlertStatus to $alertStatus")
            try {
                //val status = chatRepository.updateAlertStatus(crRoomId, userId, alertStatus)
                //_showAlert.value = status
                _showAlert.value = alertStatus
                //_alertChange.value = alertStatus
                fetchShowAlert(crRoomId)
                Log.d("ChatriseViewModel", "Updating AlertStatus successfully changed to: $alertStatus")
            }catch (e: Exception){
                Log.d("ViewModel", "failed to update AlertStatus: ${e.message}")
            }
        }
    }

    fun fetchSystemAlertType(crRoomId: String){
        viewModelScope.launch {
            val alert = chatRepository.getSystemAlertType(crRoomId)
            _systemAlertType.value = alert
        }
    }
    fun fetchUserAlertType(crRoomId: String){
        viewModelScope.launch {
            chatRepository.getUsersAlertType(crRoomId, userId)
        }
    }
    /*
    fun alertTypeFlow(crRoomId: String): Flow<AlertType?> = flow {
        while (true){
            val type = chatRepository.getAlertType(crRoomId) // fetch the current alert type
            emit(type)
            delay(1000L) // Poll every second
        }
    }
    fun monitorAlertType(crRoomId: String, gameName: String, context: Context){
        // if alert type changes
        /*
        when alertype
        new_player -> introduce new player
        game -> create game
        game_results -> show results
        rank -> go to rankings
        rank_results -> show rank results
        blocking -> block player
        */


        // Keep track of previous type to detect changes
        var previousType: AlertType? = null

        viewModelScope.launch {
            try {
                // Collect alert type changes
                alertTypeFlow(crRoomId).collect {type ->

                    // trigger only when there is a change
                    if (type != null && type != previousType){

                        // Update the previous type
                        previousType = type

                        Log.d("ChatRiseViewModel", "AlertType change to $type")

                        when (type) {
                            AlertType.none -> {
                                Log.d("ChatRiseViewModel", "No alert to process")
                            }
                            AlertType.new_player -> {
                                Log.d("ChatRiseViewModel", "New player alert triggered.")

                            }
                            AlertType.game -> {
                                // look for had alert for current game
                                // if had alert = null then create game
                                // if had alert = false then send alert then switch to had alert = true
                                // if had alert = true then nothing
                                Log.d("ChatriseViewModel", "Game alert triggered. Checking for gameName.")
                                try {

                                    // Check user's game alert status
                                    if (gameName == ""){
                                        Log.d("ChatriseViewModel", "No gameName found. Generating random game.")
                                        generateRandomGameInfo(crRoomId){ game ->
                                            if (game != null){
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    val selectedGame = game
                                                    val allMembers = chatRepository.getCRRoomMembers(crRoomId)
                                                    val userIds: List<String> = allMembers.map { it.userId }
                                                    Log.d("ChatriseViewModel", "All members: ${allMembers.map { it.userId }}")
                                                    saveGame(
                                                        crRoomId = crRoomId,
                                                        userIds = userIds,
                                                        gameInfo = selectedGame,
                                                        allMembers = allMembers
                                                    )
                                                    Log.d("ChatriseViewModel", "game saved successfully.")

                                                    chatRepository.updateAlertStatus(
                                                        crRoomId = crRoomId,
                                                        userId = userId,
                                                        alertStatus = false
                                                    )

                                                    try {
                                                        // Log the event in Firebase Analytics
                                                        val params = Bundle().apply {
                                                            putString("cr_room_id", crRoomId)
                                                            putString("game_name", game.title)
                                                            putString("game_mode", game.mode)
                                                        }
                                                        AnalyticsManager.getInstance(context).logEvent("game_started", params)
                                                        Log.d("ChatriseViewModel", "Game started event logged in Firebase Analytics.")
                                                    }catch (e: Exception){
                                                        Log.e("ChatriseViewModel", "Error logging game started event: ${e.message}")
                                                    }
                                                }
                                            } else {
                                                Log.d("ChatRiseViewModel", "No game was returned for generateRandomGameInfo, skipping addGame")
                                            }
                                        }
                                    }else {
                                        val status = chatRepository.getAlertStatus(
                                            crRoomId = crRoomId,
                                            userId = userId
                                        )

                                        if (!status){
                                            if (_usersAlertStatus.value != status){
                                                Log.d("ChatRiseViewModel", "Updating usersAlertStatus to: $status for user: $userId")
                                                _usersAlertStatus.value = status
                                            }else {
                                                Log.d("ChatRiseViewModel", "usersAlertStatus is already $status. No update needed.")
                                            }
                                        }
                                    }
                                }catch (e: Exception){
                                    Log.e("ChatriseViewModel", "Error processing game alert: ${e.message}")
                                }
                            }
                            AlertType.game_result -> {
                                Log.d("ChatRiseViewModel", "game_result alert triggered.")

                            }
                            AlertType.ranking -> {
                                Log.d("ChatRiseViewModel", "ranking alert triggered.")

                            }
                            AlertType.rank_result -> {
                                Log.d("ChatRiseViewModel", "rank_result alert triggered.")

                            }
                            AlertType.blocking -> {
                                Log.d("ChatRiseViewModel", "blocking alert triggered.")

                            }
                            else -> {
                                Log.d("ChatRiseViewModel", "Unknown alert type: $type")
                            }
                        }
                    }
                }
            }catch (e: Exception){
                Log.e("ChatriseViewModel", "Error monitoring alert type: ${e.message}")
            }
        }
    }

     */
    fun resetGames(crRoomId: String, userIds: List<String>, gameName: String, context: Context){
        viewModelScope.launch {
            try {
                chatRepository.removeGameNameFromRoom(crRoomId)
                _isAllDoneWithQuestions.value = false
                /*
                chatRepository.saveOrUpdateGame(
                    crRoomId = crRoomId,
                    gameName = gameName,
                    allDone = true,
                    context = context
                )

                 */

                val retrievedGameInfo = chatRepository.getGameInfo(crRoomId)
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
    private val _userRankVote = MutableStateFlow<List<Pair<UserProfile, Int>>>(emptyList())
    val userRankVote: StateFlow<List<Pair<UserProfile, Int>>> = _userRankVote
    private val _rankedUsers = MutableStateFlow<List<Pair<UserProfile, Int>>>(emptyList())
    val rankedUsers: StateFlow<List<Pair<UserProfile, Int>>> = _rankedUsers
    fun saveRanking(crRoomId: String, memberId: String, userId: String, newPoints: Int){
        viewModelScope.launch {
            Log.d("ViewModel", "Saving ranking system")
            chatRepository.saveRanking(crRoomId, memberId, userId, newPoints)
            chatRepository.updateUserRankingStatus(crRoomId = crRoomId, userId = userId, updatedStatus = "Done")
            checkUserRankingStatus(crRoomId = crRoomId, userId = userId)
            fetchUserVote(crRoomId)
        }
    }
    fun fetchAndSortRankings(crRoomId: String){
        viewModelScope.launch {
            try {
                val rankingSnapshot = chatRepository.getAllRankDocuments(crRoomId)
                val userPointsList = rankingSnapshot.documents.mapNotNull { document ->
                    val userId = document.id
                    val totalPoints = document.getLong("totalPoints")?.toInt() ?: 0
                    val userProfile = chatRepository.getUserProfile(crRoomId, userId)
                    userProfile?.let { Pair(it, totalPoints) }
                }
                // Sort by decending
                val sortedUserPointsList = userPointsList.sortedByDescending { it.second }

                sortedUserPointsList.forEachIndexed { index, (userProfile, _) ->
                    val rank = index + 1
                    chatRepository.updateCurrentRank(crRoomId, userProfile.userId, rank)
                }

                // Extract top two players (if available)
                val topTwoPlayers = sortedUserPointsList.take(2).map { it.first.userId }

                val rank1 = topTwoPlayers.getOrNull(0) ?: ""
                val rank2 = topTwoPlayers.getOrNull(1) ?: ""

                Log.d("ChatRiseViewModel", "Top Two Players -> Rank 1: $rank1, Rank 2: $rank2")

                // Save top two players
                saveTopTwoPlayers(
                    crRoomId = crRoomId,
                    rank1 = rank1,
                    rank2 = rank2
                )
                _rankedUsers.value = sortedUserPointsList
            }catch (e: Exception){
                Log.d("ViewModel", "Error fetching and sorting")
            }
        }
    }

    fun fetchUserVote(crRoomId: String) {
        viewModelScope.launch {
            try {
                Log.d("ChatRiseViewModel", "Fetching rankings for room: $crRoomId")

                val rankingSnapshot = chatRepository.getAllRankDocuments(crRoomId)
                Log.d("ChatRiseViewModel", "Fetched ${rankingSnapshot.documents.size} ranking documents.")

                val userPointsList = rankingSnapshot.documents.mapNotNull { document ->
                    val memberId = document.id
                    val votes = document.get("votes") as? Map<String, Map<String, Any>> ?: emptyMap()

                    val pointsGiven = (votes[userId]?.get("pointsGiven") as? Long)?.toInt() ?: 0
                    Log.d("ChatRiseViewModel", "User $userId gave $pointsGiven points to $memberId")

                    if (pointsGiven > 0) {
                        val userProfile = chatRepository.getUserProfile(crRoomId, memberId)
                        if (userProfile != null) {
                            Log.d("ChatRiseViewModel", "Retrieved UserProfile for $memberId: ${userProfile.fname}")
                            Pair(userProfile, pointsGiven)
                        } else {
                            Log.w("ChatRiseViewModel", "UserProfile not found for $memberId")
                            null
                        }
                    } else {
                        Log.d("ChatRiseViewModel", "Skipping $memberId as pointsGiven is 0")
                        null
                    }
                }

                // Sort results before updating the state
                val sortedUserPointsList = userPointsList.sortedByDescending { it.second }
                Log.d("ChatRiseViewModel", "Sorted user votes: $sortedUserPointsList")

                // Update StateFlow with the sorted list
                _userRankVote.value = sortedUserPointsList
                Log.d("ChatRiseViewModel", "Updated _userRankVote successfully.")

            } catch (e: Exception) {
                Log.e("ChatRiseViewModel", "Error fetching user vote rankings", e)
            }
        }
    }


    fun checkUserRankingStatus(crRoomId: String, userId: String){
        viewModelScope.launch {
            val status = chatRepository.getUserRankingStatus(crRoomId, userId)
            _rankingStatus.value = status ?: "View"
        }
    }
    fun updateToRanking(crRoomId: String, userId: String, allMembers: List<UserProfile>){
        viewModelScope.launch {
            allMembers.forEach { member ->
                chatRepository.updateUserRankingStatus(crRoomId, member.userId, "Ranking")
            }
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

    /**
     * New Player Management
     */
    fun newPlayerInvite(crRoomId: String) {
        viewModelScope.launch {
            try {
                Log.d("EntryViewModel", "Fetching users in Pending state...")

                val pendingUsers = entryRepository.fetchUsersPendingState()
                Log.d("EntryViewModel", "Fetched ${pendingUsers.size} pending users.")

                if (pendingUsers.isNotEmpty()) {
                    val pickedUser = pendingUsers.take(1) // Take one user
                    Log.d("EntryViewModel", "Picked user for invite: $pickedUser")

                    val success = entryRepository.updateUsersToInGame(pickedUser)
                    if (success) {
                        Log.d("EntryViewModel", "User successfully updated to InGame.")

                        entryRepository.updateUsersGameRoomId(userIds = pickedUser, roomId = crRoomId)
                        Log.d("EntryViewModel", "User's Game Room ID updated to $crRoomId.")

                        entryRepository.createCRSelectedProfileUsers(crRoomId = crRoomId, userIds = pickedUser)
                        Log.d("EntryViewModel", "CR Selected Profile Users created for room: $crRoomId.")
                    } else {
                        Log.e("EntryViewModel", "Error updating user to InGame.")
                    }
                } else {
                    Log.d("EntryViewModel", "No Pending Users found.")
                }
            } catch (e: Exception) {
                Log.e("EntryViewModel", "Error in newPlayerInvite: ${e.message}", e)
            }
        }
    }


    /**
     * Blocking Management
     */












    /**
     *  Top Player Mangement
     */

    private val _topPlayers = MutableStateFlow<Pair<String?, String?>?>(null)
    val topPlayers: StateFlow<Pair<String?, String?>?> = _topPlayers
    private val _currentUsersSelection = MutableStateFlow<UserProfile?>(null)
    val currentUsersSelection: StateFlow<UserProfile?> = _currentUsersSelection
    private val _otherUsersSelection = MutableStateFlow<UserProfile?>(null)
    val otherUsersSelection: StateFlow<UserProfile?> = _otherUsersSelection
    private val _currentUserTradeStatus = MutableStateFlow<String>("")
    val currentUserTradeStatus: StateFlow<String> get() = _currentUserTradeStatus
    private val _otherUserTradeStatus = MutableStateFlow("")
    val otherUserTradeStatus: StateFlow<String> get() = _otherUserTradeStatus
    private val _topPlayerMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val topPlayerMessages: StateFlow<List<ChatMessage>> = _topPlayerMessages



    fun topPlayerDiscuss(crRoomId: String, memberIds: List<String>){
        viewModelScope.launch {
            val allMemberIds = (memberIds).sorted()
            val roomName = "Leader Discussion"
            val existingRoomId = chatRepository.checkIfTopPlayerRoomExist(crRoomId, allMemberIds)

            if (existingRoomId != null){

            } else {
                val roomId = chatRepository.createTopPlayerChatRoom(crRoomId, allMemberIds, roomName)
                memberIds.forEach { memberIds ->
                    chatRepository.addMemberToTopPlayerRoom(crRoomId, roomId, memberIds)
                }
            }
        }
    }
    fun sendTopPlayerMessage(crRoomId: String, roomId: String, message: String){
        viewModelScope.launch {
            val userProfile = chatRepository.getUserProfile(crRoomId, userId)
            if (userProfile != null) {
                val chatMessage = ChatMessage(
                    senderId = userProfile.userId,
                    senderName = userProfile.fname,
                    message = message,
                    image = userProfile.imageUrl
                )
                chatRepository.sendTopPlayerMessage(crRoomId, roomId, chatMessage)
                fetchTopPlayerChatMessages(crRoomId, roomId)
            }
        }
    }
    fun sendGoodbyeMessage(crRoomId: String, roomId: String, message: String, remove: String){
        viewModelScope.launch {
            val userProfile = chatRepository.getUserProfile(crRoomId = crRoomId, userId)
            if (userProfile != null) {
                val chatMessage = ChatMessage(
                    senderId = userProfile.userId,
                    senderName = userProfile.fname,
                    message = message,
                    image = userProfile.imageUrl
                )
                chatRepository.sendGoodbyeMessage(crRoomId, roomId, chatMessage, remove)

                checkUserRemoved(crRoomId)
            }
        }
    }
    private val _removedPlayer = MutableStateFlow(false)
    val removedPlayer: StateFlow<Boolean> = _removedPlayer

    fun checkUserRemoved(CRRoomId: String){
        viewModelScope.launch {
            _removedPlayer.value = chatRepository.isRemovedSet(CRRoomId)
        }
    }

    fun fetchTopPlayerChatMessages(CRRoomId: String, roomId: String){
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val messages = chatRepository.getTopPlayerChatMessages(CRRoomId, roomId, userId)
            _topPlayerMessages.value = messages
        }
    }
    fun getUsersSelection(CRRoomId: String, UserId: String, isCurrentUser: Boolean) {
        viewModelScope.launch {
            try {
                Log.d("ChatRiseViewModel", "Fetching user selection for CRRoomId: $CRRoomId, UserId: $UserId, isCurrentUser: $isCurrentUser")

                val targetStateFlow = if (isCurrentUser) _currentUsersSelection else _otherUsersSelection

                chatRepository.getUsersSelection(CRRoomId, UserId).collect { selection ->
                    Log.d("ChatRiseViewModel", "Received selection for UserId: $UserId -> $selection")

                    targetStateFlow.value = selection

                    Log.d("ChatRiseViewModel", "Updated ${if (isCurrentUser) "_currentUsersSelection" else "_otherUsersSelection"} with: $selection")
                }
            } catch (e: Exception) {
                Log.e("ChatRiseViewModel", "Error fetching user selection for UserId: $UserId in CRRoomId: $CRRoomId", e)
            }
        }
    }

    fun checkTradeStatus(CRRoomId: String, otherUserId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModelScope.launch {
            val currentUserIdStatus = chatRepository.checkTradeStatus(CRRoomId, userId)
            _currentUserTradeStatus.value = currentUserIdStatus
            val otherUserIdStatus = chatRepository.checkTradeStatus(CRRoomId, otherUserId)
            _otherUserTradeStatus.value = otherUserIdStatus
        }
    }
    fun updateTradeStatus(CRRoomId: String, otherUserId: String){
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModelScope.launch {
            chatRepository.UpdateTradeStatus(CRRoomId, userId, otherUserId)
            val currentUserIdStatus = chatRepository.checkTradeStatus(CRRoomId, userId)
            _currentUserTradeStatus.value = currentUserIdStatus
            val otherUserIdStatus = chatRepository.checkTradeStatus(CRRoomId, otherUserId)
            _otherUserTradeStatus.value = otherUserIdStatus
        }
    }
    fun saveCurrentUsersSelection(CRRoomId: String, currentUserId: String, selectedPlayer: String){
        viewModelScope.launch {
            if (selectedPlayer.isNullOrEmpty()){
                chatRepository.saveCurrentUsersSelection(CRRoomId, currentUserId, "")
            } else{
                chatRepository.saveCurrentUsersSelection(CRRoomId, currentUserId, selectedPlayer)
            }
        }
    }
    fun cancelTradeStatus(CRRoomId: String){
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModelScope.launch {
            chatRepository.cancelTradeStatus(CRRoomId, userId)
            checkTradeStatus(CRRoomId, userId)
        }
    }
    fun saveTopTwoPlayers(crRoomId: String, rank1: String, rank2: String){
        viewModelScope.launch {
            chatRepository.saveTopPlayers(crRoomId, rank1, rank2)
        }
    }
    fun getTopPlayers(crRoomId: String){
        viewModelScope.launch {
            val result = chatRepository.getTopPlayers(crRoomId)
            Log.d("ChatRiseViewModel", "Top Players $result")
            _topPlayers.value = result

        }
    }
    private val _topPlayerRoomId = MutableStateFlow<String?>(null)
    val topPlayerRoomId: StateFlow<String?> = _topPlayerRoomId

    fun fetchTopPlayerRoomId(crRoomId: String) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Fetching Top Player Room ID for crRoomId: $crRoomId")

                val roomId = chatRepository.getTopPlayerRoomId(crRoomId, userId)
                _topPlayerRoomId.value = roomId

                if (roomId != null) {
                    Log.d("ChatViewModel", "Top Player Room ID found: $roomId")
                } else {
                    Log.w("ChatViewModel", "No valid Top Player Room ID found.")
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching Top Player Room ID", e)
            }
        }
    }
    private val _userSelections = MutableStateFlow<Map<String, String>>(emptyMap())
    val userSelections: StateFlow<Map<String, String>> = _userSelections

    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> = _result
    fun userSelectionPick(crRoomId: String, pick: String){
        viewModelScope.launch {
            chatRepository.userSelectionPick(crRoomId, userId, pick)
            evaluateDecision(crRoomId)
        }
    }
    fun evaluateDecision(crRoomId: String) {
        viewModelScope.launch {
            val selections = chatRepository.getUserPickSelection(crRoomId)

            if (selections == null || selections.size < 2) return@launch // Wait until both users have selected

            _userSelections.value = selections

            val userIds = selections.keys.toList()
            val user1 = userIds[0]
            val user2 = userIds[1]

            val choice1 = selections[user1] ?: return@launch
            val choice2 = selections[user2] ?: return@launch

            _result.value = when {
                choice1 == "me" && choice2 == "you" -> user1
                choice1 == "you" && choice2 == "me" -> user2
                choice1 == "me" && choice2 == "me" -> listOf(user1, user2).random()
                choice1 == "you" && choice2 == "you" -> listOf(user1, user2).random()
                choice1 == "me" && choice2 == "random" -> user1
                choice1 == "random" && choice2 == "me" -> user2
                choice1 == "you" && choice2 == "random" -> user1
                choice1 == "random" && choice2 == "you" -> user2
                choice1 == "random" && choice2 == "random" -> listOf(user1, user2).random()
                else -> null
            }
        }
    }
    private val _hasUserSelected = MutableStateFlow(false)
    val hasUserSelected: StateFlow<Boolean?> = _hasUserSelected

    fun checkIfUserSelected(crRoomId: String){
        viewModelScope.launch {
            val selection = chatRepository.getUserPickSelection(crRoomId)
            _hasUserSelected.value = selection?.containsKey(userId) == true
        }
    }
    private val _goodbyeMessage = MutableStateFlow<ChatMessage?>(null)
    val goodbyeMessage: StateFlow<ChatMessage?> = _goodbyeMessage
    fun fetchGoodbyeMessage(crRoomId: String, roomId: String){
        viewModelScope.launch {
            val message = chatRepository.getGoodbyeMessage(crRoomId, roomId)
            _goodbyeMessage.value = message
        }
    }
    fun hasGoodbyeMessage(): Boolean{
        return _goodbyeMessage.value != null
    }

}