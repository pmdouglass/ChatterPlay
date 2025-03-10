package com.example.backend

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation){
        json()
    }

    routing {
        get("/") {
            call.respondText("Server is running on port 8080!", status = HttpStatusCode.OK)
        }
        post("/update-alert") {
            val requestData = call.receive<UpdateAlertRequest>()
            println("Recieved Request: ${requestData.crRoomId}, ${requestData.alertType}")
            call.respond(HttpStatusCode.OK, "Alert Updated Successfully")
        }
    }

}

data class UpdateAlertRequest(val crRoomId: String, val alertType: String)