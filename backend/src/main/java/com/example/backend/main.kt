package com.example.backend

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }
        routing {
            get("/") {
                call.respondText("Welcome to ChatterPlay Backend!")
            }

            // Example: API to get system alert type
            get("/alert/{roomId}") {
                val roomId = call.parameters["roomId"]
                // Here you would fetch alert data from a database
                call.respond(mapOf("roomId" to roomId, "alertType" to "game"))
            }
        }
    }.start(wait = true)
}

