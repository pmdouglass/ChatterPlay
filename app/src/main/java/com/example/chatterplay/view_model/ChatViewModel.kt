package com.example.chatterplay.view_model

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatterplay.BuildConfig
import com.example.chatterplay.data_class.ChatMessage
import com.example.chatterplay.data_class.ChatRoom
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.data_class.UserState
import com.example.chatterplay.repository.ChatRepository
import com.example.chatterplay.view_model.SupabaseClient.client
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(GoTrue)
        install(Storage)
    }
}


class ChatViewModel: ViewModel() {

    private val chatRepository = ChatRepository()


    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _userState = mutableStateOf<UserState>(UserState.Loading)
    val userState: State<UserState> = _userState

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> get() = _userProfile
    private val _crUserProfile = MutableStateFlow<UserProfile?>(null)
    val crUserProfile: StateFlow<UserProfile?> get() = _crUserProfile
    private val _imageUrl = mutableStateOf<String?>(null)
    val imageUrl: State<String?> = _imageUrl
    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading
    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> get() = _allUsers
    private val _alternateProfileCompletion = MutableStateFlow(false)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    private val _chatRoomMembers = MutableStateFlow<List<UserProfile>>(emptyList())
    val chatRoomMembers: StateFlow<List<UserProfile>> get() = _chatRoomMembers
    private val _chatRoomMembersCount = MutableStateFlow<Int>(0)
    val chatRoomMembersCount: StateFlow<Int> get() = _chatRoomMembersCount
    private val _allChatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val allChatRooms: StateFlow<List<ChatRoom>>  = _allChatRooms
    private val _roomInfo = MutableStateFlow<ChatRoom?>(null)
    val roomInfo: StateFlow<ChatRoom?> get() = _roomInfo
    private var isUnreadMessageCountFetched = false
    private val _unreadMessageCount = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadMessageCount: StateFlow<Map<String, Int>> = _unreadMessageCount





    init {
        viewModelScope.launch {
            getUserProfile()
            getAllUsers()
            //getChatRoomsWithUnreadCount()
            //fetchAllChatRooms() // needs Improvements
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
    fun fetchSingleChatRoomMemberCount(roomId: String) {
        Log.d("Test Message", "Attempting - Fetchedsinglechatroommembercount")
        viewModelScope.launch {
            val chatRoom = chatRepository.getSingleChatRoom(roomId)
            _chatRoomMembersCount.value = chatRoom?.members?.size ?: 0
            Log.d("Test Message", "Success - Fetchedsinglechatroommembercount")
        }
    }
    fun fetchUnreadMessageCount(){
        Log.d("Test Message", "Attempting - fetchunreadmessagecount")

        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            val counts = allChatRooms.value.associate { room ->
                val count = chatRepository.getUnreadMessageCount(room.roomId, currentUser.uid)
                room.roomId to count
            }
            _unreadMessageCount.value = counts
            Log.d("Test Message", "Success - fetchunreadmessagecount")

        }
    }
    private fun fetchAllChatRooms() {
        Log.d("Test Message", "Attempting - fetchallchatrooms")

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
                        Log.d("Test Message", "snapshot is not null")
                        val rooms = snapshot.documents.map { document ->
                            document.toObject(ChatRoom::class.java)!!
                        }.filter { room ->
                            !room.hiddenFor.contains(user.uid)
                        }.sortedBy { it.lastMessageTimestamp }  // Sort in descending order
                        _allChatRooms.value = rooms
                        Log.d("Test Message", "Success - fetchallchatrooms")

                        if (!isUnreadMessageCountFetched) {
                            fetchUnreadMessageCount()
                            isUnreadMessageCountFetched = true
                            Log.d("Test Message", "Success - !isunreadmessagecountfetched")

                        }
                    }
                }
        }
    }
    fun getRoomInfo(CRRoomId: String, roomId: String){
        viewModelScope.launch {
            val roomInfo = chatRepository.getRoomInfo(CRRoomId = CRRoomId, roomId = roomId)
            _roomInfo.value = roomInfo
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

    fun createBucket(name: String){
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                client.storage.createBucket(id = name){
                    public = true
                    fileSizeLimit = 10.megabytes
                }
                _userState.value = UserState.Success("Created bucket successfully!")
            }catch (e: Exception){
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }
    fun uploadImage(bucketName: String, fileName: String, byteArray: ByteArray){
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val bucket = client.storage[bucketName]
                bucket.upload("$fileName.jpg", byteArray, true)
                _userState.value = UserState.Success("Uploade Image Successful!")
            }catch (e: Exception){
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }
    fun readPublicFile(bucketName: String, fileName: String, onImageUrlRetrieved:(url: String) -> Unit){
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading
                val bucket = client.storage[bucketName]
                val url = bucket.publicUrl("$fileName.jpg")
                onImageUrlRetrieved(url)
                _userState.value = UserState.Success("File read successfully!")
            }catch (e: Exception){
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }
    fun getPublicUrl(fileName: String, bucketName: String): String {
        val bucket = client.storage.from(bucketName)
        return bucket.publicUrl(fileName)
    }
    fun selectUploadAndGet(fileName: String, byteArray: ByteArray, onResult: (url: String?, error: String?) -> Unit){
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val bucket = client.storage["photo"]

                bucket.upload("$fileName.jpg", byteArray, true)

                val publicUrl = bucket.publicUrl("$fileName.jpg")

                _userState.value = UserState.Success("Image uploaded and URL retrieved")
                onResult(publicUrl, null)
            }catch (e: Exception){
                _userState.value = UserState.Error("Error: ${e.message}")
                onResult(null, e.message)
            }
        }
    }





}