package com.example.diploma.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

// ---------- REQUESTS ----------
data class RegisterRequest(
    val email: String,
    val password: String,
    val password_confirm: String
)



// СТАЛО:
data class LoginRequest(
    val username: String,  // ИЗМЕНИЛИ С email НА username
    val password: String
)

data class RefreshRequest(
    val refresh: String
)

data class ProfileRequest(
    val full_name: String,
    val relationship: String,
    val relationship_other: String? = null
)

// ---------- RESPONSES ----------
data class RegisterResponse(
    val message: String?,
    val user_id: Int?
)

data class TokenResponse(
    val access: String,
    val refresh: String
)

data class AccessResponse(
    val access: String
)

data class ProfileResponse(
    val id: Int,
    val email: String,
    val full_name: String,
    val relationship: String,
    val relationship_other: String?
)


