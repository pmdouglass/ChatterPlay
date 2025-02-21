package com.example.chatterplay.repository

import android.net.Uri
import android.util.Log
import com.example.chatterplay.data_class.ChatMessage
import com.example.chatterplay.data_class.ChatRoom
import com.example.chatterplay.data_class.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("Users")
    private val chatRoomsCollection = firestore.collection("Chat Rooms")
    private val crGameRoomsCollection = firestore.collection("ChatriseRooms")
    private val more = "Alternate"
    private val chatrise = "ChatRise"
    private val private = "PrivateChats"




    /**
     *  User Management
     */
    suspend fun saveUserProfile(userId: String, userProfile: UserProfile, game: Boolean) {
        try {
            if (!game) {
                usersCollection.document(userId)
                    .set(userProfile)
                    .await()
                Log.d("ChatRepository", "User profile saved successfully for userId: $userId")

            } else {
                usersCollection.document(userId)
                    .collection(more)
                    .document(chatrise)
                    .set(userProfile)
                    .await()
                Log.d("ChatRepository", "Game-related user profile saved for userId: $userId")

            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error saving user profile for userId: $userId", e)
        }
    }

    suspend fun getProfileToSend(userId: String, crRoomId: String, game: Boolean): UserProfile? {
        return try {
            val snapshot = if (!game) {
                usersCollection
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
            } else {
                crGameRoomsCollection
                    .document(crRoomId)
                    .collection("Users")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
            }

            val userProfile = snapshot.documents.firstOrNull()?.toObject(UserProfile::class.java)

            // Log success
            Log.d("ChatRepository", "Profile fetched successfully for userId: $userId")

            userProfile
        } catch (e: Exception) {
            // Log the error
            Log.e("ChatRepository", "Error fetching profile for userId: $userId", e)
            null
        }
    }
    suspend fun getRealUserProfile(userId: String): UserProfile?{
        val snapshot = usersCollection
            .document(userId)
            .get().await()

        return snapshot.toObject(UserProfile::class.java)
    }

    suspend fun getUserProfile(userId: String, game: Boolean): UserProfile? {
        try {
            Log.d("ChatRepository", "Fetching profile for userId: $userId, game: $game")

            return if (!game) {
                val snapshot = usersCollection
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val userProfile = snapshot.documents.firstOrNull()?.toObject(UserProfile::class.java)

                // Log success
                Log.d("ChatRepository", "Profile fetched successfully for userId: $userId (main)")
                userProfile
            } else {
                val snapshot = usersCollection
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val document = snapshot.documents.firstOrNull()

                document?.let {
                    val subCollectionSnapshot = usersCollection
                        .document(it.id)
                        .collection(more)
                        .document(chatrise)
                        .get()
                        .await()

                    val userProfile = subCollectionSnapshot.toObject(UserProfile::class.java)

                    // Log success
                    Log.d("ChatRepository", "Profile fetched successfully for userId: $userId (game)")
                    return userProfile
                }
            }
        } catch (e: Exception) {
            // Log error
            Log.e("ChatRepository", "Error fetching profile for userId: $userId", e)
        }
        return null
    }

    suspend fun uploadImage(userId: String, uri: Uri, game: Boolean): String {
        val profileType = if (game) "chatrise" else "normal"
        val storagePath = when (profileType) {
            "normal" -> "profile_images/$userId.jpg"
            "chatrise" -> "chatrise_profile_images/$userId.jpg"
            else -> throw IllegalArgumentException("Unknown profile type: $profileType")
        }

        val storageRef = FirebaseStorage.getInstance().reference.child(storagePath)

        return try {
            // Upload file
            storageRef.putFile(uri).await()
            Log.d("ChatRepository", "Image uploaded successfully to $storagePath for userId: $userId")

            // Retrieve and return download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.d("ChatRepository", "Download URL retrieved successfully: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error uploading image for userId: $userId to $storagePath", e)
            // Return an empty string in case of failure
            ""
        }
    }

    suspend fun getAllUsers(): List<UserProfile> {
        val userProfiles = mutableListOf<UserProfile>()
        return try {
            val userDocuments = usersCollection.get().await()

            // Process each document and convert to UserProfile
            for (document in userDocuments.documents) {
                val user = document.toObject(UserProfile::class.java)
                if (user != null) {
                    userProfiles.add(user)
                }
            }

            Log.d("ChatRepository", "Successfully fetched all users. Total users: ${userProfiles.size}")
            userProfiles
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching all users", e)

            // Return an empty list in case of failure
            emptyList()
        }
    }

    suspend fun getAllRisers(crRoomId: String): List<UserProfile> {
        val userProfiles = mutableListOf<UserProfile>()
        return try {
            val userDocuments = crGameRoomsCollection
                .document(crRoomId)
                .collection("Users")
                .get()
                .await()

            // Process each document and convert to UserProfile
            for (document in userDocuments.documents) {
                val user = document.toObject(UserProfile::class.java)
                if (user != null) {
                    userProfiles.add(user)
                }
            }

            Log.d("ChatRepository", "Successfully fetched all risers for roomId: $crRoomId. Total risers: ${userProfiles.size}")
            userProfiles
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching risers for roomId: $crRoomId", e)

            // Return an empty list in case of failure
            emptyList()
        }
    }

    suspend fun getUsersStatus(userId: String): String? {
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            val status = documentSnapshot.getString("pending")

            // Log success
            Log.d("ChatRepository", "Successfully fetched status for userId: $userId - Status: $status")
            status
        } catch (e: Exception) {
            // Log the error
            Log.e("ChatRepository", "Failed to get user status for userId: $userId - ${e.message}", e)
            null
        }
    }




    /**
     *  Chat Room Management
     */

    fun getChatRooms() = chatRoomsCollection
    fun getRiserRoom() = crGameRoomsCollection
    suspend fun checkIfChatRoomExists(crRoomId: String, members: List<String>): String? {
        val sortedMembers = members.sorted()
        return try {
            if (crRoomId == "0") {
                Log.d("ChatRepository", "Checking for chat room in chatRoomsCollection.")
                val querySnapshot = chatRoomsCollection.get().await()
                for (document in querySnapshot.documents) {
                    val chatRoom = document.toObject(ChatRoom::class.java)
                    if (chatRoom != null && chatRoom.members.sorted() == sortedMembers) {
                        Log.d("ChatRepository", "Matching chat room found with ID: ${document.id}")
                        return document.id
                    }
                }
            } else {
                Log.d("ChatRepository", "Checking for chat room in crGameRoomsCollection with crRoomId: $crRoomId.")
                val querySnapshot = crGameRoomsCollection
                    .document(crRoomId)
                    .collection(private)
                    .get().await()
                for (document in querySnapshot.documents) {
                    val chatRoom = document.toObject(ChatRoom::class.java)
                    if (chatRoom != null && chatRoom.members.sorted() == sortedMembers) {
                        Log.d("ChatRepository", "Matching chat room found with ID: ${document.id}")
                        return document.id
                    }
                }
            }
            Log.d("ChatRepository", "No matching chat room found.")
            null
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error while checking for chat room: ${e.message}", e)
            null
        }
    }
    suspend fun checkIfSingleRoomExists(crRoomId: String, userId: String, otherMemberId: String): String? {
        return try {
            if (crRoomId == "0") {
                Log.d("ChatRepository", "Checking for single chat room in chatRoomsCollection.")
                val querySnapshot = chatRoomsCollection.get().await()
                for (document in querySnapshot.documents) {
                    val chatRoom = document.toObject(ChatRoom::class.java)
                    if (chatRoom != null && chatRoom.members.size == 2 &&
                        chatRoom.members.contains(userId) && chatRoom.members.contains(otherMemberId)) {
                        Log.d("ChatRepository", "Single chat room found with ID: ${document.id}")
                        return document.id
                    }
                }
            } else {
                Log.d("ChatRepository", "Checking for single chat room in crGameRoomsCollection with crRoomId: $crRoomId.")
                val querySnapshot = crGameRoomsCollection
                    .document(crRoomId)
                    .collection(private)
                    .get().await()
                for (document in querySnapshot.documents) {
                    val chatRoom = document.toObject(ChatRoom::class.java)
                    if (chatRoom != null && chatRoom.members.size == 2 &&
                        chatRoom.members.contains(userId) && chatRoom.members.contains(otherMemberId)) {
                        Log.d("ChatRepository", "Single chat room found with ID: ${document.id}")
                        return document.id
                    }
                }
            }
            Log.d("ChatRepository", "No single chat room found.")
            null
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error while checking for single chat room: ${e.message}", e)
            null
        }
    }
    suspend fun createChatRoom(crRoomId: String, members: List<String>, roomName: String): String {
        val sortedMembers = members.sorted()
        val roomId = chatRoomsCollection.document().id
        val chatRoom = ChatRoom(roomId = roomId, members = sortedMembers, roomName = roomName)

        return try {
            if (crRoomId == "0") {
                Log.d("ChatRepository", "Creating chat room in chatRoomsCollection with roomId: $roomId.")
                chatRoomsCollection.document(roomId).set(chatRoom).await()
                Log.d("ChatRepository", "Chat room successfully created in chatRoomsCollection.")
            } else {
                Log.d("ChatRepository", "Creating chat room in crGameRoomsCollection under crRoomId: $crRoomId with roomId: $roomId.")
                crGameRoomsCollection.document(crRoomId)
                    .collection(private)
                    .document(roomId)
                    .set(chatRoom).await()
                Log.d("ChatRepository", "Chat room successfully created in crGameRoomsCollection.")
            }
            roomId
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error while creating chat room: ${e.message}", e)
            ""
        }
    }
    suspend fun addMemberToRoom(crRoomId: String, roomId: String, memberId: String) {
        try {
            if (crRoomId == "0") {
                Log.d("ChatRepository", "Adding member with ID: $memberId to chatRoomsCollection room with ID: $roomId.")
                val roomRef = chatRoomsCollection.document(roomId)
                roomRef.update("members", FieldValue.arrayUnion(memberId)).await()
                Log.d("ChatRepository", "Member successfully added to chatRoomsCollection room.")
            } else {
                Log.d("ChatRepository", "Adding member with ID: $memberId to crGameRoomsCollection room with crRoomId: $crRoomId and roomId: $roomId.")
                val roomRef = crGameRoomsCollection
                    .document(crRoomId)
                    .collection(private)
                    .document(roomId)
                roomRef.update("members", FieldValue.arrayUnion(memberId)).await()
                Log.d("ChatRepository", "Member successfully added to crGameRoomsCollection room.")
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error while adding member to room: ${e.message}", e)
        }
    }
    suspend fun getMainChatRoomMembers(crRoomId: String, roomId: String, game: Boolean, mainChat: Boolean): List<UserProfile> {
        Log.d("ChatRepository", "Fetching main chat room members. crRoomId: $crRoomId, roomId: $roomId, game: $game, mainChat: $mainChat")

        return try {
            // Determine the base collection
            val roomCollection = if (game) {
                if (mainChat) {
                    crGameRoomsCollection
                } else {
                    crGameRoomsCollection.document(crRoomId).collection(private)
                }
            } else {
                chatRoomsCollection
            }

            // Determine the users collection path
            val usersPath = if (game) {
                if (mainChat) {
                    roomCollection.document(crRoomId).collection("Users")
                } else {
                    crGameRoomsCollection.document(crRoomId).collection("Users")
                }
            } else {
                usersCollection
            }

            // Fetch the chat room details
            Log.d("ChatRepository", "Fetching chat room details for roomId: $roomId.")
            val chatRoomSnapshot = roomCollection.document(roomId).get().await()
            val chatRoom = chatRoomSnapshot.toObject(ChatRoom::class.java)

            // Fetch user profiles for members
            if (chatRoom == null) {
                Log.d("ChatRepository", "Chat room not found or does not exist.")
                return emptyList()
            }

            Log.d("ChatRepository", "Fetching user profiles for members of the chat room.")
            chatRoom.members.mapNotNull { memberId ->
                try {
                    usersPath.document(memberId).get().await().toObject(UserProfile::class.java).also {
                        Log.d("ChatRepository", "Fetched user profile for memberId: $memberId.")
                    }
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error fetching user profile for memberId: $memberId - ${e.message}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error while fetching main chat room members: ${e.message}", e)
            emptyList()
        }
    }
    suspend fun getMembersForGame(crRoomId: String, roomId: String, game: Boolean, mainChat: Boolean): List<UserProfile> {
        Log.d("ChatRepository", "Getting Members for Game")
        Log.d("ChatRepository", "Parameters - crRoomId: $crRoomId, roomId: $roomId, game: $game, mainChat: $mainChat")

        // Determine the room collection
        val roomCollection = if (game) crGameRoomsCollection else chatRoomsCollection
        Log.d("ChatRepository", "Using roomCollection path: ${roomCollection.path}")

        return try {
            if (mainChat) {
                // Main chat logic
                val usersPath = roomCollection.document(crRoomId).collection("Users")
                Log.d("ChatRepository", "MainChat UserPath: ${usersPath.path}")

                val users = usersPath.get().await().documents.mapNotNull { document ->
                    document.toObject(UserProfile::class.java).also { user ->
                        Log.d("ChatRepository", "Fetched user: ${user?.userId ?: "null"}")
                    }
                }
                Log.d("ChatRepository", "Total users fetched for mainChat: ${users.size}")
                users
            } else {
                // Private room logic
                val privateRoomDoc = roomCollection.document(crRoomId)
                    .collection(private)
                    .document(roomId)
                    .get()
                    .await()

                if (!privateRoomDoc.exists()) {
                    Log.d("ChatRepository", "Private room document does not exist.")
                    return emptyList()
                }

                val members = privateRoomDoc.get("members") as? List<String> ?: emptyList()
                Log.d("ChatRepository", "Private Room members fetched: $members")

                val userPath = roomCollection.document(crRoomId).collection("Users")
                Log.d("ChatRepository", "PrivateRoom UserPath: ${userPath.path}")

                val users = members.mapNotNull { memberId ->
                    try {
                        userPath.document(memberId).get().await().toObject(UserProfile::class.java).also { user ->
                            Log.d("ChatRepository", "Fetched member user: ${user?.userId ?: "null"}")
                        }
                    } catch (e: Exception) {
                        Log.e("ChatRepository", "Error fetching user profile for memberId: $memberId - ${e.message}", e)
                        null
                    }
                }
                Log.d("ChatRepository", "Total users fetched for privateRoom: ${users.size}")
                users
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching members for game: ${e.message}", e)
            emptyList()
        }
    }
    suspend fun getChatRoom(crRoomId: String, roomId: String?, game: Boolean, mainChat: Boolean): ChatRoom? {
        if (roomId.isNullOrBlank()) {
            Log.e("ChatRepository", "Error: roomId is blank or null!")
            return null
        }

        return try {
            val snapshot = when {
                !game -> chatRoomsCollection.document(roomId)
                mainChat -> crGameRoomsCollection.document(crRoomId)
                else -> crGameRoomsCollection.document(crRoomId).collection("PrivateChats").document(roomId)
            }

            Log.d("ChatRepository", "Fetching chat room for crRoomId: $crRoomId roomId: $roomId")
            val documentSnapshot = snapshot.get().await()
            val chatRoom = documentSnapshot.toObject(ChatRoom::class.java)

            chatRoom?.let {
                Log.d("ChatRepository", "Successfully fetched chat room: $it")
            } ?: Log.d("ChatRepository", "No chat room found for roomId: $roomId")

            chatRoom
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching chat room with roomId: $roomId - ${e.message}", e)
            null
        }
    }
    suspend fun getRoomInfo(crRoomId: String, roomId: String): ChatRoom? {
        return try {
            val snapshot = if (crRoomId == "0") {
                Log.d("ChatRepository", "Fetching chat room info from chatRoomsCollection for roomId: $roomId")
                chatRoomsCollection.document(roomId).get().await()
            } else {
                Log.d("ChatRepository", "Fetching chat room info from crGameRoomsCollection for crRoomId: $crRoomId and roomId: $roomId")
                crGameRoomsCollection.document(crRoomId)
                    .collection(private)
                    .document(roomId)
                    .get().await()
            }
            val chatRoom = snapshot.toObject(ChatRoom::class.java)
            if (chatRoom != null) {
                Log.d("ChatRepository", "Successfully fetched chat room: $chatRoom")
            } else {
                Log.d("ChatRepository", "No chat room found for roomId: $roomId and crRoomId: $crRoomId")
            }
            chatRoom
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching room info for roomId: $roomId and crRoomId: $crRoomId - ${e.message}", e)
            null
        }
    }
    suspend fun updateLastSeenTimestamp(roomId: String, userId: String){
        val roomRef = chatRoomsCollection.document(roomId)
        val timestamp = Timestamp.now()
        roomRef.update("lastSeenTimestamps.$userId", timestamp).await()
    }
    suspend fun updateLastCRSeenTimestamp(crRoomId: String, roomId: String, userId: String){
        val roomRef = crGameRoomsCollection.document(crRoomId).collection("PrivateChats").document(roomId)
        val timestamp = Timestamp.now()
        roomRef.update("lastSeenTimestamps.$userId", timestamp).await()
    }

    suspend fun getUnreadMessageCount(roomId: String, userId: String): Int {
        Log.d("ChatRepository", "Fetching unread message count for roomId: $roomId and userId: $userId")
        return try {
            val roomRef = chatRoomsCollection.document(roomId)
            val roomSnapshot = roomRef.get().await()
            val chatRoom = roomSnapshot.toObject(ChatRoom::class.java)

            if (chatRoom == null) {
                Log.d("ChatRepository", "Chat room not found for roomId: $roomId")
                return 0
            }

            val lastSeenTimestamp = chatRoom.lastSeenTimestamps?.get(userId) ?: Timestamp(0, 0)
            Log.d("ChatRepository", "Last seen timestamp for userId: $userId is $lastSeenTimestamp")

            val messagesSnapshot = roomRef.collection("messages")
                .whereGreaterThan("timestamp", lastSeenTimestamp)
                .whereNotEqualTo("senderId", userId)
                .get()
                .await()

            val unreadCount = messagesSnapshot.size()
            Log.d("ChatRepository", "Unread message count for userId: $userId in roomId: $roomId is $unreadCount")
            unreadCount
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching unread message count for roomId: $roomId and userId: $userId - ${e.message}", e)
            0
        }
    }
    suspend fun getCRUnreadMessageCount(crRoomId: String, roomId: String, userId: String): Int {
        Log.d("ChatRepository", "Fetching unread message count for roomId: $roomId and userId: $userId")
        return try {
            val roomRef = crGameRoomsCollection.document(crRoomId).collection("PrivateChats").document(roomId)
            val roomSnapshot = roomRef.get().await()
            val chatRoom = roomSnapshot.toObject(ChatRoom::class.java)

            if (chatRoom == null) {
                Log.d("ChatRepository", "Chat room not found for crRoomId: $crRoomId in roomId: $roomId")
                return 0
            }

            val lastSeenTimestamp = chatRoom.lastSeenTimestamps?.get(userId) ?: Timestamp(0, 0)
            Log.d("ChatRepository", "Last seen timestamp for userId: $userId is $lastSeenTimestamp")

            val messagesSnapshot = roomRef.collection("messages")
                .whereGreaterThan("timestamp", lastSeenTimestamp)
                .whereNotEqualTo("senderId", userId)
                .get()
                .await()

            val unreadCount = messagesSnapshot.size()
            Log.d("ChatRepository", "Unread message count for userId: $userId in crRoomId: $crRoomId in roomId: $roomId is $unreadCount")
            unreadCount
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching unread message count for roomId: $roomId and userId: $userId - ${e.message}", e)
            0
        }
    }
    suspend fun sendMessage(crRoomId: String, roomId: String, chatMessage: ChatMessage, game: Boolean, mainChat: Boolean) {
        try {
            Log.d("ChatRepository", "Sending message to roomId: $roomId, crRoomId: $crRoomId, game: $game, mainChat: $mainChat")

            // Determine the appropriate room reference
            val room = if (game) crGameRoomsCollection else chatRoomsCollection
            val roomRef = if (!game) {
                room.document(roomId)
            } else {
                if (mainChat) {
                    room.document(crRoomId)
                } else {
                    room.document(crRoomId)
                        .collection(private)
                        .document(roomId)
                }
            }

            // Add timestamp to the chat message
            val messageWithTimestamp = chatMessage.copy(timestamp = Timestamp.now())

            // Execute Firestore transaction to send the message and update the room metadata
            firestore.runTransaction { transaction ->
                // Add the message to the room's messages collection
                transaction.set(roomRef.collection("messages").document(), messageWithTimestamp)

                // Update the room's metadata
                transaction.update(
                    roomRef, mapOf(
                        "lastMessage" to chatMessage.message,
                        "lastMessageTimestamp" to messageWithTimestamp.timestamp,
                        "lastProfile" to chatMessage.image,
                        "hiddenFor" to emptyList<String>(),
                        "hiddenTimestamp" to emptyMap<String, Timestamp>()
                    )
                )
            }.await()

            Log.d("ChatRepository", "Message successfully sent to roomId: $roomId")
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending message to roomId: $roomId and crRoomId: $crRoomId - ${e.message}", e)
        }
    }
    suspend fun getChatMessages(crRoomId: String, roomId: String, userId: String, game: Boolean, mainChat: Boolean): List<ChatMessage> {
        return try {
            Log.d(
                "ChatRepository",
                "Fetching chat messages for roomId: $roomId, crRoomId: $crRoomId, userId: $userId, game: $game, mainChat: $mainChat"
            )

            // Determine the room reference
            val room = if (game) crGameRoomsCollection else chatRoomsCollection
            val queryRoom = if (game) {
                if (mainChat) {
                    room.document(crRoomId)
                } else {
                    room.document(crRoomId)
                        .collection(private)
                        .document(roomId)
                }
            } else {
                room.document(roomId)
            }

            // Fetch the chat room details
            val roomSnapshot = queryRoom.get().await()
            val chatRoom = roomSnapshot.toObject(ChatRoom::class.java)

            if (chatRoom == null) {
                Log.d("ChatRepository", "Chat room not found for roomId: $roomId and crRoomId: $crRoomId")
                return emptyList()
            }

            val hiddenTimestamp = chatRoom.hiddenTimestamp[userId] ?: Timestamp(0, 0)
            Log.d("ChatRepository", "Hidden timestamp for userId: $userId is $hiddenTimestamp")

            // Fetch chat messages
            val querySnapshot = queryRoom
                .collection("messages")
                .whereGreaterThan("timestamp", hiddenTimestamp)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            val messages = querySnapshot.documents.mapNotNull { document ->
                val message = document.toObject(ChatMessage::class.java)

                // Exclude "blockedMessage" field
                if (message != null && !document.contains("BlockedMessage")){
                    Log.d("ChatRepository", "Fetched message with id: ${document.id}")
                    message
                }else {
                    Log.d("ChatRepository", "Excluded message with BlockedMessage field: ${document.id}")
                    null
                }
            }

            Log.d("ChatRepository", "Total messages fetched: ${messages.size}")
            messages
        } catch (e: Exception) {
            Log.e(
                "ChatRepository",
                "Error fetching chat messages for roomId: $roomId and crRoomId: $crRoomId - ${e.message}",
                e
            )
            emptyList()
        }
    }
    fun observeChatMessages(userId: String, roomId: String, game: Boolean, onMessagesChanged: (List<ChatMessage>) -> Unit){
        val room = if (game) crGameRoomsCollection else chatRoomsCollection
        room.document(roomId)
            .collection("messages")
            .addSnapshotListener { snapshot, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                if (snapshot != null){
                    val chatRoomDocument = room.document(roomId).get().result
                    val chatRoom = chatRoomDocument?.toObject(ChatRoom::class.java) ?: return@addSnapshotListener

                    val hiddenTimestamp = chatRoom.hiddenTimestamp[userId] ?: Timestamp(0,0)
                    val messages = snapshot.documents
                        .mapNotNull { document ->
                            val chatMessage = document.toObject(ChatMessage::class.java)
                            chatMessage?.takeIf { it.timestamp > hiddenTimestamp }
                        }
                    onMessagesChanged(messages)
                }
            }
    }
}



suspend fun fetchUserProfile(userId: String): UserProfile? {
    return try {
        Log.d("ChatRepository", "Fetching user profile for userId: $userId")
        val documentSnapshot = FirebaseFirestore.getInstance()
            .collection("Users")
            .document(userId)
            .get()
            .await()

        val userProfile = documentSnapshot.toObject(UserProfile::class.java)
        if (userProfile != null) {
            Log.d("ChatRepository", "Successfully fetched user profile for userId: $userId")
        } else {
            Log.d("ChatRepository", "No user profile found for userId: $userId")
        }
        userProfile
    } catch (e: Exception) {
        Log.e("ChatRepository", "Error fetching user profile for userId: $userId - ${e.message}", e)
        null
    }
}

