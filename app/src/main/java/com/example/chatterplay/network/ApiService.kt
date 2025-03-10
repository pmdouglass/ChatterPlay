package com.example.chatterplay.network

import retrofit2.http.GET

interface ApiService {
    @GET("/")
    fun getStatus(): Map<String, String>
}
