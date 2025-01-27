package com.example.chatterplay.view_model


import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatterplay.data_class.Questions
import com.example.chatterplay.data_class.Title
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.repository.ChatRiseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class cleanedup : ViewModel() {

    private val chatRepository = ChatRiseRepository()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /**
     * User Profile management
     */
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    // Ranking Status
    private val _rankingStatus = MutableStateFlow("View")
    val rankingStatus: StateFlow<String> = _rankingStatus

    /**
     * Questions Management
     */
    private val _gameQuestions = MutableStateFlow<List<Questions>>(emptyList())
    val gameQuestions: StateFlow<List<Questions>> = _gameQuestions

    private val _isDoneAnswering = mutableStateOf<Boolean?>(null)
    val isDoneAnswering: State<Boolean?> = _isDoneAnswering

    // Game Info
    private val _gameInfo = MutableStateFlow<Title?>(null)
    val gameInfo: StateFlow<Title?> = _gameInfo

    private val _isAllDoneWithQuestions = mutableStateOf(false)
    val isAllDoneWithQuestions: State<Boolean> = _isAllDoneWithQuestions

    // Ranking Data
    private val _rankedUsers = MutableStateFlow<List<Pair<UserProfile, Int>>>(emptyList())
    val rankedUsers: StateFlow<List<Pair<UserProfile, Int>>> = _rankedUsers

    // User Methods
    fun fetchUserProfile(crRoomId: String) {
        viewModelScope.launch {
            val profile = chatRepository.getUserProfile(crRoomId, userId)
            _userProfile.value = profile
        }
    }

    fun updateRankingStatus(crRoomId: String, newStatus: String) {
        viewModelScope.launch {
            chatRepository.updateUserRankingStatus(crRoomId, userId, newStatus)
            _rankingStatus.value = newStatus
        }
    }

    /**
     * Rankings Management
     */
    fun fetchAndSortRankings(crRoomId: String) {
        viewModelScope.launch {
            try {
                val rankings = chatRepository.getRanks(crRoomId)
                val rankedUsers = rankings.documents.mapNotNull { document ->
                    val userId = document.id
                    val points = document.getLong("totalPoints")?.toInt() ?: 0
                    val profile = chatRepository.getUserProfile(crRoomId, userId)
                    profile?.let { Pair(it, points) }
                }.sortedByDescending { it.second }
                _rankedUsers.value = rankedUsers
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching rankings: ${e.message}", e)
            }
        }
    }

    fun finalizeRankings(crRoomId: String) {
        viewModelScope.launch {
            try {
                val usersSnapshot = chatRepository.getAllUsers(crRoomId)
                val users = usersSnapshot.documents.map { it.id }

                users.forEach { userId ->
                    val rankingStatus = chatRepository.getUserRankingStatus(crRoomId, userId) ?: "View"
                    val hasVoted = rankingStatus == "Done"
                    chatRepository.updatePointsBasedOnVote(crRoomId, userId, hasVoted)

                    if (!hasVoted) {
                        users.filter { it != userId }.forEach { otherUserId ->
                            chatRepository.saveRanking(crRoomId, otherUserId, userId, 0)
                        }
                    }

                    chatRepository.updateUserRankingStatus(crRoomId, userId, "View")
                }

                _rankingStatus.value = "View"
            } catch (e: Exception) {
                Log.e("ViewModel", "Error finalizing rankings: ${e.message}", e)
            }
        }
    }

    /**
     * QuestionStatement Management
     */
    fun fetchQuestions(title: String) {
        viewModelScope.launch {
            try {
                val questions = chatRepository.getAllQuestions(title)
                _gameQuestions.value = questions
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching questions: ${e.message}", e)
            }
        }
    }

    fun assignQuestions(crRoomId: String, title: String, members: List<UserProfile>) {
        viewModelScope.launch {
            try {
                val questions = chatRepository.getAllQuestions(title)
                val assignedQuestions = chatRepository.getAllAssignedQuestionIds(crRoomId, title)
                val unassignedQuestions = questions.filter { it.id !in assignedQuestions }

                if (unassignedQuestions.size < members.size) {
                    throw IllegalArgumentException("Not enough questions for all members")
                }

                members.forEachIndexed { index, member ->
                    chatRepository.saveQuestionsToFirebase(
                        crRoomId = crRoomId,
                        title = title,
                        questionId = unassignedQuestions[index].id,
                        memberId = member.userId
                    )
                }

                Log.d("ViewModel", "Questions successfully assigned")
            } catch (e: Exception) {
                Log.e("ViewModel", "Error assigning questions: ${e.message}", e)
            }
        }
    }

    fun checkUserAnswers(crRoomId: String, title: String): Boolean {
        viewModelScope.launch {
            try {
                val answers = chatRepository.getAllAssignedQuestionIds(crRoomId, title)
                _isDoneAnswering.value = answers.isNotEmpty()
            } catch (e: Exception) {
                Log.e("ViewModel", "Error checking user answers: ${e.message}", e)
            }
        }
        return _isDoneAnswering.value ?: false
    }

    // Games
    fun fetchGameInfo(crRoomId: String) {
        viewModelScope.launch {
            try {
                val game = chatRepository.getGameInfo(crRoomId, userId)
                _gameInfo.value = game
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching game info: ${e.message}", e)
            }
        }
    }

    fun addGame(crRoomId: String, userIds: List<String>, gameInfo: Title) {
        viewModelScope.launch {
            try {
                chatRepository.saveGameNameToAllUsers(crRoomId, userIds, gameInfo)
                _gameInfo.value = gameInfo
            } catch (e: Exception) {
                Log.e("ViewModel", "Error adding game: ${e.message}", e)
            }
        }
    }

    fun deleteGame(crRoomId: String, userIds: List<String>, gameName: String) {
        viewModelScope.launch {
            try {
                chatRepository.resetGameNameFromAllUsers(crRoomId, userIds)
                _gameInfo.value = null
            } catch (e: Exception) {
                Log.e("ViewModel", "Error deleting game: ${e.message}", e)
            }
        }
    }
}
