package com.example.chatterplay.view_model

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatterplay.data_class.ChatMessage
import com.example.chatterplay.data_class.ChatRoom
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel: ViewModel() {

    private val chatRepository = ChatRepository()


    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> get() = _userProfile
    private val _crUserProfile = MutableStateFlow<UserProfile?>(null)
    val crUserProfile: StateFlow<UserProfile?> get() = _crUserProfile
    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> get() = _allUsers
    private val _alternateProfileCompletion = MutableStateFlow(false)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    private val _chatRoomMembers = MutableStateFlow<List<UserProfile>>(emptyList())
    val chatRoomMembers: StateFlow<List<UserProfile>> get() = _chatRoomMembers
    private val _chatRoomMembersCount = MutableStateFlow<Int>(0)
    val chatRoomMembersCount: StateFlow<Int> get() = _chatRoomMembersCount
    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>>  = _chatRooms
    private var isUnreadMessageCountFetched = false
    private val _unreadMessageCount = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadMessageCount: StateFlow<Map<String, Int>> = _unreadMessageCount





    init {
        viewModelScope.launch {
            getUserProfile()
            getAllUsers()
            //getChatRoomsWithUnreadCount()
            //getChatrooms()
        }
    }



    fun saveUserProfile(userId: String, userProfile: UserProfile, game: Boolean){
      viewModelScope.launch {
          if (!game){
              chatRepository.saveUserProfile(userId = userId, userProfile = userProfile, game = false)
              _userProfile.value = userProfile
          }else {
              chatRepository.saveUserProfile(userId = userId, userProfile = userProfile,game = true)
              _crUserProfile.value = userProfile
          }

      }
    }

    suspend fun getUserProfile(){
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val profile = chatRepository.getUserProfile(currentUser, false)
            _userProfile.value = profile
            val crProfile = chatRepository.getUserProfile(currentUser, true)
            _crUserProfile.value = crProfile

        }
    }

    suspend fun uploadImage(userId: String, uri: Uri, fname: String, lname: String, game: Boolean): String {
        val imageUrl = chatRepository.uploadImage(userId, uri, game)
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

    private suspend fun getAllUsers() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        viewModelScope.launch {
            val users = chatRepository.getAllUsers()
            val filterUsers = users.filter { it.userId != currentUser?.uid }
            _allUsers.value = filterUsers
            Log.d("ProfileViewModel", "Fetched ${filterUsers.size} users")
        }
    }

    fun createAndInviteToChatRoom(CRRoomId: String, memberIds: List<String>, roomName: String, onRoomCreated: (String) -> Unit){
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
            val allMemberIds = (memberIds + currentUser.uid).sorted()

            val existingRoomId = chatRepository.checkIfChatRoomExists(CRRoomId = CRRoomId, members = allMemberIds)

            if (existingRoomId != null){
                onRoomCreated(existingRoomId)
            } else {
                val roomId = chatRepository.createChatRoom(CRRoomId = CRRoomId, members = allMemberIds,roomName = roomName)
                memberIds.forEach { memberIds ->
                    chatRepository.addMemberToRoom(CRRoomId = CRRoomId, roomId = roomId,memberId = memberIds)
                }
                onRoomCreated(roomId)
            }
        }
    }
    fun fetchChatMessages(roomId: String){
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val messages = chatRepository.getChatMessages(roomId, userId)
            _messages.value = messages
        }
    }
    fun fetchChatRoomMembers(roomId: String){
        val currentUser = FirebaseAuth.getInstance().currentUser
        viewModelScope.launch {
            val members = chatRepository.getChatRoomMembers(roomId).filter { it.userId != currentUser?.uid }
            _chatRoomMembers.value = members
        }
    }
    fun fetchChatRoomMemberCount(roomId: String) {
        viewModelScope.launch {
            val chatRoom = chatRepository.getChatRoom(roomId)
            _chatRoomMembersCount.value = chatRoom?.members?.size ?: 0
        }
    }
    fun fetchUnreadMessageCount(){
        Log.d("Time", "Inside Model unread count")
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            val counts = chatRooms.value.associate { room ->
                val count = chatRepository.getUnreadMessageCount(room.roomId, currentUser.uid)
                room.roomId to count
            }
            _unreadMessageCount.value = counts
        }
    }
    private fun fetchChatRooms() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            chatRepository.getChatRooms()
                .whereArrayContains("members", user.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("ChatViewModel", "listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val rooms = snapshot.documents.map { document ->
                            document.toObject(ChatRoom::class.java)!!
                        }.filter { room ->
                            !room.hiddenFor.contains(user.uid)
                        }.sortedBy { it.lastMessageTimestamp }  // Sort in descending order
                        _chatRooms.value = rooms
                        if (!isUnreadMessageCountFetched) {
                            fetchUnreadMessageCount()
                            isUnreadMessageCountFetched = true
                        }
                    }
                }
        }
    }
    fun sendMessage(roomId: String, message: String, game: Boolean){
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            val userProfile = chatRepository.getUserProfile(userId = currentUser.uid, game = game)
            if (userProfile != null) {
                val chatMessage = ChatMessage(
                    senderId = userProfile.userId,
                    senderName = userProfile.fname,
                    message = message,
                    image = userProfile.imageUrl
                )
                chatRepository.sendMessage(roomId, chatMessage)
                fetchChatMessages(roomId)
            }
        }
    }



}