package com.example.chatterplay.repository

import android.net.Uri
import com.example.chatterplay.data_class.ChatRoom
import com.example.chatterplay.data_class.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("Users")
    private val chatRoomsCollection = firestore.collection("Chat Rooms")
    private val CRGameRoomsCollection = firestore.collection("ChatRise Game")
    val more = "Alternate"
    val chatrise = "ChatRise"


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
}

