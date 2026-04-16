package com.example.diploma.data.remote

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit



//class AuthInterceptor : Interceptor {
//    override fun intercept(chain: Interceptor.Chain): Response {
//        val originalRequest = chain.request()
//
//        // Добавляем токен, если он есть
//        val requestWithToken = TokenStorage.accessToken?.let { token ->
//            originalRequest.newBuilder()
//                .addHeader("Authorization", "Bearer $token")
//                .build()
//        } ?: originalRequest
//
//        return chain.proceed(requestWithToken)
//    }
//}

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        // НЕ добавляем токен к auth запросам
        if (url.contains("/api/auth/register") ||
            url.contains("/api/auth/login") ||
            url.contains("/api/auth/refresh")
        ) {
            return chain.proceed(originalRequest)
        }

        val token = TokenStorage.accessToken

        val request = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}

object ApiClient {

    private const val BASE_URL = "http://91.201.215.251:8000/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // 👈 Увеличиваем таймауты
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true) // 👈 Повторяем при ошибке
        .addInterceptor(logging)
        .addInterceptor(AuthInterceptor())
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}