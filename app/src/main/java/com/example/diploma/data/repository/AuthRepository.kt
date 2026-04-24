package com.example.diploma.data.repository

import com.example.diploma.data.remote.*

class AuthRepository(
    private val api: ApiService
) {
    suspend fun register(email: String, pass: String, pass2: String) =
        api.register(RegisterRequest(email = email, password = pass, password_confirm = pass2))

    suspend fun login(email: String, pass: String) =
        api.login(LoginRequest(email = email, password = pass))

    suspend fun refresh(refreshToken: String) =
        api.refresh(RefreshRequest(refresh = refreshToken))
}
