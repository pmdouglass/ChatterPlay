package com.example.chatterplay.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoomCreationViewModel: ViewModel(){

    // User state flow ("NotPending", "Pending", "InGame")
    private val _userState = MutableStateFlow("NotPending")
    val userState: StateFlow<String> = _userState

    // Room readiness flow
    private val _roomReady = MutableStateFlow(false)
    val roomReady: StateFlow<Boolean> = _roomReady

    // Simulating backend service
    private val backendService = BackendService()

    // Initialize and fetch user state
    init {
        checkUserState()
    }

    // Check the user's current state from the backend
    private fun checkUserState() {
        viewModelScope.launch {
            val state = backendService.fetchUserState()
            _userState.value = state
            if (state == "InGame") {
                _roomReady.value = true
            }
        }
    }

    // Update user state to "Pending" when the button is clicked
    fun setToPending() {
        if (_userState.value == "NotPending") {
            viewModelScope.launch {
                val success = backendService.updateUserState("Pending")
                if (success) {
                    _userState.value = "Pending"
                    monitorPendingUsers()
                }
            }
        }
    }

    // Monitor the backend for room readiness
    private fun monitorPendingUsers() {
        viewModelScope.launch {
            backendService.listenForRoomUpdates { isReady ->
                _roomReady.value = isReady
                if (isReady) {
                    _userState.value = "InGame"
                }
            }
        }
    }

    // Simulating backend service class
    class BackendService {
        suspend fun fetchUserState(): String {
            // Simulate backend fetch
            return "NotPending"
        }

        suspend fun updateUserState(newState: String): Boolean {
            // Simulate backend update
            return true
        }

        fun listenForRoomUpdates(onRoomReady: (Boolean) -> Unit) {
            // Simulate backend listener
            onRoomReady(true)
        }
    }
}