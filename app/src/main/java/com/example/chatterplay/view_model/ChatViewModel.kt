package com.example.chatterplay.view_model

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatterplay.analytics.AnalyticsManager
import com.example.chatterplay.data_class.ChatMessage
import com.example.chatterplay.data_class.ChatRoom
import com.example.chatterplay.data_class.SupabaseClient.client
import com.example.chatterplay.data_class.UserProfile
import com.example.chatterplay.data_class.UserState
import com.example.chatterplay.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID


class ChatViewModel: ViewModel() {

    private val chatRepository = ChatRepository()

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""


    private val _userState = mutableStateOf<UserState>(UserState.Loading)
    val userState: State<UserState> = _userState
    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading


    init {
        viewModelScope.launch {
            fetchAllUsers()
            //getChatRoomsWithUnreadCount()
            fetchAllChatRooms()
            //fetchCRUnreadMessageCount()
            fetchUnreadMessageCount()
            fetchUsersStatus()
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


    /**
     * User Management
     */

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> get() = _userProfile
    private val _alternateUserProfile = MutableStateFlow<UserProfile?>(null)
    val alternateUserProfile: StateFlow<UserProfile?> get() = _alternateUserProfile
    private val _personalImage = mutableStateOf<String?>(null)
    val personalImage: State<String?> get() = _personalImage
    private val _alternateImage = mutableStateOf<String?>(null)
    val alternateImage: State<String?> get() = _alternateImage
    private val _imageUrl = mutableStateOf<String?>(null)
    val imageUrl: State<String?> = _imageUrl
    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> get() = _allUsers
    private val _allRisers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allRisers: StateFlow<List<UserProfile>> get() = _allRisers
    private val _usersStatus = MutableStateFlow<String?>("Unknown")
    val usersStatus: StateFlow<String?> = _usersStatus




    fun saveUserProfile(context: Context, userId: String, userProfile: UserProfile, game: Boolean) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Attempting to save user profile for userId: $userId, game: $game")

                if (!game) {
                    chatRepository.saveUserProfile(userId = userId, userProfile = userProfile, game = false)
                    _userProfile.value = userProfile
                    Log.d("ChatViewModel", "User profile successfully saved for userId: $userId (Personal Profile)")

                    // Log the event in Firebase Analytics
                    val params = Bundle().apply {
                        putString("userId", userId)
                        putString("age", userProfile.age)
                        putString("location", userProfile.location)
                        putString("gender", userProfile.gender)
                    }
                    AnalyticsManager.getInstance(context).logEvent("user_profile", params)
                } else {
                    chatRepository.saveUserProfile(userId = userId, userProfile = userProfile, game = true)
                    _alternateUserProfile.value = userProfile
                    Log.d("ChatViewModel", "User profile successfully saved for userId: $userId (Game Profile)")
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error saving user profile for userId: $userId - ${e.message}", e)
            }
        }
    }

    suspend fun getRealUserProfile(userId: String): UserProfile? {
        return try {
            val profile = chatRepository.getRealUserProfile(userId)
            profile
        } catch (e: Exception) {
            Log.e("ChatRiseViewModel", "Error fetching user profile for $userId", e)
            null // Return null in case of failure
        }
    }

    suspend fun fetchUserProfile(userId: String): UserProfile? {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Fetching user profile for userId: $userId")

                // Fetch personal profile
                val profile = chatRepository.getUserProfile(userId, false)
                _userProfile.value = profile
                if (profile != null) {
                    Log.d("ChatViewModel", "Successfully fetched personal profile for userId: $userId")
                } else {
                    Log.d("ChatViewModel", "No personal profile found for userId: $userId")
                }

                // Fetch game profile
                val crProfile = chatRepository.getUserProfile(userId, true)
                _alternateUserProfile.value = crProfile
                if (crProfile != null) {
                    Log.d("ChatViewModel", "Successfully fetched game profile for userId: $userId")
                } else {
                    Log.d("ChatViewModel", "No game profile found for userId: $userId")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching user profile for userId: $userId - ${e.message}", e)
            }
        }
        return null
    }

    private suspend fun fetchAllUsers() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Fetching all users, excluding current user: ${currentUser?.uid}")

                // Fetch all users from the repository
                val users = chatRepository.getAllUsers()
                val filterUsers = users.filter { it.userId != currentUser?.uid }
                _allUsers.value = filterUsers

                Log.d("ChatViewModel", "Successfully fetched ${filterUsers.size} users")

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching all users - ${e.message}", e)
            }
        }
    }

    suspend fun fetchAllRisers(crRoomId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                Log.d("ChatViewModel", "Fetching all risers for crRoomId: $crRoomId, excluding current user: ${currentUser?.uid}")

                // Fetch risers from the repository
                val users = chatRepository.getAllRisers(crRoomId = crRoomId)
                val filterUsers = users.filter { it.userId != currentUser?.uid }
                _allRisers.value = filterUsers

                Log.d("ChatViewModel", "Successfully fetched ${filterUsers.size} risers")

                // Update user state
                _userState.value = UserState.Success("Successfully fetched all risers")

            } catch (e: Exception) {
                val errorMessage = "Error fetching risers for crRoomId: $crRoomId - ${e.message}"
                Log.e("ChatViewModel", errorMessage, e)
                _userState.value = UserState.Error(errorMessage)
            }
        }
    }

    fun fetchUsersStatus() {
        Log.d("ChatViewModel", "Fetching user status")
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    Log.d("ChatViewModel", "Fetching status for userId: ${user.uid}")

                    // Fetch user status from the repository
                    val status = chatRepository.getUsersStatus(user.uid)
                    _usersStatus.value = status ?: "Unknown"

                    Log.d("ChatViewModel", "User status fetched successfully: ${_usersStatus.value}")

                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error fetching user status for userId: ${user.uid} - ${e.message}", e)
                    _usersStatus.value = "Unknown"
                }
            }
        } ?: run {
            Log.d("ChatViewModel", "No current user logged in; unable to fetch user status.")
        }
    }

    fun selectUploadAndGetImage(game: Boolean, userId: String, byteArray: ByteArray, onResult: (url: String?, error: String?) -> Unit) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                Log.d("ChatViewModel", "Starting image upload for userId: $userId, game: $game")

                val bucket = client.storage["ProfilePictures"]
                val fileName = if (!game) "Personal$userId" else "Alternate$userId"
                val path = if (!game) "Personal/$fileName.jpg" else "Alternate/$fileName.jpg"

                Log.d("ChatViewModel", "Uploading image to path: $path")
                bucket.upload(path, byteArray, true)

                val publicUrl = bucket.publicUrl(path)
                Log.d("ChatViewModel", "Image uploaded successfully. Public URL: $publicUrl")

                _userState.value = UserState.Success("Image uploaded and URL retrieved")
                if (!game) {
                    _personalImage.value = publicUrl
                } else {
                    _alternateImage.value = publicUrl
                }

                onResult(publicUrl, null)
            } catch (e: Exception) {
                val errorMessage = "Error uploading image for userId: $userId - ${e.message}"
                Log.e("ChatViewModel", errorMessage, e)

                _userState.value = UserState.Error("Error: ${e.message}")

                onResult(null, e.message)
            }
        }
    }

    suspend fun uploadImage(context: Context, userId: String, uri: Uri, game: Boolean): String {
        return try {
            Log.d("ChatViewModel", "Starting image upload for userId: $userId, game: $game")

            // Upload the image via the repository
            val imageUrl = chatRepository.uploadImage(userId, uri, game)
            if (imageUrl.isNotEmpty()) {
                Log.d("ChatViewModel", "Image uploaded successfully. URL: $imageUrl")

                // Update user profile with the uploaded image URL
                if (!game) {
                    _userProfile.value = _userProfile.value?.copy(imageUrl = imageUrl)
                    _userProfile.value?.let {
                        saveUserProfile(context = context, userId = userId, userProfile = it, game = false)
                    }
                } else {
                    _alternateUserProfile.value = _alternateUserProfile.value?.copy(imageUrl = imageUrl)
                    _alternateUserProfile.value?.let {
                        saveUserProfile(context = context, userId = userId, userProfile = it, game = true)
                    }
                }

            } else {
                Log.d("ChatViewModel", "Image upload returned an empty URL for userId: $userId")
            }

            imageUrl
        } catch (e: Exception) {
            val errorMessage = "Error uploading image for userId: $userId - ${e.message}"
            Log.e("ChatViewModel", errorMessage, e)
            ""
        }
    }






    /**
     * Chat Room Management
     */

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
    private var isUnreadMessageCountFetched = false
    private var isUnreadCRMessageCountFetched = false


    private var isListenerAdded = false


    fun createAndInviteToChatRoom(crRoomId: String, memberIds: List<String>, roomName: String, onRoomCreated: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: run {
                    Log.e("ChatViewModel", "No current user found. Cannot create or invite to chat room.")
                    return@launch
                }

                val allMemberIds = (memberIds + currentUser.uid).sorted()
                Log.d("ChatViewModel", "Creating or fetching chat room for members: $allMemberIds")

                // Check if a chat room already exists
                val existingRoomId = chatRepository.checkIfChatRoomExists(crRoomId = crRoomId, members = allMemberIds)

                if (existingRoomId != null) {
                    Log.d("ChatViewModel", "Existing chat room found with ID: $existingRoomId")
                    onRoomCreated(existingRoomId)

                } else {
                    // Create a new chat room
                    val roomId = chatRepository.createChatRoom(
                        crRoomId = crRoomId,
                        members = allMemberIds,
                        roomName = roomName
                    )
                    Log.d("ChatViewModel", "New chat room created with ID: $roomId")

                    // Add members to the newly created chat room
                    memberIds.forEach { memberId ->
                        chatRepository.addMemberToRoom(crRoomId = crRoomId, roomId = roomId, memberId = memberId)
                        Log.d("ChatViewModel", "Member with ID: $memberId added to room: $roomId")
                    }

                    onRoomCreated(roomId)

                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error creating or inviting to chat room - ${e.message}", e)
            }
        }
    }
    fun sendMessage(context: Context, crRoomId: String, roomId: String, message: String, memberCount: Int, game: Boolean, mainChat: Boolean) {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Preparing to send message from userId: $currentUser")

                // Fetch the user profile for the sender
                val userProfile = chatRepository.getProfileToSend(userId = currentUser, crRoomId = crRoomId, game = game)
                if (userProfile == null) {
                    Log.e("ChatViewModel", "Failed to fetch user profile for userId: $currentUser")
                    return@launch
                }

                // Construct the chat message
                val chatMessage = ChatMessage(
                    senderId = userProfile.userId,
                    senderName = userProfile.fname,
                    message = message,
                    image = userProfile.imageUrl
                )
                Log.d("ChatViewModel", "Sending message: $chatMessage")

                // Send the message via the repository
                chatRepository.sendMessage(crRoomId = crRoomId, roomId = roomId, chatMessage = chatMessage, game = game, mainChat = mainChat)
                Log.d("ChatViewModel", "Message sent successfully to roomId: $roomId")

                // Fetch updated messages
                fetchChatMessages(context, crRoomId, roomId, game, mainChat)
                //fetchUnreadMessageCount(crRoomId, roomId, game, mainChat)
                fetchCRUnreadMessageCount(crRoomId)


                // firebase analytics

                val messageId = UUID.randomUUID().toString()
                val senderType = "player"
                val roomType =
                    if (mainChat){
                        if (roomId != crRoomId){
                            if (memberCount == 2) {
                                "ChatRise/Private"
                            }else {
                                "ChatRise/Group"
                            }
                        } else {
                            "ChatRise"
                        }
                    }else {
                        if (memberCount == 2){
                            "Private"
                        }else {
                            "Group"
                        }
                    }
                val appVersion = context.packageManager
                    .getPackageInfo(context.packageName, 0).versionName


                // Log the event in Firebase Analytics
                val params = Bundle().apply {
                    putString("message_id", messageId)
                    putString("room_id", roomId)
                    putString("cr_room_id", crRoomId)
                    putString("sender_id", userId)
                    putString("sender_type", senderType)
                    putString("message_type", "text")
                    putInt("message_length", message.length) // Length of the message text
                    putBoolean("contains_media", false)
                    putString("room_type", roomType)
                    putInt("member_count", memberCount)
                    putLong("timestamp", System.currentTimeMillis())
                    putString("device_type", "Android")
                    putString("app_version", appVersion)
                }
                AnalyticsManager.getInstance(context).logEvent("message_sent", params)

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message to roomId: $roomId - ${e.message}", e)
            }
        }
    }
    fun fetchChatMessages(context: Context, crRoomId: String, roomId: String, game: Boolean, mainChat: Boolean) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Fetching chat messages for roomId: $roomId, crRoomId: $crRoomId")

                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                    Log.e("ChatViewModel", "No current user found. Unable to fetch messages.")
                    return@launch
                }

                // Fetch messages from the repository
                val messages = chatRepository.getChatMessages(
                    crRoomId = crRoomId,
                    roomId = roomId,
                    userId = userId,
                    game = game,
                    mainChat = mainChat
                )
                // Log the event in Firebase Analytics
                val params = Bundle().apply {
                    putString("room_id", roomId)
                }
                AnalyticsManager.getInstance(context).logEvent("message_received", params)

                _messages.value = messages

                Log.d("ChatViewModel", "Successfully fetched ${messages.size} messages for roomId: $roomId")

            } catch (e: Exception) {
                Log.e(
                    "ChatViewModel",
                    "Error fetching chat messages for roomId: $roomId, crRoomId: $crRoomId - ${e.message}",
                    e
                )

            }
        }
    }
    fun observeChatMessages(roomId: String, game: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("ChatViewModel", "No current user found. Unable to observe chat messages.")
            return
        }

        try {
            Log.d("ChatViewModel", "Setting up message observer for roomId: $roomId, game: $game")

            chatRepository.observeChatMessages(
                userId = userId,
                roomId = roomId,
                game = game
            ) { messages ->
                _fetchedMessages.value = messages

                Log.d("ChatViewModel", "Observed ${messages.size} messages for roomId: $roomId")
            }

        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error setting up message observer for roomId: $roomId - ${e.message}", e)
        }
    }
    fun fetchChatRoomMembers(crRoomId: String, roomId: String, game: Boolean, mainChat: Boolean) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Fetching members for roomId: $roomId, crRoomId: $crRoomId, game: $game, mainChat: $mainChat")

                // Fetch members from the repository
                val members = chatRepository.getMainChatRoomMembers(
                    crRoomId = crRoomId,
                    roomId = roomId,
                    game = game,
                    mainChat = mainChat
                )
                Log.d("ChatViewModel", "Fetched room members $members")
                _allChatRoomMembers.value = members

                Log.d("ChatViewModel", "Successfully fetched ${members.size} members for roomId: $roomId")

                // Update the user state
                _userState.value = UserState.Success("Success fetching Members")

            } catch (e: Exception) {
                Log.e(
                    "ChatViewModel",
                    "Error fetching members for roomId: $roomId, crRoomId: $crRoomId - ${e.message}",
                    e
                )
                _userState.value = UserState.Error("Error fetching members: ${e.message}")

            }
        }
    }
    fun fetchChatRoomMemberCount(crRoomId: String, roomId: String, game: Boolean, mainChat: Boolean) {
        Log.d("ChatViewModel", "Attempting to fetch single chat room member count for roomId: $roomId")

        viewModelScope.launch {
            try {
                // Fetch the chat room from the repository
                val chatRoom = chatRepository.getChatRoom(crRoomId, roomId, game, mainChat)

                // Update the member count
                _chatRoomMembersCount.value = chatRoom?.members?.size ?: 0
                Log.d("ChatViewModel", "Successfully fetched member count: ${_chatRoomMembersCount.value} for roomId: $roomId")

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching member count for roomId: $roomId - ${e.message}", e)

            }
        }
    }
    fun updateLastSeenTimestamp(roomId: String){
        viewModelScope.launch {
            chatRepository.updateLastSeenTimestamp(roomId, userId)
            fetchUnreadMessageCount()
        }
    }
    fun updateLastCRSeenTimestamp(crRoomId: String, roomId: String){
        viewModelScope.launch {
            chatRepository.updateLastCRSeenTimestamp(crRoomId, roomId, userId)
            fetchCRUnreadMessageCount(crRoomId)
        }
    }

    private val _unreadMessageCount = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadMessageCount: StateFlow<Map<String, Int>> = _unreadMessageCount
    fun fetchUnreadMessageCount() {
        Log.d("ChatViewModel", "Attempting to fetch unread message counts")
        viewModelScope.launch {
            try {
                // Fetch unread message counts for all chat rooms
                val counts = allChatRooms.value.associate { room ->
                    val count = chatRepository.getUnreadMessageCount(room.roomId, userId)
                    room.roomId to count
                }

                // Update the unread message count state
                _unreadMessageCount.value = counts

                Log.d("ChatViewModel", "Successfully fetched unread message counts for ${counts.size} rooms")

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching unread message counts - ${e.message}", e)
            }
        }
    }

    private val _unreadCRMessageCount = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadCRMessageCount: StateFlow<Map<String, Int>> = _unreadCRMessageCount
    fun fetchCRUnreadMessageCount(crRoomId: String) {
        Log.d("ChatViewModel", "Attempting to fetch unread message counts")
        viewModelScope.launch {
            try {
                // Fetch unread message counts for all chat rooms
                val counts = allRiserRooms.value.associate { room ->
                    val count = chatRepository.getCRUnreadMessageCount(crRoomId, room.roomId, userId)
                    room.roomId to count
                }

                // Update the unread message count state
                _unreadCRMessageCount.value = counts

                Log.d("ChatViewModel", "Successfully fetched unread message counts for ${counts.size} rooms")

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching CR unread message counts - ${e.message}", e)
            }
        }
    }

    private fun fetchAllChatRooms() {
        Log.d("ChatViewModel", "Attempting to fetch all chat rooms")

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            try {
                chatRepository.getChatRooms()
                    .whereArrayContains("members", user.uid)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("ChatViewModel", "Error listening for chat rooms - ${e.message}", e)
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            Log.d("ChatViewModel", "Snapshot is not null, processing chat rooms")
                            val rooms = snapshot.documents.mapNotNull { document ->
                                document.toObject(ChatRoom::class.java)
                            }.filter { room ->
                                !room.hiddenFor.contains(user.uid)
                            }.sortedByDescending { it.lastMessageTimestamp } // Sort in descending order
                            _allChatRooms.value = rooms
                            if (!isUnreadMessageCountFetched){
                                fetchUnreadMessageCount()
                                isUnreadMessageCountFetched = true
                            }
                            Log.d("ChatViewModel", "Successfully fetched ${rooms.size} chat rooms")

                            /*
                            // Fetch unread message counts if not already fetched
                            if (!isUnreadMessageCountFetched) {
                                fetchUnreadMessageCount()
                                isUnreadMessageCountFetched = true
                                Log.d("ChatViewModel", "Unread message count fetched")
                            }

                             */
                        }
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Unexpected error fetching chat rooms - ${e.message}", e)
            }
        } ?: run {
            Log.e("ChatViewModel", "No current user found. Unable to fetch chat rooms.")
        }
    }
    fun fetchSingleRoom(crRoomId: String, otherUserId: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Checking if a single room exists for crRoomId: $crRoomId, otherUserId: $otherUserId")

                // Check if the single room exists in the repository
                val roomId = chatRepository.checkIfSingleRoomExists(crRoomId, userId, otherUserId)
                onResult(roomId)

                Log.d("ChatViewModel", "Fetched roomId: $roomId for crRoomId: $crRoomId, otherUserId: $otherUserId")

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching single room for crRoomId: $crRoomId, otherUserId: $otherUserId - ${e.message}", e)
                onResult(null) // Return null in case of an error
            }
        }
    }
    fun fetchAllRiserRooms(crRoomId: String) {
        if (isListenerAdded) {
            Log.d("ChatViewModel", "Listener already added. Skipping fetch for riser rooms.")
            return
        }
        isListenerAdded = true

        Log.d("ChatViewModel", "Attempting to fetch all riser rooms for crRoomId: $crRoomId")

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            try {
                chatRepository.getRiserRoom().document(crRoomId).collection("PrivateChats")
                    .whereArrayContains("members", user.uid)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("ChatViewModel", "Error listening for riser rooms - ${e.message}", e)
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            Log.d("ChatViewModel", "Snapshot received for riser rooms, processing data")

                            val rooms = snapshot.documents.mapNotNull { document ->
                                document.toObject(ChatRoom::class.java)
                            }.filter { room ->
                                !room.hiddenFor.contains(user.uid)
                            }.sortedByDescending { it.lastMessageTimestamp } // Sort in descending order

                            _allRiserRooms.value = rooms

                            Log.d("ChatViewModel", "Successfully fetched ${rooms.size} riser rooms for crRoomId: $crRoomId")


                            // Fetch unread message counts if not already fetched
                            if (!isUnreadCRMessageCountFetched) {
                                fetchCRUnreadMessageCount(crRoomId)
                                isUnreadCRMessageCountFetched = true
                                Log.d("ChatViewModel", "Unread message counts fetched")
                            }


                        }
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Unexpected error fetching riser rooms for crRoomId: $crRoomId - ${e.message}", e)
            }
        } ?: run {
            Log.e("ChatViewModel", "No current user found. Unable to fetch riser rooms.")
        }
    }
    fun getRoomInfo(crRoomId: String, roomId: String) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Fetching room info for crRoomId: $crRoomId, roomId: $roomId")

                // Fetch room info from the repository
                val roomInfo = chatRepository.getRoomInfo(crRoomId = crRoomId, roomId = roomId)
                _roomInfo.value = roomInfo

                if (roomInfo != null) {
                    Log.d("ChatViewModel", "Successfully fetched room info for roomId: $roomId")
                } else {
                    Log.d("ChatViewModel", "No room info found for roomId: $roomId")
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching room info for crRoomId: $crRoomId, roomId: $roomId - ${e.message}", e)

            }
        }
    }


    /*
    fun fetchCRUnreadMessageCount() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: run {
            Log.e("ChatViewModel", "No current user found. Unable to fetch CR unread message count.")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Fetching main chat room for userId: ${currentUser.uid}")

                // Fetch the main chat room for the current user
                val room = chatRepository.getMainChatRoomForUser(currentUser.uid)

                if (room != null) {
                    Log.d("ChatViewModel", "Main chat room found for userId: ${currentUser.uid}, roomId: ${room.roomId}")

                    // Fetch unread message count for the main chat room
                    val unreadCounts = chatRepository.getUnreadMessageCount(room.roomId, currentUser.uid)
                    _unreadCRMessageCount.value = unreadCounts

                    Log.d("ChatViewModel", "Unread message count for roomId: ${room.roomId} is $unreadCounts")

                } else {
                    Log.d("ChatViewModel", "No main chat room found for userId: ${currentUser.uid}")

                }
            } catch (e: Exception) {
                Log.e(
                    "ChatViewModel",
                    "Error fetching unread message count for main chat room of userId: ${currentUser.uid} - ${e.message}",
                    e
                )

            }
        }
    }

     */


    /**
     *  Blocked Player Management
     */
    fun announceBlockedPlayer(
        crRoomId: String,
        blockedPlayer: UserProfile,
        context: Context
    ){
        viewModelScope.launch {
            val systemMessage = ChatMessage(
                senderId = "System",
                senderName = "ChatRise",
                message = "The Top Players have made their decision . . .  \n\nThe player leaving the game is . . . \n\n${blockedPlayer.fname}!",
                image = ""
            )

            chatRepository.sendMessage(crRoomId = crRoomId, roomId = crRoomId, chatMessage = systemMessage, game = true, mainChat = true)
            fetchChatMessages(
                context = context,
                crRoomId = crRoomId,
                roomId = crRoomId,
                game = true,
                mainChat = true
            )

        }
    }
}