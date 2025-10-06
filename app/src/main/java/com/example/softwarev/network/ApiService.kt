package com.example.softwarev.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: Int,
    val name: String
)

interface ApiService {
    @POST("login/") // URL del endpoint de login del DRF
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
