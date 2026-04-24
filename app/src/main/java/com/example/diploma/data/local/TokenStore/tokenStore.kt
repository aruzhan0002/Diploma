package kz.aruzhan.care_steps.data.remote

import android.content.Context
import android.content.SharedPreferences

object TokenStorage {
    var accessToken: String? = null
    var refreshToken: String? = null
    var userKey: String? = null

    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_USER = "user_key"

    // Сохраняем токены
    fun saveTokens(context: Context, access: String, refresh: String) {
        accessToken = access
        refreshToken = refresh

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_ACCESS, access)
            putString(KEY_REFRESH, refresh)
            apply()
        }
    }

    // Загружаем токены
    fun loadTokens(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        accessToken = prefs.getString(KEY_ACCESS, null)
        refreshToken = prefs.getString(KEY_REFRESH, null)
        userKey = prefs.getString(KEY_USER, null)
    }

    fun saveUserKey(context: Context, value: String) {
        val normalized = value.trim().lowercase()
        if (normalized.isBlank()) return
        userKey = normalized
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USER, normalized).apply()
    }

    // Очищаем токены
    fun clearTokens(context: Context) {
        accessToken = null
        refreshToken = null
        userKey = null

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    // Проверка авторизации
    fun isLoggedIn(): Boolean = !accessToken.isNullOrBlank()
}