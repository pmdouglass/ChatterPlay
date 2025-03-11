package com.example.chatterplay.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.InternalAPI

class KtorApiRepository {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    @OptIn(InternalAPI::class)
    suspend fun updateAlert(crRoomId: String, alertType: String): String {
        val response: HttpResponse = client.post("http://10.0.2.2:8080/update-alert"){
            contentType(ContentType.Application.Json)
            body = UpdateAlertRequest(crRoomId, alertType)
        }
        return response.body<String>()
    }
}

data class UpdateAlertRequest(val crRoomId: String, val alertType: String)
