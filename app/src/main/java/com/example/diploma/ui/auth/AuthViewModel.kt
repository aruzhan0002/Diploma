package kz.aruzhan.care_steps.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kz.aruzhan.care_steps.data.remote.*
import kz.aruzhan.care_steps.domain.model.ParentRelationship
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private fun parseApiError(rawBody: String?, fallback: String): String {
        if (rawBody.isNullOrBlank()) return fallback
        return runCatching {
            val obj = JSONObject(rawBody)
            when {
                obj.has("detail") -> obj.optString("detail")
                obj.has("message") -> obj.optString("message")
                obj.has("code") -> obj.optString("code")
                else -> fallback
            }.ifBlank { fallback }
        }.getOrDefault(fallback)
    }

    private suspend fun loginWithRetryAfterRegister(
        normalizedEmail: String,
        password: String
    ): TokenResponse {
        var lastError: Throwable? = null
        repeat(4) { attempt ->
            try {
                return ApiClient.withNetworkRetry {
                    ApiClient.api.login(
                        LoginRequest(email = normalizedEmail, password = password)
                    )
                }
            } catch (e: Exception) {
                lastError = e
                if (attempt < 3) {
                    // Backend can still finish user creation; short backoff prevents false failure.
                    delay(400L * (attempt + 1))
                }
            }
        }
        throw lastError ?: IllegalStateException("Не удалось выполнить вход")
    }

    fun register(
        email: String,
        password: String,
        passwordConfirm: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val normalizedEmail = email.trim().lowercase()

                val response: Response<Unit> =
                    ApiClient.withNetworkRetry {
                        ApiClient.api.register(
                        RegisterRequest(
                            email = normalizedEmail,
                            password = password,
                            password_confirm = passwordConfirm
                        )
                    )
                }

                if (!response.isSuccessful) {
                    onError("Ошибка регистрации: ${response.code()}")
                    return@launch
                }

                println("✅ Регистрация успешна: ${response.code()}")

                // --- Автологин после регистрации ---
                try {
                    val loginResponse = loginWithRetryAfterRegister(
                        normalizedEmail = normalizedEmail,
                        password = password
                    )

                    // Сохраняем токены
                    TokenStorage.saveTokens(
                        getApplication(),
                        loginResponse.access,
                        loginResponse.refresh
                    )
                    TokenStorage.saveUserKey(getApplication(), normalizedEmail)

                    println("✅ Токен получен: ${loginResponse.access.take(20)}...")
                    onSuccess()

                } catch (e: Exception) {
                    println("❌ Ошибка при автологине: ${e.message}")
                    onError("Регистрация успешна, но автовход временно не сработал. Войдите вручную.")
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
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val normalizedEmail = email.trim().lowercase()
                val tokens = ApiClient.api.login(
                    LoginRequest(email = normalizedEmail, password = password)
                )

                TokenStorage.saveTokens(
                    getApplication(),
                    tokens.access,
                    tokens.refresh
                )
                TokenStorage.saveUserKey(getApplication(), normalizedEmail)

                println("✅ Токен сохранен")
                onSuccess(tokens.user_type)

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

    fun requestPasswordResetCode(
        email: String,
        onSuccess: (retryAfterSeconds: Int?) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val normalizedEmail = email.trim().lowercase()
                val response = ApiClient.api.requestPasswordResetCode(
                    PasswordResetRequestBody(email = normalizedEmail)
                )
                if (response.isSuccessful) {
                    val retry = response.body()?.retry_after_seconds
                        ?: response.body()?.resend_after_seconds
                    onSuccess(retry)
                } else {
                    val fallback = when (response.code()) {
                        429 -> "Слишком часто. Подождите перед повторной отправкой."
                        else -> "Не удалось отправить код"
                    }
                    onError(parseApiError(response.errorBody()?.string(), fallback))
                }
            } catch (e: HttpException) {
                onError("Ошибка сервера: ${e.code()}")
            } catch (e: IOException) {
                onError("Ошибка сети")
            } catch (e: Exception) {
                onError(e.message ?: "Не удалось отправить код")
            }
        }
    }

    fun verifyPasswordResetCode(
        email: String,
        code: String,
        onSuccess: (resetToken: String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.verifyPasswordResetCode(
                    PasswordResetVerifyBody(email = email.trim().lowercase(), code = code)
                )
                if (response.isSuccessful) {
                    val token = response.body()?.reset_token.orEmpty()
                    if (token.isBlank()) {
                        onError("Не получили reset token от сервера")
                    } else {
                        onSuccess(token)
                    }
                } else {
                    onError(
                        parseApiError(
                            response.errorBody()?.string(),
                            "Неверный код подтверждения"
                        )
                    )
                }
            } catch (e: HttpException) {
                onError("Ошибка сервера: ${e.code()}")
            } catch (e: IOException) {
                onError("Ошибка сети")
            } catch (e: Exception) {
                onError(e.message ?: "Ошибка проверки кода")
            }
        }
    }

    fun confirmPasswordReset(
        email: String,
        resetToken: String,
        newPassword: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.confirmPasswordReset(
                    PasswordResetConfirmBody(
                        reset_token = resetToken,
                        new_password = newPassword,
                        new_password_confirm = newPassword
                    )
                )
                if (!response.isSuccessful) {
                    onError(
                        parseApiError(
                            response.errorBody()?.string(),
                            "Не удалось обновить пароль"
                        )
                    )
                    return@launch
                }

                val body = response.body()
                val access = body?.access
                val refresh = body?.refresh
                if (!access.isNullOrBlank() && !refresh.isNullOrBlank()) {
                    TokenStorage.saveTokens(getApplication(), access, refresh)
                    TokenStorage.saveUserKey(getApplication(), email.trim().lowercase())
                    onSuccess(body?.user_type)
                    return@launch
                }

                // Фолбэк: если backend вернул 200 без токенов — логинимся новым паролем.
                val tokens = ApiClient.api.login(
                    LoginRequest(
                        email = email.trim().lowercase(),
                        password = newPassword
                    )
                )
                TokenStorage.saveTokens(getApplication(), tokens.access, tokens.refresh)
                TokenStorage.saveUserKey(getApplication(), email.trim().lowercase())
                onSuccess(tokens.user_type)
            } catch (e: HttpException) {
                onError("Ошибка сервера: ${e.code()}")
            } catch (e: IOException) {
                onError("Ошибка сети")
            } catch (e: Exception) {
                onError(e.message ?: "Не удалось завершить сброс пароля")
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
                val request = ProfileRequest(
                    full_name = fullName,
                    relationship = relationship.apiValue,
                    relationship_other = if (relationship == ParentRelationship.OTHER) otherText else null
                )

                val response = ApiClient.api.createProfile(
                    request
                )

                if (response.isSuccessful) {
                    println("✅ Профиль создан (код ${response.code()})")
                    onSuccess()
                } else {
                    val err = response.errorBody()?.string()
                    println("❌ Ошибка createProfile: ${response.code()} $err")
                    val alreadyCreated = response.code() == 400 &&
                        (
                            err?.contains("Профиль уже создан", ignoreCase = true) == true ||
                                err?.contains("already", ignoreCase = true) == true
                            )
                    if (alreadyCreated) {
                        val patchResp = ApiClient.api.patchProfile(request)
                        if (patchResp.isSuccessful) {
                            println("✅ Профиль обновлен через PATCH (код ${patchResp.code()})")
                            onSuccess()
                        } else {
                            val patchErr = patchResp.errorBody()?.string()
                            println("❌ Ошибка patchProfile: ${patchResp.code()} $patchErr")
                            onError(
                                parseApiError(
                                    patchErr,
                                    "Не удалось обновить профиль: ${patchResp.code()}"
                                )
                            )
                        }
                    } else {
                        onError(parseApiError(err, "Ошибка сервера: ${response.code()}"))
                    }
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