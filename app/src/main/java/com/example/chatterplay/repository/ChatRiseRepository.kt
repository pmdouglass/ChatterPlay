package com.example.chatterplay.repository

import android.util.Log
import com.example.chatterplay.data_class.Questions
import com.example.chatterplay.data_class.SupabaseClient
import com.example.chatterplay.data_class.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.FilterOperator
import kotlinx.coroutines.tasks.await


class ChatRiseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val crGameRoomsCollection = firestore.collection("ChatriseRooms")

    private val users = "Users"

    suspend fun getUserProfile(crRoomId: String, userId: String): UserProfile?{
        val snapshot = crGameRoomsCollection
            .document(crRoomId)
            .collection(users)
            .document(userId)
            .get().await()
        return snapshot.toObject(UserProfile::class.java)
    }
    suspend fun getAllUsersInRoom(crRoomId: String) =
        crGameRoomsCollection.document(crRoomId).collection(users).get().await()!!
    suspend fun getAllRankingsInRoom(crRoomId: String) =
        crGameRoomsCollection.document(crRoomId).collection("Rankings").get().await()!!
    fun addBonusPoint(crRoomId: String, userId: String, bonus: Int){
        val rankingRef = crGameRoomsCollection
            .document(crRoomId)
            .collection("Rankings")
            .document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(rankingRef)
            val currentPoints = snapshot.getLong("totalPoints")?.toInt() ?: 0
            transaction.update(rankingRef, "totalPoints", currentPoints + bonus)
        }
    }
    suspend fun fetchUserRankingStatus(crRoomId: String, userId: String): String? {
        return try {
            val documentSnapshot = crGameRoomsCollection
                .document(crRoomId)
                .collection(users)
                .document(userId)
                .get().await()
            documentSnapshot.getString("rankingStatus")
        }catch (e: Exception){
            null
        }
    }
    fun updateUserRankingStatus(crRoomId: String, userId: String, updatedStatus: String){
        val data = mapOf(
            "rankingStatus" to updatedStatus
        )
        crGameRoomsCollection
            .document(crRoomId)
            .collection(users)
            .document(userId)
            .set(data, SetOptions.merge())
    }

    suspend fun resetRanking(crRoomId: String){
        val rankingRef = crGameRoomsCollection
            .document(crRoomId)
            .collection("Rankings")
        try {
            val snapshot = rankingRef.get().await()

            firestore.runTransaction { transaction ->
                if (snapshot.isEmpty){

                }else {
                    snapshot.documents.forEach { document ->
                        val memberId = document.id
                        val memberRef = rankingRef.document(memberId)

                        transaction.update(memberRef, mapOf(
                            "totalPoints" to 0,
                            "votes" to emptyMap<String, Any>()
                        ))
                    }
                }

            }
        }catch (e: Exception){
            Log.d("Repository", "Failed to reset Rank")
        }

    }

    fun saveRanking(crRoomId: String, memberId: String, userId: String, newPoints: Int) {
        Log.d("Repository", "Saving ranking system")
        val rankingRef = crGameRoomsCollection
            .document(crRoomId)
            .collection("Rankings")
            .document(memberId)
        Log.d("Repository", "roomRef = $rankingRef")

        firestore.runTransaction { transaction ->
            Log.d("Repository", "Running Transaction")

            val snapshot = transaction.get(rankingRef)

            if (!snapshot.exists()) {
                // Document does not exist, create it with initial values
                val initialData = mapOf(
                    "totalPoints" to newPoints,
                    "bonusPoints" to 0,
                    "votes" to mapOf(userId to mapOf("pointsGiven" to newPoints))
                )
                transaction.set(rankingRef, initialData)
            } else {
                // Document exists, update it
                val currentBonusPoints = snapshot.getLong("bonusPoints")?.toInt() ?: 0
                val totalPoints = snapshot.getLong("totalPoints")?.toInt() ?: 0
                val votes = snapshot.get("votes") as? Map<String, Map<String, Any>> ?: emptyMap()

                val previousPoints = (votes[userId]?.get("pointsGiven") as? Long)?.toInt() ?: 0
                val updatedTotalPoints = totalPoints - previousPoints + newPoints + currentBonusPoints

                val updatedVotes = votes.toMutableMap()
                updatedVotes[userId] = mapOf("pointsGiven" to newPoints)

                transaction.update(rankingRef, mapOf(
                    "memberId" to memberId,
                    "totalPoints" to updatedTotalPoints,
                    "votes" to updatedVotes
                ))
            }
        }.addOnSuccessListener {
            Log.d("Repository", "Transaction success: Ranking updated successfully.")
        }.addOnFailureListener { e ->
            Log.e("Repository", "Transaction failure: Failed to update ranking", e)
        }
    }
    fun updatePointsBasedOnVote(crRoomId: String, userId: String, hasVoted: Boolean) {
        val rankingRef = crGameRoomsCollection
            .document(crRoomId)
            .collection("Rankings")
            .document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(rankingRef)
            val currentPoints = snapshot.getLong("totalPoints")?.toInt() ?: 0
            val updatedPoints = if (hasVoted){
                currentPoints + 10
            } else {
                currentPoints - 10
            }
            transaction.update(rankingRef, "totalPoints", updatedPoints)
        }.addOnSuccessListener {
            Log.d("Repository", "Deducted 10 points from $userId successfully.")
        }.addOnFailureListener { e ->
            Log.e("Repository", "Failed to deduct points from $userId", e)
        }
    }

    suspend fun getQuestions(titleId: Int): List<Questions> {
        val response = SupabaseClient.client.postgrest["game"]
            .select(
                filter = {
                    filter("TitleId", FilterOperator.EQ, titleId)
                }
            )
        Log.d("Repository", "Raw response: ${response.body}")

        return response.decodeList()

    }
    suspend fun getAllQuestions(): List<Questions>{
        val response = SupabaseClient.client.postgrest["game"]
            .select()
        return response.decodeList()
    }



}