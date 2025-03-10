package com.example.chatterplay.network

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("/")
    fun getStatus(): Call<Map<String, String>>
}
