package com.example.chatterplay.data_class

import kotlinx.serialization.json.Json

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
    val selectedProfile: String = "self",
    val gameRoomId: String = "0"
)

data class DateOfBirth(
    val month: String = "",
    val day: String = "",
    val year: String = ""
)
