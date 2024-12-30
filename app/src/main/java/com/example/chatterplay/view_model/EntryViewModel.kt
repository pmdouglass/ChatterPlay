package com.example.chatterplay.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.repository.ChatRepository
import com.example.chatterplay.repository.RoomCreateRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoomCreationViewModel: ViewModel(){


    val viewModel = ChatViewModel()

    private val userRepository = RoomCreateRepository()
    // User state flow ("NotPending", "Pending", "InGame")
    private val _userState = MutableStateFlow<String?>("NotPending")
    val userState: StateFlow<String?> = _userState

    // Room readiness flow
    private val _roomReady = MutableStateFlow(false)
    val roomReady: StateFlow<Boolean> = _roomReady

    // Get CRRoomId
    private val _CRRoomId = MutableStateFlow<String?>(null)
    val CRRoomId: StateFlow<String?> = _CRRoomId

    // selected Profile
    private val _selectedProfile = MutableStateFlow<String?>("self")
    val selectedProfile: StateFlow<String?> = _selectedProfile

    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Initialize and fetch user state
    init {
        checkUserState()
        checkSelectedProfile()
    }

    // Check the user's current state from the backend
    private fun checkUserState() {
        viewModelScope.launch {
            val status = userRepository.fetchUserProfile(userId)
            _userState.value = status ?: "NotPending"
            if (status == "InGame"){
                _roomReady.value = true
                monitorPendingUsers()
            }
        }
    }
    private fun checkSelectedProfile(){
        viewModelScope.launch {
            val status = userRepository.fetchUsersSelectedProfileStatus(userId)
            _selectedProfile.value = status ?: "self"
        }
    }

    // Update user state to "Pending" when the button is clicked
    fun setToPending() {
        if (_userState.value == "NotPending") {
            viewModelScope.launch {
                val success = userRepository.updateUserPendingState(userId, "Pending")
                if (success){
                    _userState.value = "Pending"
                    monitorPendingUsers()
                }
            }
        }
    }

    // Monitor the backend for room readiness
    private fun monitorPendingUsers() {
        viewModelScope.launch {
            while (_userState.value == "Pending"){
                val roomSize = 3
                val pendingUsers = userRepository.fetchUsersPendingState()
                if (pendingUsers.size >= roomSize){
                    val usersToUpdate = pendingUsers.take(roomSize)
                    val success = userRepository.updateUsersToInGame(usersToUpdate)
                    if (success){
                        val roomSuccess = userRepository.createNewCRRoom(
                            roomName = "ChatRise",
                            members = usersToUpdate
                        )
                        // Update users
                        if (roomSuccess){
                            _userState.value = "InGame"
                            _roomReady.value = true
                            // get roomID
                            val roomId = userRepository.getCRRoomId(userId)
                            if (roomId != null){
                                _CRRoomId.value = roomId
                                // add users
                                userRepository.createCRSelectedProfileUsers(CRRoomId = roomId, userIds = usersToUpdate)
                            }
                            break
                        }
                    }
                }
                kotlinx.coroutines.delay(2000)
            }
        }
    }



}