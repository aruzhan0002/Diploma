package com.example.diploma.data.remote

import com.example.diploma.SearchableCourse
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * Карточка курса (публичный список и курсы специалиста).
 * Бэк может отдавать `average_rating: null`, `purchased` как boolean.
 */
data class CourseCardResponse(
    val id: Int,
    val title: String,
    val level: String,
    val specialist_name: String,
    val price: String,
    val average_rating: Double? = null,
    val purchased: Boolean? = null,
    val preview_image: String?,
    /** Теги курса с бэкенда (как в карточке специалиста), до 3 показываем в списке родителя. */
    @SerializedName("tags") val tags: List<String>? = null
) {
    fun toSearchableCourse(): SearchableCourse {
        val levelRu = when (level.lowercase()) {
            "beginner" -> "Начальный"
            "intermediate" -> "Базовый"
            "advanced" -> "Продвинутый"
            else -> "Начальный"
        }
        val priceDouble = price.replace(",", ".").toDoubleOrNull() ?: 0.0
        return SearchableCourse(
            id = id,
            title = title,
            author = specialist_name,
            price = priceDouble,
            rating = average_rating ?: 0.0,
            audienceLabel = "РОДИТЕЛЯМ",
            level = levelRu
        )
    }
}

data class CourseDescriptionResponse(
    val id: Int,
    val description: String? = null,
    @SerializedName("learning_outcomes") val learningOutcomesRaw: JsonElement? = null,
    @SerializedName("tags") val tagsRaw: JsonElement? = null
)

data class CourseContentResponse(
    val id: Int,
    @SerializedName("modules_count") val modulesCount: String? = null,
    val duration: String? = null,
    val modules: List<CourseContentModuleResponse> = emptyList()
)

data class CourseContentModuleResponse(
    val id: Int,
    val title: String? = null,
    @SerializedName("material_type") val materialType: String? = null,
    @SerializedName("material_type_label") val materialTypeLabel: String? = null,
    val file: String? = null,
    val url: String? = null,
    @SerializedName("material_file") val materialFile: String? = null
)

data class CourseSpecialistResponse(
    @SerializedName("full_name") val fullName: String? = null,
    val avatar: String? = null,
    /** Backend may return a string or a JSON array of codes (e.g. ["speech_therapist"]). */
    val specializations: JsonElement? = null,
    /** Backend may return a string or a number. */
    @SerializedName("years_experience") val yearsExperience: JsonElement? = null,
    @SerializedName("approach_description") val approachDescription: String? = null
)

private fun jsonElementToTrimmedString(el: JsonElement?): String? {
    if (el == null || el.isJsonNull) return null
    if (el.isJsonPrimitive) {
        val p = el.asJsonPrimitive
        return when {
            p.isString -> p.asString.trim().takeIf { it.isNotEmpty() }
            p.isNumber -> p.asString.trim().takeIf { it.isNotEmpty() }
            else -> null
        }
    }
    if (el.isJsonArray) {
        val first = el.asJsonArray.firstOrNull() ?: return null
        if (first.isJsonPrimitive) return first.asString.trim().takeIf { it.isNotEmpty() }
    }
    return null
}

private fun specializationCodeToRu(code: String): String {
    val c = code.trim().lowercase().trim('"', '[', ']')
    return when (c) {
        "aba" -> "ABA-терапия"
        "speech_therapist" -> "Логопед"
        "neuropsychologist" -> "Нейропсихолог"
        "occupational_therapy" -> "Эрготерапия"
        "art_therapy" -> "Арт-терапия"
        "sensory_therapy" -> "Сенсорная терапия"
        "special_education" -> "Спецпедагог"
        else -> code.replace('_', ' ').trim().ifBlank { code }
    }
}

/** Текст для блока «о специалисте», если нет approach_description. */
fun CourseSpecialistResponse.specializationsForDescription(): String {
    val el = specializations ?: return ""
    val codes = when {
        el.isJsonArray ->
            el.asJsonArray.mapNotNull { item ->
                if (item.isJsonPrimitive) item.asString.trim().takeIf { it.isNotEmpty() } else null
            }
        el.isJsonPrimitive -> {
            val p = el.asJsonPrimitive
            when {
                p.isString -> listOfNotNull(p.asString.trim().takeIf { it.isNotEmpty() })
                else -> emptyList()
            }
        }
        else -> emptyList()
    }
    return codes.map { specializationCodeToRu(it) }.distinct().joinToString(", ")
}

/** Подпись под именем: специализация и стаж из ответа GET specialist. */
fun CourseSpecialistResponse.specialistRoleSubtitle(): String {
    val specRaw = jsonElementToTrimmedString(specializations).orEmpty()
    val specLabel = if (specRaw.isNotBlank()) specializationCodeToRu(specRaw) else ""
    val yearsEl = yearsExperience
    val yearsText = when {
        yearsEl == null || yearsEl.isJsonNull -> ""
        yearsEl.isJsonPrimitive && yearsEl.asJsonPrimitive.isNumber -> {
            val n = yearsEl.asInt
            val w = when {
                n % 10 == 1 && n % 100 != 11 -> "год"
                n % 10 in 2..4 && n % 100 !in 12..14 -> "года"
                else -> "лет"
            }
            "$n $w опыта"
        }
        yearsEl.isJsonPrimitive && yearsEl.asJsonPrimitive.isString -> {
            val s = yearsEl.asString.trim()
            val n = s.toIntOrNull()
            if (n != null) {
                val w = when {
                    n % 10 == 1 && n % 100 != 11 -> "год"
                    n % 10 in 2..4 && n % 100 !in 12..14 -> "года"
                    else -> "лет"
                }
                "$n $w опыта"
            } else {
                s
            }
        }
        else -> ""
    }
    return when {
        specLabel.isNotBlank() && yearsText.isNotBlank() -> "$specLabel • $yearsText"
        specLabel.isNotBlank() -> specLabel
        yearsText.isNotBlank() -> yearsText
        else -> ""
    }
}

fun CourseDescriptionResponse.learningOutcomesList(): List<String> =
    learningOutcomesRaw.toStringList()

fun CourseDescriptionResponse.tagLabels(): List<String> =
    tagsRaw.toStringList()

private fun JsonElement?.toStringList(): List<String> {
    val element = this ?: return emptyList()
    return when {
        element.isJsonArray -> {
            (element as JsonArray)
                .mapNotNull { item ->
                    when {
                        item.isJsonNull -> null
                        item.isJsonPrimitive -> item.asString
                        item.isJsonObject -> {
                            val obj = item.asJsonObject
                            when {
                                obj.has("label") -> obj.get("label").asString
                                obj.has("value") -> obj.get("value").asString
                                else -> null
                            }
                        }
                        else -> null
                    }
                }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }
        element.isJsonPrimitive -> listOf(element.asString.trim()).filter { it.isNotEmpty() }
        else -> emptyList()
    }
}
