package com.example.chatterplay.ApiService

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface RailwayApiService {
    @POST("rooms")
    suspend fun createRoom(@Body request: RoomRequest)
}

data class RoomRequest(
    val roomId: String,
    val createdAt: Long
)

object RetrofitClient{
    private const val BASE_URL = ""

    val apiService: RailwayApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RailwayApiService::class.java)
    }
}