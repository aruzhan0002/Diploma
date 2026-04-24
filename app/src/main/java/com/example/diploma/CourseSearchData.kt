package kz.aruzhan.care_steps

import kz.aruzhan.care_steps.data.remote.ApiClient
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class SearchableCourse(
    val id: Int,
    val title: String,
    val author: String,
    val price: Double,
    val rating: Double,
    val audienceLabel: String,
    val level: String
)

object CourseSearchRepository {

    private val allCourses = listOf(
        SearchableCourse(1, "Основы Аутизма", "Алина Захарова", 12000.0, 4.9, "РОДИТЕЛЯМ", "Начальный"),
        SearchableCourse(2, "Основы Аутизма", "Алина Захарова", 12000.0, 4.9, "РОДИТЕЛЯМ", "Начальный"),
        SearchableCourse(3, "Развитие моторики", "Алина Киримбаева", 15000.0, 4.7, "РОДИТЕЛЯМ", "Базовый"),
        SearchableCourse(4, "Речевая терапия", "Карим Мардан", 18000.0, 4.5, "РОДИТЕЛЯМ", "Базовый"),
        SearchableCourse(5, "ABA-терапия для начинающих", "Мария Иванова", 25000.0, 5.0, "СПЕЦИАЛИСТАМ", "Продвинутый"),
        SearchableCourse(6, "Сенсорная интеграция", "Алина Захарова", 20000.0, 4.8, "РОДИТЕЛЯМ", "Начальный"),
        SearchableCourse(7, "Нейропсихология детства", "Дана Султанова", 32000.0, 4.6, "СПЕЦИАЛИСТАМ", "Продвинутый"),
        SearchableCourse(8, "Коммуникация и социализация", "Карим Мардан", 14000.0, 4.4, "РОДИТЕЛЯМ", "Базовый")
    )

    private val levelRuByCode = mapOf(
        "beginner" to "Начальный",
        "intermediate" to "Базовый",
        "advanced" to "Продвинутый"
    )

    fun search(query: String, filters: CourseFilters): List<SearchableCourse> {
        var result = allCourses

        if (query.isNotBlank()) {
            val q = query.lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                    it.author.lowercase().contains(q) ||
                    it.audienceLabel.lowercase().contains(q)
            }
        }

        if (filters.selectedRatings.isNotEmpty()) {
            val minRating = filters.selectedRatings.minOf { it.toDoubleOrNull() ?: 0.0 }
            result = result.filter { it.rating >= minRating }
        }

        result = result.filter { it.price in filters.priceMin.toDouble()..filters.priceMax.toDouble() }

        if (filters.selectedLevels.isNotEmpty()) {
            val selectedRu = filters.selectedLevels.mapNotNull { levelRuByCode[it] }
            result = result.filter { it.level in selectedRu }
        }

        return result
    }

    /**
     * GET /api/courses/public/cards/ — параметры: title, rating_min, price_min, price_max, level (как в Swagger).
     */
    suspend fun searchFromApi(query: String, filters: CourseFilters): List<SearchableCourse> {
        return try {
            val ratingMin: Double? = if (filters.selectedRatings.isNotEmpty()) {
                filters.selectedRatings.minOf { it.toDoubleOrNull() ?: 0.0 }
            } else {
                null
            }

            val levelForApi: String? = when (filters.selectedLevels.size) {
                1 -> filters.selectedLevels.first()
                else -> null
            }

            val results = ApiClient.withNetworkRetry {
                ApiClient.api.getCourseCards(
                    title = query.trim().ifBlank { null },
                    ratingMin = ratingMin,
                    priceMin = filters.priceMin.toInt(),
                    priceMax = filters.priceMax.toInt(),
                    level = levelForApi
                )
            }
            var list = results.map { it.toSearchableCourse() }

            if (filters.selectedLevels.size > 1) {
                val allowedRu = filters.selectedLevels.mapNotNull { levelRuByCode[it] }.toSet()
                list = list.filter { it.level in allowedRu }
            }

            list
        } catch (_: Exception) {
            search(query, filters)
        }
    }
}

class CourseFilters {
    val selectedRatings = mutableStateListOf<String>()
    var priceMin by mutableStateOf(5000f)
    var priceMax by mutableStateOf(32990f)
    val selectedLevels = mutableStateListOf<String>()

    val activeCount: Int
        get() {
            var count = 0
            if (selectedRatings.isNotEmpty()) count++
            if (selectedLevels.isNotEmpty()) count++
            if (priceMin > 5000f || priceMax < 32990f) count++
            return count
        }

    fun clear() {
        selectedRatings.clear()
        priceMin = 5000f
        priceMax = 32990f
        selectedLevels.clear()
    }
}
