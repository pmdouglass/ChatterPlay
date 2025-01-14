package com.example.chatterplay.view_model

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatterplay.data_class.Questions
import com.example.chatterplay.data_class.Answers
import com.example.chatterplay.data_class.GameTitle
import com.example.chatterplay.data_class.SupabaseClient.client
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.repository.ChatRiseRepository
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.FilterOperator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



class ChatRiseViewModel: ViewModel() {
    private val chatRepository = ChatRiseRepository()

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
    fun manualUpdateRankingStatus(crRoomId: String, userId: String, newStatus: String){
        viewModelScope.launch {
            chatRepository.updateUserRankingStatus(crRoomId = crRoomId, userId = userId, updatedStatus = newStatus)
            checkUserRankingStatus(crRoomId = crRoomId, userId = userId)
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
    private val _isDoneWithGame = mutableStateOf(false)
    val isDoneWithGame: State<Boolean> = _isDoneWithGame
    fun monitorUntilAllUsersDoneWithCurrentGame(crRoomId: String){
        viewModelScope.launch {
            try {
                while (true){
                    // check all users gameStatus
                    val statuses = chatRepository.fetchAllUsersGameStatus(crRoomId)

                    // check if all statuses are true
                    if (statuses.isNotEmpty() && statuses.all { it }){
                        Log.d("ViewModel", "All users are done with the current game")
                        _isDoneWithGame.value = true
                        break
                    }

                    // Delay to prevent tight loop (pooling every 5 seconds)
                    delay(5000)
                }
            }catch (e: Exception){
                Log.d("ViewModel", "Error monitoring games")
            }
        }
    }
    fun monitorUntilAllDone(crRoomId: String, currentUserId: String){
        viewModelScope.launch {
            try {
                while (true){
                    Log.d("ViewModel", "Searching for Done Members")

                    // check current rankingStatus
                    val currentUserStatus = chatRepository.fetchUserRankingStatus(crRoomId, currentUserId)
                    if (currentUserStatus == "View"){
                        Log.d("ViewModel", "User status == 'View'")
                        break
                    }

                    val usersSnapshot = chatRepository.getAllUsersInRoom(crRoomId)
                    val allUsersDone = usersSnapshot.documents.all { document ->
                        document.getString("rankingStatus") == "Done"
                    }
                    if (allUsersDone){
                        usersSnapshot.documents.forEach { document ->
                            val userId = document.id
                            chatRepository.updateUserRankingStatus(
                                crRoomId = crRoomId,
                                userId = userId,
                                updatedStatus = "View"
                            )
                        }
                        Log.d("ViewModel", "All Users set to View")
                        break
                    }
                    kotlinx.coroutines.delay(5000)
                }
            }catch (e: Exception){
                Log.d("ViewModel", "Error Monitoring until all done")
            }
        }
    }
    fun setAllToDone(crRoomId: String){
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
    fun fetchGameTitle(titleId: Int, onComplete: (String?) -> Unit){
        viewModelScope.launch {
            try {
                val response = client.postgrest["title"]
                    .select(
                        filter = {
                            filter("id", FilterOperator.EQ, titleId)
                        }
                    )
                    .decodeList<GameTitle>()

                val gameTitle = response.firstOrNull()?.title ?: "Unknown"
                Log.d("ViewModel", "Fetched game title $gameTitle")
                onComplete(gameTitle)
            }catch (e: Exception){
                Log.d("ViewModel", "Error fetching game title ${e.message}")
            }
        }
    }
    fun addOrUpdateGame(crRoomId: String, userIdToUpdate: String? = null, gameName: String, gamePlayedUpdate: Boolean? = null, doneStatusUpdate: Boolean? = null){
        viewModelScope.launch {
            try {
                chatRepository.addOrUpdateGame(
                    crRoomId = crRoomId,
                    gameName = gameName,
                    userId = userIdToUpdate,
                    gamePlayed = gamePlayedUpdate,
                    doneStatus = doneStatusUpdate
                )
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to addOrUpdateGame ${e.message}")
            }
        }
    }
    fun updateGameStatus(crRoomId: String, questionsComplete: Boolean){
        viewModelScope.launch {
            try {
                chatRepository.updateGameStatus(crRoomId, userId, questionsComplete)
                Log.d("ViewModel", "Updated gameStatus to $questionsComplete")
            }catch (e: Exception){
                Log.d("ViewModel", "failed to update game status to $questionsComplete")
            }
        }
    }
    private val _gameQuestions = MutableStateFlow<List<Questions>>(emptyList())
    val gameQuestion: StateFlow<List<Questions>> = _gameQuestions
    fun fetchQuestions(titleId: Int){
        viewModelScope.launch {
            try {
                val query = chatRepository.getAllQuestions()
                Log.d("ViewModel", "Fetched ${query.size} questions")
                _gameQuestions.value = query
            }catch (e: Exception){
                Log.d("ViewModel", "failed to fetch questions ${e.message}")
            }
        }
    }
    private val _isDoneAnswering = mutableStateOf(false)
    val isDoneAnswering: State<Boolean> = _isDoneAnswering
    suspend fun checkForAnswers(crRoomId: String, titleId: Int, userId: String): Boolean{
        return try {
            val response = client.postgrest["answers"]
                .select(
                    filter = {
                        filter("titleId", FilterOperator.EQ, titleId)
                        filter("userId", FilterOperator.EQ, userId)
                        filter("crRoomId", FilterOperator.EQ, crRoomId)
                    }
                )
                .decodeList<Answers>()

            val hasAnswered = response.isNotEmpty()
            _isDoneAnswering.value = hasAnswered
            hasAnswered
        }catch (e: Exception){
            Log.d("ViewModel", "Error checking user answers: ${e.message}")
            false
        }
    }
    fun savePairAnswers(crRoomId: String, answers: List<Answers>){
        viewModelScope.launch {
            try {
                answers.forEach { answer ->
                    val answerInsert = Answers(
                        crRoomId = crRoomId,
                        userId = answer.userId,
                        titleId = answer.titleId,
                        questionId = answer.questionId,
                        question = answer.question,
                        answerPair = answer.answerPair
                    )
                    client.postgrest["answers"].insert(answerInsert)
                    Log.d("ViewModel", "Saving answer: ${answer.question} -> ${answer.answerPair}")
                }


                // temerarliy update answers
                val titleId = answers.firstOrNull()?.titleId ?: return@launch
                checkForAnswers(crRoomId, titleId, userId)


            }catch (e: Exception){
                Log.d("ViewModel", "Failed to save answers ${e.message}")
            }
        }
    }
    @SuppressLint("SuspiciousIndentation")
    fun fetchPairAnswers(titleId: Int, onComplete: (List<Answers>) -> Unit){
        viewModelScope.launch {
            try {
            val response = client.postgrest["answers"]
                .select(
                    filter = {
                        filter("titleId", FilterOperator.EQ, titleId)
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