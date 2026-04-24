package kz.aruzhan.care_steps.data.remote

import kotlinx.coroutines.delay
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        // НЕ добавляем токен к auth запросам
        if (url.contains("/api/auth/register") ||
            url.contains("/api/auth/login") ||
            url.contains("/api/auth/refresh") ||
            url.contains("/api/auth/password-reset/")
        ) {
            return chain.proceed(originalRequest)
        }

        // Пути /media/… — статика Django, токен там не нужен.
        if (url.contains("/media/")) {
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

/**
 * Полностью буферизует тело ответа в память — лечит `unexpected end of stream` от gunicorn
 * при `Connection: close`. Для API-запросов.
 */
private class BufferResponseBodyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body ?: return response
        return try {
            val bytes = body.bytes()
            response.newBuilder()
                .body(bytes.toResponseBody(body.contentType()))
                .build()
        } catch (e: Exception) {
            response.close()
            throw e
        }
    }
}

/**
 * Повторяет запрос картинки до 3 раз при обрыве потока на чтении тела
 * (gunicorn + `Connection: close` на тяжёлых JPG в каталоге media).
 */
private class ImageRetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        var lastError: Throwable? = null
        var attempt = 0
        while (attempt < 3) {
            try {
                val response = chain.proceed(request)
                if (!response.isSuccessful) return response
                val body = response.body ?: return response
                try {
                    val bytes = body.bytes()
                    return response.newBuilder()
                        .body(bytes.toResponseBody(body.contentType()))
                        .build()
                } catch (e: Exception) {
                    response.close()
                    lastError = e
                }
            } catch (e: Exception) {
                lastError = e
            }
            attempt++
            try {
                Thread.sleep(150L * attempt)
            } catch (_: InterruptedException) {
            }
        }
        val err = lastError
        if (err is IOException) throw err
        throw IOException("imageRetryInterceptor failed", err)
    }
}

/** Ставит `Connection: keep-alive`, чтобы gunicorn не закрывал соединение после каждого GET. */
private class KeepAliveInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request().newBuilder()
            .header("Connection", "keep-alive")
            .build()
        return chain.proceed(req)
    }
}

object ApiClient {

    private const val BASE_URL = "http://91.201.215.251:8000/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /** Shared with Coil so images get the same auth / timeouts. */
    val imageOkHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(AuthInterceptor())
        .addInterceptor(ImageRetryInterceptor())
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }
        )
        .addNetworkInterceptor(KeepAliveInterceptor())
        .build()

    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(BufferResponseBodyInterceptor())
        .addInterceptor(logging)
        .addInterceptor(AuthInterceptor())
        .addNetworkInterceptor(KeepAliveInterceptor())
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Повтор при обрыве потока / сетевых сбоях (gunicorn + Connection: close).
     */
    suspend fun <T> withNetworkRetry(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 120L,
        block: suspend () -> T
    ): T {
        var last: Throwable? = null
        repeat(maxAttempts) { attempt ->
            try {
                return block()
            } catch (e: retrofit2.HttpException) {
                throw e
            } catch (e: Exception) {
                last = e
                val msg = e.message?.lowercase() ?: ""
                val retriable = e is IOException ||
                    msg.contains("unexpected end of stream") ||
                    msg.contains("connection reset") ||
                    msg.contains("protocol")
                if (!retriable || attempt == maxAttempts - 1) throw e
                delay(initialDelayMs * (attempt + 1))
            }
        }
        throw last ?: IllegalStateException("withNetworkRetry")
    }

    /**
     * Список специалистов с крупным лимитом страницы (если бэкенд поддерживает page_size/limit).
     */
    suspend fun loadAllSpecialistCards(
        q: String? = null,
        specializationSearch: String? = null
    ): List<SpecialistCardResponse> = withNetworkRetry {
        api.getSpecialistCards(
            q = q,
            specializationSearch = specializationSearch,
            pageSize = 500,
            limit = 500
        )
    }
}
