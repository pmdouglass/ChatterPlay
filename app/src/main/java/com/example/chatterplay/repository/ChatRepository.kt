package com.example.chatterplay.repository

import android.net.Uri
import android.util.Log
import com.example.chatterplay.data_class.ChatMessage
import com.example.chatterplay.data_class.ChatRoom
import com.example.chatterplay.data_class.GameData
import com.example.chatterplay.data_class.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("Users")
    private val chatRoomsCollection = firestore.collection("Chat Rooms")
    private val CRGameRoomsCollection = firestore.collection("ChatRise Game")
    val more = "Alternate"
    val chatrise = "ChatRise"



    fun getChatRooms() = chatRoomsCollection
    fun getMainChatRoom() = CRGameRoomsCollection
    suspend fun saveUserProfile(
        userId: String,
        userProfile: UserProfile,
        game: Boolean
    ) {
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

    suspend fun getUserProfile(userId: String, game: Boolean): UserProfile? {
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


    suspend fun checkIfChatRoomExists(CRRoomId: String, members: List<String>): String? {
        val sortedMembers = members.sorted()
        if (CRRoomId == "0") {
            val querySnapshot = chatRoomsCollection.get().await()
            for (document in querySnapshot.documents) {
                val chatRoom = document.toObject(ChatRoom::class.java)
                if (chatRoom != null && chatRoom.members.sorted() == sortedMembers) {
                    return document.id
                }
            }
        }else {
            val querySnapshot = chatRoomsCollection
                .document(CRRoomId)
                .collection("Private Chats")
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
    suspend fun createChatRoom(CRRoomId: String, members: List<String>, roomName: String): String {
        val sortedMembers = members.sorted()
        val roomId = chatRoomsCollection.document().id
        val chatRoom = ChatRoom(roomId = roomId, members = sortedMembers, roomName = roomName)

        if (CRRoomId == "0"){
            chatRoomsCollection.document(roomId).set(chatRoom).await()
        } else {
            CRGameRoomsCollection.document(CRRoomId)
                .collection("Private Chats")
                .document(roomId)
                .set(chatRoom).await()
        }
        return roomId
    }
    suspend fun addMemberToRoom(CRRoomId: String, roomId: String, memberId: String){
        if (CRRoomId == "0"){
            val roomRef = chatRoomsCollection.document(roomId)
            roomRef.update("members", FieldValue.arrayUnion(memberId)).await()
        } else {
            val roomRef = CRGameRoomsCollection
                .document(CRRoomId)
                .collection("Private Chats")
                .document(roomId)
            roomRef.update("members", FieldValue.arrayUnion(memberId)).await()
        }
    }









    suspend fun getChatMessages(roomId: String, userId: String): List<ChatMessage> {
        val roomSnapshot = chatRoomsCollection.document(roomId).get().await()
        val chatRoom = roomSnapshot.toObject(ChatRoom::class.java) ?: return emptyList()
        val hiddenTimestamp = chatRoom.hiddenTimestamp[userId] ?: Timestamp(0,0)

        val querySnapshot = chatRoomsCollection
            .document(roomId)
            .collection("messages")
            .whereGreaterThan("timestamp", hiddenTimestamp)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .await()
        return querySnapshot.documents.map { document ->
            document.toObject(ChatMessage::class.java)!!
        }

    }
    suspend fun getChatRoomMembers(roomId: String, game: Boolean): List<UserProfile> {
        val roomCollection = if (game) CRGameRoomsCollection else chatRoomsCollection
        val usersPath = if (game) roomCollection.document(roomId).collection("Users") else usersCollection

        val chatRoomSnapshot = roomCollection.document(roomId).get().await()
        val chatRoom = chatRoomSnapshot.toObject(ChatRoom::class.java)

        return chatRoom?.members?.mapNotNull { memberId ->
            usersPath.document(memberId).get().await().toObject(UserProfile::class.java)
        } ?: emptyList()
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


    suspend fun getRoomInfo(CRRoomId: String, roomId: String): ChatRoom? {
        if (CRRoomId == "0"){
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
                val snapshot = CRGameRoomsCollection.document(CRRoomId)
                    .collection("Private Chats")
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
    suspend fun sendMessage(roomId: String, chatMessage: ChatMessage) {
        val roomRef = chatRoomsCollection.document(roomId)
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
    }






    suspend fun createMainChatriseRoomIfPendingusersMeetCondition(minimumPendingUsers: Int, userId: String): Boolean {
        val pendingUsersSnapshot = usersCollection
            .whereEqualTo("pending", "Pending")
            .get()
            .await()

        if (pendingUsersSnapshot.documents.size >= minimumPendingUsers) {
            val pendingUsers = pendingUsersSnapshot.documents.mapNotNull { it.toObject(UserProfile::class.java) }
                .filter { it.userId != userId }

            if (pendingUsers.size == minimumPendingUsers -1){
                val selectedUsers = pendingUsers.shuffled().take(minimumPendingUsers-1)
                val memberIds = selectedUsers.map { it.userId } + userId

                val uniqueMemberIds = memberIds.toSet().toList()
                val CRRoomId = CRGameRoomsCollection.document().id

                val gamePathRef = CRGameRoomsCollection
                    .document(CRRoomId)
                    .collection("Games")

                val chatRoom = ChatRoom(
                    roomId = CRRoomId,
                    roomName = "Chatrise",
                    members = uniqueMemberIds,
                    createdAt = Timestamp.now()
                )
                val gameDocuments = listOf(
                    "AskMeAnything",
                    "PopQuiz",
                    "NameThePicture",
                    "YesOrNo",
                    "Mojojojo"
                )

                CRGameRoomsCollection.document(CRRoomId).set(chatRoom).await()

                gameDocuments.forEach { gameName ->
                    val gameData = GameData(
                        gameName = gameName,
                        gameStatus = "NotYetPlayed",
                        hasViewed = false
                    )
                    gamePathRef.document(gameName).set(gameData)
                }

                val defaultGameStatus = gameDocuments.associateWith { false }

                selectedUsers.forEach { user ->
                    usersCollection.document(user.userId).update("pending", "In Game").await()
                    CRGameRoomsCollection.document(CRRoomId).collection("Members").document(user.userId)
                        .set(mapOf("Games" to defaultGameStatus)).await()

                }
                usersCollection.document(userId).update("pending", "In Game").await()
                return true
            }
        }
        return false
    }
    suspend fun getUsersStatus(userId: String): String?{
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            documentSnapshot.getString("pending")
        }catch (e: Exception) {
            null
        }
    }
    suspend fun updateUserPendingStatus(userId: String, status: String) {
        try {
            usersCollection.document(userId).update("pending", status).await()
        } catch (e: Exception) {
            e.printStackTrace()
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
