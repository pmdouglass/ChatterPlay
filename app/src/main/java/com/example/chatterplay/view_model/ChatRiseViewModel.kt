package com.example.chatterplay.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.repository.ChatRiseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatRiseViewModel: ViewModel() {
    private val chatRepository = ChatRiseRepository()

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile



    suspend fun getUserProfile(crRoomId: String){
        viewModelScope.launch {
            val profile = chatRepository.getUserProfile(crRoomId, userId)
            _userProfile.value = profile
        }
    }
}