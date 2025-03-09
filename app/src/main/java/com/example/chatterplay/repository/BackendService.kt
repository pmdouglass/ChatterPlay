package com.example.chatterplay.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json

class BackendService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getAlertType(roomId: String): String {
        val response: Map<String, String> = client.get("http://10.0.2.2:8080/alert/$roomId").body()
        return response["alertType"] ?: "unknown"
    }
}
