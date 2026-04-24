package com.example.diploma.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.diploma.data.remote.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class SpecialistViewModel(application: Application) : AndroidViewModel(application) {
    private fun parseApiError(rawBody: String?, fallback: String): String {
        if (rawBody.isNullOrBlank()) return fallback
        return runCatching {
            val obj = JSONObject(rawBody)
            when {
                obj.has("detail") -> obj.optString("detail")
                obj.has("message") -> obj.optString("message")
                else -> fallback
            }.ifBlank { fallback }
        }.getOrDefault(fallback)
    }

    private fun looksLikeAlreadyCreated(rawBody: String?): Boolean {
        val text = rawBody.orEmpty().lowercase()
        return text.contains("уже") || text.contains("already") || text.contains("exists")
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
                    delay(400L * (attempt + 1))
                }
            }
        }
        throw lastError ?: IllegalStateException("Не удалось выполнить вход")
    }

    // Page 1
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    // Page 2
    var fullName by mutableStateOf("")
    var approachDescription by mutableStateOf("")

    // Page 3
    var specialization by mutableStateOf("ABA-терапия")
    var specializationOther by mutableStateOf("")

    // Page 4
    var yearsExperience by mutableFloatStateOf(5f)
    var method by mutableStateOf("ABA")
    var methodOther by mutableStateOf("")

    // Page 5
    var ageRange by mutableStateOf("2-4, Подростки")
    var developmentType by mutableStateOf("Аутизм (РАС)")
    var developmentTypeOther by mutableStateOf("")
    var workFormat by mutableStateOf("Онлайн")

    // Page 6
    var selectedLanguages by mutableStateOf(setOf("Русский"))
    var timeZone by mutableStateOf("UTC/GMT +5 часов")
    var city by mutableStateOf("Алматы")

    // Page 7
    var provideConsultations by mutableStateOf(true)
    var workViaParent by mutableStateOf(false)
    var provideRecommendations by mutableStateOf(true)
    var trackAnalytics by mutableStateOf(false)

    fun register(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val normalizedEmail = email.trim().lowercase()
                val response = ApiClient.withNetworkRetry {
                    ApiClient.api.register(
                        RegisterRequest(
                            email = normalizedEmail,
                            password = password,
                            password_confirm = confirmPassword
                        )
                    )
                }

                if (!response.isSuccessful) {
                    onError("Ошибка регистрации: ${response.code()}")
                    return@launch
                }

                try {
                    val loginResponse = loginWithRetryAfterRegister(
                        normalizedEmail = normalizedEmail,
                        password = password
                    )
                    TokenStorage.saveTokens(
                        getApplication(),
                        loginResponse.access,
                        loginResponse.refresh
                    )
                    TokenStorage.saveUserKey(getApplication(), normalizedEmail)
                    onSuccess()
                } catch (e: Exception) {
                    onError("Регистрация успешна, но автовход временно не сработал. Войдите вручную.")
                }

            } catch (e: HttpException) {
                when (e.code()) {
                    400 -> onError("Пользователь уже существует")
                    else -> onError("Ошибка сервера: ${e.code()}")
                }
            } catch (e: IOException) {
                onError("Ошибка сети. Проверьте подключение")
            } catch (e: Exception) {
                onError("Произошла неизвестная ошибка")
            }
        }
    }

    fun createSpecialistProfile(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = SpecialistRequest(
                    full_name = fullName,
                    approach_description = approachDescription
                )
                val response = ApiClient.api.createSpecialist(request)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val err = response.errorBody()?.string()
                    val alreadyCreated = (response.code() == 400 || response.code() == 409) &&
                        looksLikeAlreadyCreated(err)
                    if (alreadyCreated) {
                        val patchResp = ApiClient.api.patchSpecialist(request)
                        if (patchResp.isSuccessful) {
                            onSuccess()
                        } else {
                            val patchErr = patchResp.errorBody()?.string()
                            onError(parseApiError(patchErr, "Не удалось обновить профиль специалиста"))
                        }
                    } else {
                        onError(parseApiError(err, "Ошибка: ${response.code()}"))
                    }
                }
            } catch (e: IOException) {
                onError("Ошибка сети: ${e.message}")
            } catch (e: Exception) {
                onError(e.message ?: "Ошибка создания профиля")
            }
        }
    }

    fun submitSpecialistDescription(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val finalSpecialization = if (specialization == "Другое") specializationOther else specialization
                val finalMethod = if (method == "Другое") methodOther else method
                val finalDevType = if (developmentType == "Другое") developmentTypeOther else developmentType
                val formatApi = if (workFormat == "Офлайн") "offline" else "online"

                val request = SpecialistDescriptionRequest(
                    specializations = finalSpecialization,
                    years_experience = yearsExperience.toInt(),
                    methods = finalMethod,
                    age_range = ageRange,
                    development_types = finalDevType,
                    work_format = formatApi,
                    languages = selectedLanguages.joinToString(", "),
                    time_zone = timeZone,
                    city = city,
                    provide_individual_consultations = provideConsultations,
                    work_with_child_through_parent = workViaParent,
                    provide_recommendations_and_plans = provideRecommendations,
                    track_progress_and_analytics = trackAnalytics
                )
                val response = ApiClient.api.createSpecialistDescription(request)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val err = response.errorBody()?.string()
                    val alreadyCreated = (response.code() == 400 || response.code() == 409) &&
                        looksLikeAlreadyCreated(err)
                    if (alreadyCreated) {
                        val patchResp = ApiClient.api.patchSpecialistDescription(request)
                        if (patchResp.isSuccessful) {
                            onSuccess()
                        } else {
                            val patchErr = patchResp.errorBody()?.string()
                            onError(parseApiError(patchErr, "Не удалось обновить описание специалиста"))
                        }
                    } else {
                        onError(parseApiError(err, "Ошибка: ${response.code()}"))
                    }
                }
            } catch (e: IOException) {
                onError("Ошибка сети: ${e.message}")
            } catch (e: Exception) {
                onError(e.message ?: "Ошибка отправки данных")
            }
        }
    }
}
