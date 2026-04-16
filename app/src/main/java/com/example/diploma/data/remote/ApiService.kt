package com.example.diploma.data.remote


import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

interface ApiService {

    @POST("api/auth/register/")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/login/")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("api/auth/refresh/")
    suspend fun refresh(@Body body: RefreshRequest): AccessResponse

    @POST("api/auth/profile/")
    suspend fun createProfile(@Body body: ProfileRequest): Response<Unit>

    @POST("api/auth/children/")
    suspend fun createChildProfile(@Body body: CreateChildRequest): retrofit2.Response<ChildProfileResponse>
}