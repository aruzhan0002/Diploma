package com.example.diploma.data.remote

import android.content.Context
import android.content.SharedPreferences

object TokenStorage {
    var accessToken: String? = null
    var refreshToken: String? = null

    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"

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
    }

    // Очищаем токены
    fun clearTokens(context: Context) {
        accessToken = null
        refreshToken = null

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    // Проверка авторизации
    fun isLoggedIn(): Boolean = !accessToken.isNullOrBlank()
}