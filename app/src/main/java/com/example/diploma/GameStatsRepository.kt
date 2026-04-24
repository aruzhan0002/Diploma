package com.example.diploma

import android.content.Context
import android.util.Base64
import com.example.diploma.data.remote.TokenStorage
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TodayGameStats(
    val playedCount: Int,
    val wonCount: Int,
    val totalMinutes: Int,
    val lastGameTitle: String
)

object GameStatsRepository {
    private const val PREFS_NAME = "child_game_stats"
    private const val KEY_SESSIONS_PREFIX = "sessions_"
    private const val MAX_SESSIONS = 200

    private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun currentSessionsKey(): String {
        val suffix = currentAccountFingerprint()
        return KEY_SESSIONS_PREFIX + suffix
    }

    private fun currentAccountFingerprint(): String {
        val explicitUserKey = TokenStorage.userKey.orEmpty().trim().lowercase(Locale.US)
        if (explicitUserKey.isNotBlank()) return explicitUserKey

        val access = TokenStorage.accessToken.orEmpty()
        val refresh = TokenStorage.refreshToken.orEmpty()

        val accessClaim = jwtStableClaim(access)
        if (accessClaim.isNotBlank()) return accessClaim

        val refreshClaim = jwtStableClaim(refresh)
        if (refreshClaim.isNotBlank()) return refreshClaim

        val source = when {
            refresh.isNotBlank() -> refresh
            access.isNotBlank() -> access
            else -> "guest"
        }
        return source.hashCode().toString()
    }

    private fun migrateLegacyIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentKey = currentSessionsKey()
        val currentRaw = prefs.getString(currentKey, null)
        if (!currentRaw.isNullOrBlank()) return

        // If there is exactly one legacy sessions_* key with data, migrate it.
        val all = prefs.all
        val legacyCandidates = all.keys
            .filter { it.startsWith(KEY_SESSIONS_PREFIX) && it != currentKey }
            .mapNotNull { key ->
                val raw = prefs.getString(key, null)
                if (raw.isNullOrBlank()) null else key to raw
            }

        if (legacyCandidates.isEmpty()) return

        val picked = legacyCandidates.maxByOrNull { (_, raw) ->
            runCatching { JSONArray(raw).length() }.getOrDefault(0)
        }?.second
        if (!picked.isNullOrBlank()) {
            prefs.edit().putString(currentKey, picked).apply()
        }
    }

    private fun jwtStableClaim(token: String): String {
        if (token.isBlank()) return ""
        val parts = token.split(".")
        if (parts.size < 2) return ""
        val payload = runCatching {
            val decoded = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            String(decoded, Charsets.UTF_8)
        }.getOrNull() ?: return ""

        val obj = runCatching { JSONObject(payload) }.getOrNull() ?: return ""
        val candidate = listOf("user_id", "userId", "sub", "username", "email")
            .firstNotNullOfOrNull { key -> obj.optString(key).takeIf { it.isNotBlank() } }
            ?: return ""
        return candidate.lowercase(Locale.US)
    }

    fun recordSession(
        context: Context,
        gameTitle: String,
        durationSeconds: Int,
        won: Boolean
    ) {
        migrateLegacyIfNeeded(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getString(currentSessionsKey(), "[]").orEmpty()
        val arr = runCatching { JSONArray(current) }.getOrElse { JSONArray() }

        val item = JSONObject().apply {
            put("ts", System.currentTimeMillis())
            put("date", todayKey())
            put("gameTitle", gameTitle)
            put("durationSec", durationSeconds.coerceAtLeast(0))
            put("won", won)
        }
        arr.put(item)

        while (arr.length() > MAX_SESSIONS) {
            arr.remove(0)
        }

        prefs.edit().putString(currentSessionsKey(), arr.toString()).apply()
    }

    fun todayStats(context: Context): TodayGameStats {
        migrateLegacyIfNeeded(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getString(currentSessionsKey(), "[]").orEmpty()
        val arr = runCatching { JSONArray(current) }.getOrElse { JSONArray() }
        val today = todayKey()

        var played = 0
        var won = 0
        var totalSec = 0
        var lastTs = -1L
        var lastTitle = "—"

        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            if (obj.optString("date") != today) continue

            played += 1
            if (obj.optBoolean("won", false)) won += 1
            totalSec += obj.optInt("durationSec", 0)

            val ts = obj.optLong("ts", 0L)
            if (ts >= lastTs) {
                lastTs = ts
                lastTitle = obj.optString("gameTitle", "—")
            }
        }

        val minutes = (totalSec / 60).coerceAtLeast(if (played > 0) 1 else 0)
        return TodayGameStats(
            playedCount = played,
            wonCount = won,
            totalMinutes = minutes,
            lastGameTitle = lastTitle
        )
    }
}

