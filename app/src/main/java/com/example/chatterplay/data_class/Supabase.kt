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
    val modeId: Int,
    val Question: String,
    val TitleId: Int
)

@Serializable
data class Answers(
    val crRoomId: String,
    val userId: String,
    val titleId: Int,
    val questionId: Int,
    val question: String,
    val answerPair: Boolean
)
@Serializable
data class GameTitle(
    val id: Int,
    val title: String
)