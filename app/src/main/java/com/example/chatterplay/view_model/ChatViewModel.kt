package com.example.chatterplay.view_model

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel: ViewModel() {

    private val profileRepository = ChatRepository()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> get() = _userProfile
    private val _crUserProfile = MutableStateFlow<UserProfile?>(null)
    val crUserProfile: StateFlow<UserProfile?> get() = _crUserProfile
    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> get() = _allUsers
    private val _alternateProfileCompletion = MutableStateFlow(false)
    val alternateProfileCompletion: StateFlow<Boolean> get() = _alternateProfileCompletion



    init {
        viewModelScope.launch {
            getUserProfile()
            GetAllUsers()
        }
    }



    fun saveUserProfile(userId: String, userProfile: UserProfile, game: Boolean){
      viewModelScope.launch {
          if (!game){
              profileRepository.saveUserProfile(userId = userId, userProfile = userProfile, game = false)
              _userProfile.value = userProfile
          }else {
              profileRepository.saveUserProfile(userId = userId, userProfile = userProfile,game = true)
              _crUserProfile.value = userProfile
          }

      }
    }

    suspend fun getUserProfile(){
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val profile = profileRepository.getUserProfile(currentUser, false)
            _userProfile.value = profile
            val crProfile = profileRepository.getUserProfile(currentUser, true)
            _crUserProfile.value = crProfile

        }
    }

    suspend fun uploadImage(userId: String, uri: Uri, fname: String, lname: String, game: Boolean): String {
        val imageUrl = profileRepository.uploadImage(userId, uri, game)
        if (imageUrl.isNotEmpty()) {
            if (!game) {
                _userProfile.value = _userProfile.value?.copy(imageUrl = imageUrl)
                _userProfile.value?.let {
                    saveUserProfile(userId = userId, userProfile = it, game =  false)
                }
            } else {
                _crUserProfile.value = _crUserProfile.value?.copy(imageUrl = imageUrl)
                _crUserProfile.value?.let {
                    saveUserProfile(userId = userId, userProfile = it, game =  true)
                }
            }
        }
        return imageUrl
    }

    private suspend fun GetAllUsers() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        viewModelScope.launch {
            val users = profileRepository.getAllUsers()
            val filterUsers = users.filter { it.userId != currentUser?.uid }
            _allUsers.value = filterUsers
            Log.d("ProfileViewModel", "Fetched ${filterUsers.size} users")
        }
    }

    fun createAndInviteToChatRoom(CRRoomId: String, memberIds: List<String>, roomName: String, onRoomCreated: (String) -> Unit){
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
            val allMemberIds = (memberIds + currentUser.uid).sorted()

            val existingRoomId = profileRepository.checkIfChatRoomExists(CRRoomId = CRRoomId, members = allMemberIds)

            if (existingRoomId != null){
                onRoomCreated(existingRoomId)
            } else {
                val roomId = profileRepository.createChatRoom(CRRoomId = CRRoomId, members = allMemberIds,roomName = roomName)
                memberIds.forEach { memberIds ->
                    profileRepository.addMemberToRoom(CRRoomId = CRRoomId, roomId = roomId,memberId = memberIds)
                }
                onRoomCreated(roomId)
            }
        }
    }



}