package com.example.chatterplay.data_class

import com.example.chatterplay.BuildConfig
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object SupabaseClient {
    @OptIn(SupabaseExperimental::class)
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(GoTrue)
        install(Postgrest) {
            serializer = KotlinXSerializer(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        install(Storage)
    }
}

@Serializable
data class Questions (
    val id: Int,
    val mode: String,
    val question: String,
    val title: String,
    val type: String,
    val choice1: String? = null,
    val choice2: String? = null,
    val choice3: String? = null,
    val choice4: String? = null
)

@Serializable
data class Answers(
    val userId: String,
    val questionId: Int,
    val question: String,
    val answerPair: Boolean? = null,
    val crRoomId: String,
    val title: String? = null,
    val choice: String? = null
)
@Serializable
data class Title(
    val id: Int,
    val title: String,
    val mode: String,
    val type: String
)
@Serializable
data class askQuestion(
    val crRoomId: String,
    val question: String,
    val userId: String,
    val toUserId: String
)
