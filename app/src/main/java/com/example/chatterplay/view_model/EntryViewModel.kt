package com.example.chatterplay.view_model

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatterplay.repository.RoomCreateRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EntryViewModelFactory(
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomCreationViewModel::class.java)) {
            return RoomCreationViewModel(sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class RoomCreationViewModel(private val sharedPreferences: SharedPreferences): ViewModel(){


    val viewModel = ChatViewModel()

    private val userRepository = RoomCreateRepository(sharedPreferences)
    // User state flow ("NotPending", "Pending", "InGame")
    private val _userState = MutableStateFlow<String?>(null)
    val userStatus: StateFlow<String?> = _userState

    // Room readiness flow
    private val _roomReady = MutableStateFlow(false)
    val roomReady: StateFlow<Boolean> = _roomReady

    // Get crRoomId
    private val _crRoomId = MutableStateFlow<String?>(null)
    val crRoomId: StateFlow<String?> = _crRoomId

    // selected Profile
    private val _selectedProfile = MutableStateFlow<String?>("self")
    val selectedProfile: StateFlow<String?> = _selectedProfile

    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Initialize and fetch user state
    init {
        checkUserState()
        checkSelectedProfile()
        checkcrRoomId()
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
    private fun checkcrRoomId(){
        viewModelScope.launch {
            val usercrRoomId = userRepository.loadUserLocalcrRoomId(userId)
            if (usercrRoomId != null){
                _crRoomId.value = usercrRoomId
            }else {
                val status = userRepository.fetchcrRoomId(userId)
                _crRoomId.value = status ?: "0"
            }
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
                val roomSize = 4
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
                            // get roomID
                            val roomId = userRepository.getcrRoomId(userId)
                            if (roomId != null){
                                val addRoomId = userRepository.updateUsersGameRoomId(userIds = usersToUpdate, roomId = roomId)
                                // add users document
                                userRepository.createCRSelectedProfileUsers(crRoomId = roomId, userIds = usersToUpdate)
                                if (addRoomId){
                                    _userState.value = "InGame"
                                    _roomReady.value = true
                                }
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