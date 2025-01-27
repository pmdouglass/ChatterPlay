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
    private val private = "Private Chats"




    /**
     *  User Management
     */
    suspend fun saveUserProfile(userId: String, userProfile: UserProfile, game: Boolean) {
        if (!game) {
            usersCollection.document(userId)
                .set(userProfile)
                .await()
        } else {
            usersCollection.document(userId)
                .collection(more)
                .document(chatrise)
                .set(userProfile)
                .await()
        }
    }
    suspend fun getProfileToSend(userId: String, crRoomId: String, game: Boolean): UserProfile? {
        return if (!game){
            val snapshot = usersCollection
                .whereEqualTo("userId", userId).get().await()
            snapshot.documents.firstOrNull()?.toObject(UserProfile::class.java)
        } else {
            val snapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection("Users")
                .whereEqualTo("userId", userId).get().await()
            snapshot.documents.firstOrNull()?.toObject(UserProfile::class.java)
        }
    }
    suspend fun getUserProfile(userId: String, game: Boolean): UserProfile? {
        Log.d("Debug-Message", "Fetching profile for userId: $userId")
        if (!game) {
            val snapshot = usersCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            return snapshot.documents.firstOrNull()?.toObject(UserProfile::class.java)

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

                return subCollectionSnapshot.toObject(UserProfile::class.java)
            }
        }
        return null
    }
    suspend fun uploadImage(userId: String, uri: Uri, game: Boolean): String {
        val profileType = if (game) "chatrise" else "normal"
        val storagePath = when (profileType){
            "normal" -> "profile_images/$userId.jpg"
            "chatrise" -> "chatrise_profile_images/$userId.jpg"
            else -> throw IllegalArgumentException("Unknown profile type: $profileType")
        }

        val storageRef = FirebaseStorage.getInstance().reference.child(storagePath)

        return try {
            storageRef.putFile(uri).await()
            return storageRef.downloadUrl.await().toString()
        }catch (e: Exception){
            ""
        }
    }
    suspend fun getAllUsers(): List<UserProfile> {
        val userDocuments = usersCollection.get().await()
        val userProfiles = mutableListOf<UserProfile>()

        for (document in userDocuments.documents){
            val user = document.toObject(UserProfile::class.java)
            if (user != null){
                userProfiles.add(user)
            }
        }
        return userProfiles
    }
    suspend fun getAllRisers(crRoomId: String): List<UserProfile> {
        val userDocuments = crGameRoomsCollection.document(crRoomId).collection("Users").get().await()
        val userProfiles = mutableListOf<UserProfile>()

        for (document in userDocuments.documents){
            val user = document.toObject(UserProfile::class.java)
            if (user != null){
                userProfiles.add(user)
            }
        }
        return userProfiles
    }
    suspend fun getUsersStatus(userId: String): String?{
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            documentSnapshot.getString("pending")
        }catch (e: Exception) {
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
        if (crRoomId == "0") {
            val querySnapshot = chatRoomsCollection.get().await()
            for (document in querySnapshot.documents) {
                val chatRoom = document.toObject(ChatRoom::class.java)
                if (chatRoom != null && chatRoom.members.sorted() == sortedMembers) {
                    return document.id
                }
            }
        }else {
            val querySnapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection(private)
                .get().await()
            for (document in querySnapshot.documents){
                val chatRoom = document.toObject(ChatRoom::class.java)
                if (chatRoom != null && chatRoom.members.sorted() == sortedMembers) {
                    return document.id
                }
            }
        }
        return null
    }
    suspend fun checkIfSingleRoomExists(crRoomId: String, userId: String, otherMemberId: String): String? {
        if (crRoomId == "0") {
            val querySnapshot = chatRoomsCollection.get().await()
            for (document in querySnapshot.documents) {
                val chatRoom = document.toObject(ChatRoom::class.java)
                if (chatRoom != null && chatRoom.members.size == 2 && chatRoom.members.contains(userId) && chatRoom.members.contains(otherMemberId)) {
                    return document.id
                }
            }
        }else {
            Log.d("riser", "fetching roomId")
            val querySnapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection(private)
                .get().await()
            for (document in querySnapshot.documents){
                val chatRoom = document.toObject(ChatRoom::class.java)
                if (chatRoom != null && chatRoom.members.size == 2 && chatRoom.members.contains(userId) && chatRoom.members.contains(otherMemberId)) {
                    return document.id
                }
            }
        }
        return null
    }
    suspend fun createChatRoom(crRoomId: String, members: List<String>, roomName: String): String {
        val sortedMembers = members.sorted()
        val roomId = chatRoomsCollection.document().id
        val chatRoom = ChatRoom(roomId = roomId, members = sortedMembers, roomName = roomName)

        if (crRoomId == "0"){
            chatRoomsCollection.document(roomId).set(chatRoom).await()
        } else {
            crGameRoomsCollection.document(crRoomId)
                .collection(private)
                .document(roomId)
                .set(chatRoom).await()
        }
        return roomId
    }
    suspend fun addMemberToRoom(crRoomId: String, roomId: String, memberId: String){
        if (crRoomId == "0"){
            val roomRef = chatRoomsCollection.document(roomId)
            roomRef.update("members", FieldValue.arrayUnion(memberId)).await()
        } else {
            val roomRef = crGameRoomsCollection
                .document(crRoomId)
                .collection(private)
                .document(roomId)
            roomRef.update("members", FieldValue.arrayUnion(memberId)).await()
        }
    }
    suspend fun getMainChatRoomMembers(crRoomId: String, roomId: String, game: Boolean, mainChat: Boolean): List<UserProfile> {
        Log.d("riser", "crRoomId: $crRoomId, roomId:$roomId, game:$game, mainChat:$mainChat")

        // Base Collection
        val roomCollection = if (game) {
            if (mainChat){
                crGameRoomsCollection
            } else {
                crGameRoomsCollection.document(crRoomId).collection(private)
            }
        } else {
            chatRoomsCollection
        }
        val usersPath = if (game) {
            if (mainChat){
                roomCollection.document(crRoomId).collection("Users")
            } else {
                crGameRoomsCollection.document(crRoomId).collection("Users")
            }
        } else {
            usersCollection
        }

        val chatRoomSnapshot = roomCollection.document(roomId).get().await()
        val chatRoom = chatRoomSnapshot.toObject(ChatRoom::class.java)

        return chatRoom?.members?.mapNotNull { memberId ->
            usersPath.document(memberId).get().await().toObject(UserProfile::class.java)
        } ?: emptyList()
    }
    suspend fun getMembersforGame(crRoomId: String, roomId: String, game: Boolean, mainChat: Boolean): List<UserProfile> {
        // which path
        // which room
        // collect members
        // get userprofile

        Log.d("Repository", "Getting Members for Game")
        Log.d("Repository", "Parameters - crRoomId: $crRoomId, roomId: $roomId, game: $game, mainChat: $mainChat")

        // Determine the collection to use
        val roomCollection = if (game) crGameRoomsCollection else chatRoomsCollection
        Log.d("Repository", "Using roomCollection path: ${roomCollection.path}")

        return if (mainChat) {
            // Main chat logic
            val usersPath = roomCollection.document(crRoomId).collection("Users")
            Log.d("Repository", "MainChat UserPath: ${usersPath.path}")

            try {
                val users = usersPath.get().await().documents.mapNotNull { document ->
                    val user = document.toObject(UserProfile::class.java)
                    Log.d("Repository", "Fetched user: ${user?.userId ?: "null"}")
                    user
                }
                Log.d("Repository", "Total users fetched for mainChat: ${users.size}")
                users
            } catch (e: Exception) {
                Log.e("Repository", "Error fetching users for mainChat: ${e.message}", e)
                emptyList()
            }
        } else {
            // Private room logic
            try {
                val privateRoomDoc = roomCollection.document(crRoomId)
                    .collection(private)
                    .document(roomId)
                    .get()
                    .await()
                Log.d("Repository", "privateRoomDoc exists: ${privateRoomDoc.exists()}")

                val members = privateRoomDoc.get("members") as? List<String> ?: emptyList()
                Log.d("Repository", "Private Room members fetched: $members")

                val userPath = roomCollection.document(crRoomId).collection("Users")
                Log.d("Repository", "PrivateRoom UserPath: ${userPath.path}")

                val users = members.mapNotNull { memberId ->
                    val user = userPath.document(memberId).get().await().toObject(UserProfile::class.java)
                    Log.d("Repository", "Fetched member user: ${user?.userId ?: "null"}")
                    user
                }
                Log.d("Repository", "Total users fetched for privateRoom: ${users.size}")
                users
            } catch (e: Exception) {
                Log.e("Repository", "Error fetching users for privateRoom: ${e.message}", e)
                emptyList()
            }
        }
    }
    suspend fun getSingleChatRoom(roomId: String): ChatRoom? {
        return try {
            val documentSnapshot = chatRoomsCollection.document(roomId).get().await()
            documentSnapshot.toObject(ChatRoom::class.java)
        } catch (e: Exception){
            e.printStackTrace()
            null
        }
    }
    suspend fun getRoomInfo(crRoomId: String, roomId: String): ChatRoom? {
        if (crRoomId == "0"){
            return try {
                val snapshot = chatRoomsCollection.document(roomId)
                    .get()
                    .await()
                return snapshot.toObject(ChatRoom::class.java)
            } catch (e: Exception){
                null
            }
        } else {
            return try {
                val snapshot = crGameRoomsCollection.document(crRoomId)
                    .collection(private)
                    .document(roomId)
                    .get()
                    .await()
                return snapshot.toObject(ChatRoom::class.java)
            } catch (e: Exception){
                null
            }
        }

    }
    suspend fun getUnreadMessageCount(roomId: String, userId: String): Int{
        Log.d("Time", "Inside repository unread Messages")
        val roomRef = chatRoomsCollection.document(roomId)
        val roomSnapshot = roomRef.get().await()
        val chatRoom = roomSnapshot.toObject(ChatRoom::class.java)
        val lastSeenTimestamp = chatRoom?.lastSeenTimestamps?.get(userId) ?: Timestamp(0,0)

        val messagesSnapshot = roomRef.collection("messages")
            //.whereGreaterThan("timestamp", lastSeenTimestamp)
            //.whereNotEqualTo("senderId", userId)
            .get()
            .await()

        return messagesSnapshot.size()
    }
    suspend fun sendMessage(crRoomId: String, roomId: String, chatMessage: ChatMessage, game: Boolean, mainChat: Boolean) {
        val room = if (game) crGameRoomsCollection else chatRoomsCollection
        val roomRef = if (!game){
            room.document(roomId)
        } else {
            if (mainChat){
                room.document(crRoomId)
            } else{
                room.document(crRoomId)
                    .collection(private)
                    .document(roomId)
            }
        }
        val messageWithTimestamp = chatMessage.copy(timestamp = Timestamp.now())
        firestore.runTransaction { transaction ->
            transaction.set(roomRef.collection("messages").document(), messageWithTimestamp)
            transaction.update(roomRef, mapOf(
                "lastMessage" to chatMessage.message,
                "lastMessageTimestamp" to messageWithTimestamp.timestamp,  // Ensure this field is updated
                "lastProfile" to chatMessage.image,
                "hiddenFor" to emptyList<String>(),
                "hiddenTimestamp" to emptyMap<String, Timestamp>()
            ))
        }.await()
        Log.d("Message", "Repository send Message")
    }
    suspend fun getChatMessages(crRoomId: String, roomId: String, userId: String, game: Boolean, mainChat: Boolean): List<ChatMessage> {
        val room = if (game) crGameRoomsCollection else chatRoomsCollection
        val queryRoom = if (game) {
            if (mainChat){
                room.document(crRoomId)
            }else {
                room.document(crRoomId)
                    .collection(private)
                    .document(roomId)
            }
        } else {
            room.document(roomId)
        }
        val roomSnapshot = queryRoom.get().await()
        val chatRoom = roomSnapshot.toObject(ChatRoom::class.java) ?: return emptyList()
        val hiddenTimestamp = chatRoom.hiddenTimestamp[userId] ?: Timestamp(0,0)

        val querySnapshot = queryRoom
            .collection("messages")
            .whereGreaterThan("timestamp", hiddenTimestamp)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .await()
        return querySnapshot.documents.map { document ->
            document.toObject(ChatMessage::class.java)!!
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
    // Replace this with the actual method to fetch the user profile
    return try {
        val documentSnapshot = FirebaseFirestore.getInstance()
            .collection("Users")
            .document(userId)
            .get()
            .await()
        documentSnapshot.toObject(UserProfile::class.java)
    } catch (e: Exception){
        null
    }
}
