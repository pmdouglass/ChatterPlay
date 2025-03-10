package com.example.chatterplay.network

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("/")
    suspend fun getStatus(): Response<Map<String, String>>
}
