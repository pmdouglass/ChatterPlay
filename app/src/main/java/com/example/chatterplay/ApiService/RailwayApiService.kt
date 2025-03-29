package com.example.chatterplay.ApiService

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface RailwayApiService {
    @POST("createRoom")
    suspend fun createRoom(@Body request: RoomRequest): Response<Unit>

}

data class RoomRequest(
    @SerializedName("crRoomId")
    val crRoomId: String
)

object RetrofitClient{

    private const val BASE_URL = "https://gameroomdata-production.up.railway.app/"

    val apiService: RailwayApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RailwayApiService::class.java)
    }
}

class RailwayViewModel(): ViewModel(){


    fun createRoom(crRoomId: String){
        viewModelScope.launch {
            try {
                val request = RoomRequest(crRoomId)
                Log.d("RailwayViewModel", "Creating room with request: $request")
                val response = RetrofitClient.apiService.createRoom(request)
                Log.e("RailwayViewModel", "Error Body: ${response.errorBody()?.string()}")

                if (response.isSuccessful){
                    Log.d("RailwayViewModel", "Room Created successfully")
                }else {
                    Log.e("RailwayViewModel", "Failed to create room. Error Code: ${response.code()}, Message: ${response.message()}, response $response")
                }
            }catch (e: Exception){
                Log.e("RailwayViewModel", "Error: ${e.message}")
            }
        }
    }
}