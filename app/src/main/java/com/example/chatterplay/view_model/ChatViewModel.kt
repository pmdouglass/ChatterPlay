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
import com.example.chatterplay.data_class.RecordedAnswer
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.data_class.UserState
import com.example.chatterplay.repository.ChatRepository
import com.example.chatterplay.view_model.SupabaseClient.client
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
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
        install(Postgrest)
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
    private val _alternateUserProfile = MutableStateFlow<UserProfile?>(null)
    val alternateUserProfile: StateFlow<UserProfile?> get() = _alternateUserProfile
    private val _crUserProfile = MutableStateFlow<UserProfile?>(null)
    val crUserProfile: StateFlow<UserProfile?> get() = _crUserProfile
    private val _personalImage = mutableStateOf<String?>(null)
    val personalImage: State<String?> get() = _personalImage
    private val _alternateImage = mutableStateOf<String?>(null)
    val alternateImage: State<String?> get() = _alternateImage
    private val _imageUrl = mutableStateOf<String?>(null)
    val imageUrl: State<String?> = _imageUrl
    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading
    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> get() = _allUsers
    private val _allRisers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allRisers: StateFlow<List<UserProfile>> get() = _allRisers
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    private val _fetchedMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val fetchedMessages: StateFlow<List<ChatMessage>> = _fetchedMessages
    private val _allChatRoomMembers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allChatRoomMembers: StateFlow<List<UserProfile>> get() = _allChatRoomMembers
    private val _chatRoomMembersCount = MutableStateFlow<Int>(0)
    val chatRoomMembersCount: StateFlow<Int> get() = _chatRoomMembersCount
    private val _allChatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val allChatRooms: StateFlow<List<ChatRoom>>  = _allChatRooms
    private val _allRiserRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val allRiserRooms: StateFlow<List<ChatRoom>>  = _allRiserRooms
    private val _roomInfo = MutableStateFlow<ChatRoom?>(null)
    val roomInfo: StateFlow<ChatRoom?> get() = _roomInfo
    private val _usersStatus = MutableStateFlow<String?>("Unknown")
    val usersStatus: StateFlow<String?> = _usersStatus







    private var isUnreadMessageCountFetched = false
    private val _unreadMessageCount = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadMessageCount: StateFlow<Map<String, Int>> = _unreadMessageCount
    private val _unreadCRMessageCount = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadCRMessageCount: StateFlow<Map<String, Int>> = _unreadCRMessageCount





    init {
        viewModelScope.launch {
            getAllUsers()
            //getChatRoomsWithUnreadCount()
            fetchAllChatRooms()
            //fetchCRUnreadMessageCount()
            fetchUnreadMessageCount()
            fetchUsersStatus()
        }
    }



    fun saveUserProfile(userId: String, userProfile: UserProfile, game: Boolean){
      viewModelScope.launch {
          if (!game){
              chatRepository.saveUserProfile(userId = userId, userProfile = userProfile, game = false)
              _userProfile.value = userProfile
          }else {
              chatRepository.saveUserProfile(userId = userId, userProfile = userProfile,game = true)
              _alternateUserProfile.value = userProfile
          }

      }
    }

    suspend fun getUserProfile(userId: String){
        viewModelScope.launch {
            val profile = chatRepository.getUserProfile(userId, false)
            _userProfile.value = profile
            val crProfile = chatRepository.getUserProfile(userId, true)
            _alternateUserProfile.value = crProfile

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
                _alternateUserProfile.value = _alternateUserProfile.value?.copy(imageUrl = imageUrl)
                _alternateUserProfile.value?.let {
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
    suspend fun getAllRisers(crRoomId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val users = chatRepository.getAllRisers(crRoomId = crRoomId)
                val filterUsers = users.filter { it.userId != currentUser?.uid }
                _allRisers.value = filterUsers
                Log.d("Risers", "Fetched ${filterUsers.size} users")
            }catch (e: Exception){
                _userState.value = UserState.Error("Error fetching ${e.message}")
            }

        }
    }


    fun createAndInviteToChatRoom(crRoomId: String, memberIds: List<String>, roomName: String, onRoomCreated: (String) -> Unit){
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
            val allMemberIds = (memberIds + currentUser.uid).sorted()

            val existingRoomId = chatRepository.checkIfChatRoomExists(crRoomId = crRoomId, members = allMemberIds)

            if (existingRoomId != null){
                onRoomCreated(existingRoomId)
            } else {
                val roomId = chatRepository.createChatRoom(crRoomId = crRoomId, members = allMemberIds,roomName = roomName)
                memberIds.forEach { memberIds ->
                    chatRepository.addMemberToRoom(crRoomId = crRoomId, roomId = roomId,memberId = memberIds)
                }
                onRoomCreated(roomId)
            }
        }
    }















    fun fetchChatMessages(crRoomId: String, roomId: String, game: Boolean, mainChat: Boolean){
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val messages = chatRepository.getChatMessages(crRoomId = crRoomId, roomId = roomId, userId = userId, game = game, mainChat = mainChat)
            _messages.value = messages
        }
    }
    fun observeChatMessages(
        roomId: String,
        game: Boolean
    ){
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        chatRepository.observeChatMessages(
            userId = userId,
            roomId = roomId,
            game = game
        ) { messages ->
            _fetchedMessages.value = messages
        }
    }
    fun fetchChatRoomMembers(crRoomId: String, roomId: String, game: Boolean, mainChat: Boolean){
        val currentUser = FirebaseAuth.getInstance().currentUser
        viewModelScope.launch {
            val members = chatRepository.getChatRoomMembers(crRoomId = crRoomId, roomId = roomId, game = game, mainChat = mainChat)
            _allChatRoomMembers.value = members
            Log.d("riser", "members = $members")
            _userState.value = UserState.Success("Success fetching Members")
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
    private var isListenerAdded = false
    fun fetchSingleRoom(crRoomId: String, otherUserId: String, onResult: (String?) -> Unit){
        viewModelScope.launch {
            val roomId = chatRepository.checkIfSingleRoomExists(crRoomId, otherUserId)
            onResult(roomId)
            Log.d("riser", "viewmodel roomId is $roomId")
        }
    }
    fun fetchAllRiserRooms(crRoomId: String) {
        if (isListenerAdded) return
        isListenerAdded = true

        Log.d("Riser", "Attempting - fet all riser rooms")
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            chatRepository.getRiserRoom().document(crRoomId).collection("Private Chats")
                .whereArrayContains("members", user.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("ChatViewModel", "listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        Log.d("Riser", "snapshot is not null")
                        val rooms = snapshot.documents.map { document ->
                            document.toObject(ChatRoom::class.java)!!
                        }.filter { room ->
                            !room.hiddenFor.contains(user.uid)
                        }.sortedBy { it.lastMessageTimestamp }  // Sort in descending order
                        _allRiserRooms.value = rooms
                        Log.d("Riser", "Success - fetchallchatrooms")
                        Log.d("Riser", "Rooms found ${allRiserRooms.value.size}")

                        /*if (!isUnreadMessageCountFetched) {
                            fetchUnreadMessageCount()
                            isUnreadMessageCountFetched = true
                            Log.d("Test Message", "Success - !isunreadmessagecountfetched")

                        }*/
                    }
                }
        }


    }
    fun getRoomInfo(crRoomId: String, roomId: String){
        viewModelScope.launch {
            val roomInfo = chatRepository.getRoomInfo(crRoomId = crRoomId, roomId = roomId)
            _roomInfo.value = roomInfo
        }
    }
    fun sendMessage(crRoomId: String, roomId: String, message: String, game: Boolean, mainChat: Boolean){
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModelScope.launch {
            val userProfile = chatRepository.getProfileToSend(userId = currentUser, crRoomId = crRoomId, game = game)
            if (userProfile == null){
                Log.d("Debug-Message", "Failed to fetch user profile for userId: ${currentUser}")
                return@launch
            }
            val chatMessage = ChatMessage(
                senderId = userProfile.userId,
                senderName = userProfile.fname,
                message = message,
                image = userProfile.imageUrl
            )
            Log.d("Debug-Message", "Sending message: $chatMessage")
            chatRepository.sendMessage(crRoomId = crRoomId, roomId = roomId, chatMessage = chatMessage, game = game, mainChat = mainChat)
            fetchChatMessages(crRoomId, roomId = roomId, game = game, mainChat = mainChat)
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
    fun selectUploadAndGetImage(game: Boolean, userId: String, byteArray: ByteArray, onResult: (url: String?, error: String?) -> Unit){
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {

                val bucket = client.storage["ProfilePictures"]
                val fileName = if (!game) "Personal$userId" else "Alternate$userId"
                val path = if (!game) "Personal/$fileName.jpg" else "Alternate/$fileName.jpg"

                bucket.upload(path, byteArray, true)

                val publicUrl = bucket.publicUrl(path)

                _userState.value = UserState.Success("Image uploaded and URL retrieved")
                if (!game) {
                    _personalImage.value = publicUrl
                }else {
                    _alternateImage.value = publicUrl
                }
                onResult(publicUrl, null)
            }catch (e: Exception){
                _userState.value = UserState.Error("Error: ${e.message}")
                onResult(null, e.message)
            }
        }
    }

    fun fetchUsersStatus(){
        Log.d("ChatRise ViewModel", " fetching user Status")
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user->
            viewModelScope.launch {
                val status = chatRepository.getUsersStatus(user.uid)
                _usersStatus.value =  status ?: "Unknown"
            }
        }
    }
    /*fun fetchCRUnreadMessageCount() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            val room = chatRepository.getMainChatRoomForUser(currentUser.uid)
            room?.let {
                val unreadCounts = chatRepository.getUnreadMessageCount(it.roomId, currentUser.uid)
                _unreadCRMessageCount.value = unreadCounts
            }
        }
    }*/





}