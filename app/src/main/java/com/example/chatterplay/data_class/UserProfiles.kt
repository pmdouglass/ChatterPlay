package com.example.chatterplay.data_class

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val userId: String = "",
    val fname: String = "",
    val lname: String = "",
    val gender: String = "",
    val dob: DateOfBirth = DateOfBirth("December", "25", "2024"),
    val age: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val about: String = "",
    val pending: String = "NotPending",
)

data class DateOfBirth(
    val month: String = "",
    val day: String = "",
    val year: String = "",
){
    val birthday: String
        get() = "$month $day $year"
}

