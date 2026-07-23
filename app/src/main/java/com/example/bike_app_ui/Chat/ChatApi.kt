package com.example.bike_app_ui.Chat

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

@Serializable
data class RegisterRoomRequest(
    val hostName: String,
    val port: Int = 8080
)

@Serializable
data class RoomResponse(
    val id: Int,
    val hostName: String,
    val hostIp: String,
    val port: Int
)

interface RoomApiService {
    @GET("rooms")
    suspend fun fetchRooms(): List<RoomResponse>

    @POST("rooms")
    suspend fun createRooms(@Body request: RegisterRoomRequest) : RoomResponse

    @DELETE("rooms/{id}")
    suspend fun deleteRoom(@Path("id") id: Int)

}

object ChatApiClient {
    private  const val BASE_URL = "http://10.0.2.2:8080/"

    private val json = Json { ignoreUnknownKeys = true }
    val service: RoomApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(RoomApiService::class.java)
    }
}