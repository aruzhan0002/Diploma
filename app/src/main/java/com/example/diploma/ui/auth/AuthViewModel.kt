package com.example.diploma.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.diploma.data.remote.*
import com.example.diploma.domain.model.ParentRelationship
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    fun register(
        email: String,
        password: String,
        passwordConfirm: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {

                val response: Response<RegisterResponse> =
                    ApiClient.api.register(
                        RegisterRequest(
                            email = email,
                            password = password,
                            password_confirm = passwordConfirm
                        )
                    )

                if (!response.isSuccessful) {
                    onError("Ошибка регистрации: ${response.code()}")
                    return@launch
                }

                println("✅ Регистрация успешна: ${response.code()}")

                // --- Автологин после регистрации ---
                try {
                    delay(500)

                    val loginResponse = ApiClient.api.login(
                        LoginRequest(username = email, password = password)
                    )

                    // Сохраняем токены
                    TokenStorage.saveTokens(
                        getApplication(),
                        loginResponse.access,
                        loginResponse.refresh
                    )

                    println("✅ Токен получен: ${loginResponse.access.take(20)}...")
                    onSuccess()

                } catch (e: Exception) {
                    println("❌ Ошибка при автологине: ${e.message}")
                    onError("Регистрация успешна, но не удалось выполнить вход")
                }

            } catch (e: HttpException) {
                println("❌ HTTP ошибка: ${e.code()} - ${e.message()}")
                when (e.code()) {
                    400 -> onError("Пользователь уже существует")
                    else -> onError("Ошибка сервера: ${e.code()}")
                }
            } catch (e: IOException) {
                println("❌ Сетевая ошибка: ${e.message}")
                onError("Ошибка сети. Проверьте подключение")
            } catch (e: Exception) {
                println("❌ Неизвестная ошибка: ${e.message}")
                onError("Произошла неизвестная ошибка")
            }
        }
    }


    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val tokens = ApiClient.api.login(
                    LoginRequest(username = email, password = password)
                )

                TokenStorage.saveTokens(
                    getApplication(),
                    tokens.access,
                    tokens.refresh
                )

                println("✅ Токен сохранен")
                onSuccess()

            } catch (e: HttpException) {
                println("❌ HTTP ошибка: ${e.code()}")
                onError("Ошибка сервера: ${e.code()}")
            } catch (e: IOException) {
                println("❌ Сетевая ошибка")
                onError("Ошибка сети")
            } catch (e: Exception) {
                println("❌ Ошибка: ${e.message}")
                onError(e.message ?: "LOGIN ERROR")
            }
        }
    }


    fun createProfile(
        fullName: String,
        relationship: ParentRelationship,
        otherText: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (TokenStorage.accessToken.isNullOrBlank()) {
                    onError("Токен не найден. Сначала выполните вход.")
                    return@launch
                }

                println("📤 Отправка профиля с токеном: ${TokenStorage.accessToken?.take(20)}...")

                val response = ApiClient.api.createProfile(
                    ProfileRequest(
                        full_name = fullName,
                        relationship = relationship.apiValue,
                        relationship_other = if (relationship == ParentRelationship.OTHER) otherText else null
                    )
                )

                if (response.isSuccessful) {
                    println("✅ Профиль создан (код ${response.code()})")
                    onSuccess()
                } else {
                    val err = response.errorBody()?.string()
                    println("❌ Ошибка createProfile: ${response.code()} $err")
                    onError("Ошибка сервера: ${response.code()}")
                }

            } catch (e: IOException) {
                println("❌ Сетевая ошибка: ${e.message}")
                e.printStackTrace()
                onError("Ошибка сети: ${e.message}")
            } catch (e: Exception) {
                println("❌ Ошибка: ${e.message}")
                e.printStackTrace()
                onError(e.message ?: "PROFILE ERROR")
            }
        }
    }
}