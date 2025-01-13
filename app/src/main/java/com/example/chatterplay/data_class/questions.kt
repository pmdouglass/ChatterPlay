package com.example.chatterplay.data_class

import kotlinx.serialization.Serializable

@Serializable
data class Question (
    val id: Int,
    val modeId: Int,
    val Question: String,
    val trueFalseAnswer: Boolean?,
    val TitleId: Int
)