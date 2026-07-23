package com.example.bike_app_ui.Auth

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class AuthRequest(
    val email: String,
    val password: String,
    val name: String = ""
)
@Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val name: String
)
@Serializable
data class LoginResponse(
    val message: String,
    val user: UserResponse
)

interface AuthApiService {
    @POST("register")
    suspend fun register(@Body request: AuthRequest): UserResponse

    @POST("login")
    suspend fun login(@Body request: AuthRequest): LoginResponse
}

object AuthApiClient {
    private  const val BASE_URL = "http://10.0.2.2:8080/"
    private val json = Json { ignoreUnknownKeys = true }
    val service: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthApiService::class.java)
    }

}