package kz.aruzhan.care_steps

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kz.aruzhan.care_steps.data.remote.CourseCardResponse
import kz.aruzhan.care_steps.data.remote.CreatedCourseResponse
import kz.aruzhan.care_steps.data.remote.SpecialistCardResponse
import kz.aruzhan.care_steps.data.remote.SpecialistDetailResponse
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/**
 * Парсит детальную карточку специалиста из raw JSON-строки.
 * Fallback на случай, если полновесный DTO-запрос упал (Gson type-mismatch
 * или обрыв соединения). Использует Gson на `JsonObject`, что устойчивее
 * к неожиданным типам в `specializations`/`methods`/`years_experience`.
 */
/** Парсит массив карточек курсов из raw JSON (например ответ `.../specialists/cards/{id}/courses/`). */
fun parseCourseCardListFromRawJson(raw: String): List<CourseCardResponse> {
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val type = object : TypeToken<List<CourseCardResponse>>() {}.type
        Gson().fromJson<List<CourseCardResponse>>(raw, type)
    }.getOrNull().orEmpty()
}

fun parseSpecialistDetailFromRawJson(raw: String): SpecialistDetailResponse? {
    if (raw.isBlank()) return null
    return runCatching {
        val root = JsonParser.parseString(raw)
        val obj = when {
            root.isJsonObject -> root.asJsonObject
            root.isJsonArray && root.asJsonArray.size() > 0 && root.asJsonArray[0].isJsonObject ->
                root.asJsonArray[0].asJsonObject
            else -> return@runCatching null
        }
        Gson().fromJson(obj, SpecialistDetailResponse::class.java)
    }.getOrNull()
}

data class Course(
    val id: Int,
    val title: String,
    val description: String,
    val price: String,
    /** Подписи тегов курса для карточки (все выбранные на бэкенде). */
    val tagLabels: List<String> = listOf("РОДИТЕЛЯМ"),
    val rating: String = "4.9★",
    val previewImageUrl: String? = null
)

private const val API_ORIGIN = "http://91.201.215.251:8000"

fun resolveCourseImageUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    val p = path.trim().trimEnd('/')
    if (p.isBlank()) return null
    if (p.startsWith("http://", true) || p.startsWith("https://", true)) return p
    return "$API_ORIGIN/${p.trimStart('/')}"
}

fun CourseCardResponse.previewImageUrlResolved(): String? = resolveCourseImageUrl(preview_image)

fun CourseCardResponse.formattedPriceTenge(): String {
    val v = price.replace(",", ".").toDoubleOrNull()
    return if (v != null) String.format(Locale.US, "%.2f ₸", v) else "${price.trim()} ₸"
}

fun CourseCardResponse.priceAsDouble(): Double =
    price.replace(",", ".").toDoubleOrNull() ?: 0.0

fun CourseCardResponse.ratingShortText(): String =
    String.format(Locale.US, "★%.1f", average_rating ?: 0.0)

/** До [max] подписей тегов для UI; если тегов нет — одна заглушка «РОДИТЕЛЯМ». */
fun CourseCardResponse.displayTagLabels(max: Int = 3): List<String> {
    val slice = (tags ?: emptyList()).take(max)
    return if (slice.isNotEmpty()) slice.map { tagToAudienceLabel(it) } else listOf("РОДИТЕЛЯМ")
}

fun SpecialistCardResponse.avatarUrlResolved(): String? = resolveCourseImageUrl(avatar)

fun SpecialistCardResponse.specialistPriceFromDisplay(): String {
    val cleaned = (price_from ?: "").trim().trimEnd('.')
    val v = cleaned.replace(",", ".").toDoubleOrNull()
    val c = (currency ?: "").trim().ifBlank { "₸" }
    return when {
        v != null && (c == "₸" || c.equals("KZT", true)) ->
            String.format(Locale.US, "от %.0f₸", v)
        v != null -> String.format(Locale.US, "от %.0f %s", v, c)
        cleaned.isEmpty() && (currency.isNullOrBlank()) -> "—"
        else -> "от $cleaned $c".trim()
    }
}

fun SpecialistCardResponse.ratingWithReviews(): String {
    val r = average_rating ?: 0.0
    val n = reviews_count ?: 0
    return String.format(Locale.US, "★%.1f (%d)", r, n)
}

fun SpecialistCardResponse.experienceBadgeText(): String {
    val t = (years_experience ?: "").trim()
    if (t.isEmpty()) return "—"
    if (t.contains("лет", true) || t.contains("год", true)) return t
    return "$t лет опыта"
}

// --- Адаптеры для детального ответа специалиста ---

fun SpecialistDetailResponse.avatarUrlResolved(): String? = resolveCourseImageUrl(avatar)

fun SpecialistDetailResponse.priceFromDisplay(): String {
    val cleaned = (price_from ?: "").trim().trimEnd('.')
    val v = cleaned.replace(",", ".").toDoubleOrNull()
    val c = (currency ?: "").trim().ifBlank { "₸" }
    return when {
        v != null && (c == "₸" || c.equals("KZT", true)) ->
            String.format(Locale.US, "от %.0f ₸", v)
        v != null -> String.format(Locale.US, "от %.0f %s", v, c)
        cleaned.isEmpty() && (currency.isNullOrBlank()) -> "—"
        else -> "от $cleaned $c".trim()
    }
}

fun SpecialistDetailResponse.ratingText(): String {
    val r = average_rating ?: 0.0
    return String.format(Locale.US, "★%.1f", r)
}

/** Одна строка из элемента массива: примитив или объект `{ "value", "label" }`. */
private fun jsonArrayItemToString(item: JsonElement): String? {
    return when {
        item.isJsonNull -> null
        item.isJsonPrimitive -> item.asString.trim().takeIf { it.isNotEmpty() }
        item.isJsonObject -> {
            val o = item.asJsonObject
            val label = o.get("label")?.takeIf { !it.isJsonNull && it.isJsonPrimitive }
                ?.asString?.trim()?.takeIf { it.isNotEmpty() }
            if (label != null) return label
            val value = o.get("value")?.takeIf { !it.isJsonNull && it.isJsonPrimitive }
                ?.asString?.trim()?.takeIf { it.isNotEmpty() }
            value
        }
        else -> null
    }
}

/** Список строк из поля: строка, массив строк или массив `{ value, label }`. */
internal fun JsonElement?.asLooseStringList(): List<String> {
    val el = this ?: return emptyList()
    if (el.isJsonNull) return emptyList()
    return when {
        el.isJsonArray -> el.asJsonArray.mapNotNull { jsonArrayItemToString(it) }
        el.isJsonPrimitive -> {
            val p = el.asJsonPrimitive
            when {
                p.isString -> p.asString
                    .split(",", ";", "•", "|")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                p.isNumber -> listOf(p.asString)
                else -> emptyList()
            }
        }
        el.isJsonObject -> jsonArrayItemToString(el)?.let { listOf(it) } ?: emptyList()
        else -> emptyList()
    }
}

/** Код формата (`online` / `offline`) из строки или `{ value, label }`. */
internal fun JsonElement?.workFormatCode(): String? {
    val el = this ?: return null
    if (el.isJsonNull) return null
    if (el.isJsonPrimitive) {
        val p = el.asJsonPrimitive
        if (p.isString) return p.asString.trim()
        if (p.isNumber) return p.asString.trim()
    }
    if (el.isJsonObject) {
        val v = el.asJsonObject.get("value") ?: return null
        if (v.isJsonPrimitive) {
            val p = v.asJsonPrimitive
            if (p.isString) return p.asString.trim()
            if (p.isNumber) return p.asString.trim()
        }
    }
    return null
}

/** Подпись формата с бэка (если есть), иначе null. */
internal fun JsonElement?.workFormatBackendLabel(): String? {
    val el = this ?: return null
    if (!el.isJsonObject) return null
    val l = el.asJsonObject.get("label") ?: return null
    if (l.isJsonPrimitive && l.asJsonPrimitive.isString) {
        return l.asString.trim().takeIf { it.isNotEmpty() }
    }
    return null
}

/** Целое значение стажа: бэк может отдать числом, строкой-числом или строкой с «лет». */
internal fun JsonElement?.asIntYears(): Int? {
    val el = this ?: return null
    if (el.isJsonNull) return null
    if (!el.isJsonPrimitive) return null
    val p = el.asJsonPrimitive
    if (p.isNumber) return p.asInt
    if (p.isString) return Regex("\\d+").find(p.asString)?.value?.toIntOrNull()
    return null
}

/**
 * Ключ специализации -> UI-название (русское).
 * Синхронизирован с `/api/auth/specialist/description/choices/`.
 */
private fun specializationLabelRu(code: String): String = when (code.trim().lowercase(Locale.US)) {
    "aba" -> "ABA-терапия"
    "speech_therapist" -> "Логопед"
    "neuropsychologist" -> "Нейропсихолог"
    "occupational_therapy" -> "Эрготерапия"
    "art_therapy" -> "Арт-терапия"
    "sensory_therapy" -> "Сенсорная терапия"
    "special_educator", "special_education" -> "Спецпедагог"
    "other" -> "Другое"
    else -> code.replace('_', ' ').trim().ifBlank { code }
}

private fun methodLabelRu(code: String): String = when (code.trim().lowercase(Locale.US)) {
    "aba" -> "ABA"
    "dir_floortime" -> "DIR/Floortime"
    "teacch" -> "TEACCH"
    "pecs" -> "PECS"
    "makaton" -> "Макатон"
    "denver" -> "Denver"
    "logopedic" -> "Логопедический"
    "sensory_integration" -> "Сенсорная интеграция"
    else -> code.replace('_', ' ').trim().ifBlank { code }
}

private fun developmentTypeLabelRu(code: String): String = when (code.trim().lowercase(Locale.US)) {
    "autism" -> "Аутизм"
    "adhd" -> "СДВГ"
    "speech_delay" -> "Задержка речи"
    "dyslexia" -> "Дислексия"
    "sensory_processing" -> "Сенсорная обработка"
    "motor_skills" -> "Моторика"
    "social_skills" -> "Социальные навыки"
    "learning_disabilities" -> "Трудности обучения"
    else -> code.replace('_', ' ').trim().ifBlank { code }
}

private fun languageLabelRu(code: String): String = when (code.trim().lowercase(Locale.US)) {
    "ru" -> "Русский"
    "kz", "kk" -> "Казахский"
    "en" -> "Английский"
    else -> code.replace('_', ' ').trim().ifBlank { code }
}

fun SpecialistDetailResponse.specializationLabels(): List<String> =
    specializations.asLooseStringList().map { specializationLabelRu(it) }.distinct()

fun SpecialistDetailResponse.methodLabels(): List<String> =
    methods.asLooseStringList().map { methodLabelRu(it) }.distinct()

fun SpecialistDetailResponse.developmentTypeLabels(): List<String> =
    development_types.asLooseStringList().map { developmentTypeLabelRu(it) }.distinct()

fun SpecialistDetailResponse.languageLabels(): List<String> =
    languages.asLooseStringList().map { languageLabelRu(it) }.distinct()

/** Подзаголовок карточки: «Логопед • 7 лет опыта». */
fun SpecialistDetailResponse.subtitle(): String {
    val firstSpec = specializationLabels().firstOrNull().orEmpty()
    val years = years_experience.asIntYears()
    val yearsText = years?.let {
        val w = when {
            it % 10 == 1 && it % 100 != 11 -> "год"
            it % 10 in 2..4 && it % 100 !in 12..14 -> "года"
            else -> "лет"
        }
        "$it $w опыта"
    }.orEmpty()
    return listOf(firstSpec, yearsText).filter { it.isNotBlank() }.joinToString(" • ")
}

/**
 * Формат занятий для секции «Формат занятий»: берём `work_format` и приклеиваем
 * типы консультаций, включённые у специалиста.
 */
fun SpecialistDetailResponse.workFormatLabels(): List<String> {
    val list = mutableListOf<String>()
    val code = work_format.workFormatCode()?.lowercase(Locale.US)
    when (code) {
        "online" -> list += "онлайн"
        "offline" -> list += "офлайн"
        "both", "online_offline", "hybrid" -> {
            list += "онлайн"
            list += "офлайн"
        }
        else -> {
            val backendLabel = work_format.workFormatBackendLabel()
            if (!backendLabel.isNullOrBlank()) list += backendLabel
        }
    }
    if (provide_individual_consultations == true) list += "индивидуальные"
    if (work_with_child_through_parent == true) list += "через родителя"
    if (provide_recommendations_and_plans == true) list += "рекомендации и планы"
    if (track_progress_and_analytics == true) list += "отслеживание прогресса"
    return list.distinct()
}

private fun categoryBadgeRu(code: String): String = when (code.lowercase(Locale.US)) {
    "autism" -> "АУТИЗМ"
    "speech_therapy" -> "ЛОГОПЕДИЯ"
    "adhd" -> "СДВГ"
    "sensory_processing" -> "СЕНСОРИКА"
    "social_development" -> "СОЦИАЛИЗАЦИЯ"
    "physical_therapy" -> "ФИЗИОТЕРАПИЯ"
    "behavioral_support" -> "ПОВЕДЕНИЕ"
    "learning_disabilities" -> "ОБУЧЕНИЕ"
    else -> code.uppercase(Locale.US)
}

private fun tagToAudienceLabel(tag: String): String = when (tag.lowercase(Locale.US)) {
    "to_parents" -> "РОДИТЕЛЯМ"
    "for_children" -> "ДЕТЯМ"
    "self_regulation" -> "САМОРЕГУЛЯЦИЯ"
    "logical_thinking" -> "ЛОГИКА"
    "learning_through_play" -> "ИГРА"
    "easy_start" -> "ЛЁГКИЙ СТАРТ"
    "speech_therapy_work" -> "ЛОГОПЕДИЯ"
    "social_skills_start" -> "СОЦ. НАВЫКИ"
    "with_parent_participation" -> "С РОДИТЕЛЕМ"
    "speech_understanding" -> "РЕЧЬ"
    "gradual_development" -> "РАЗВИТИЕ"
    "structured_classes" -> "ЗАНЯТИЯ"
    "memory" -> "ПАМЯТЬ"
    "intensive_course" -> "ИНТЕНСИВ"
    else -> categoryBadgeRu(tag)
}

fun CreatedCourseResponse.toUiCourse(): Course {
    val labels = if (tags.isNotEmpty()) {
        tags.map { tagToAudienceLabel(it) }
    } else {
        listOf(categoryBadgeRu(category))
    }
    val priceFmt = price.replace(",", ".").toDoubleOrNull()?.let { v ->
        String.format(Locale.US, "%.2f ₸", v)
    } ?: "${price.trim()} ₸"
    return Course(
        id = id,
        title = title,
        description = description,
        price = priceFmt,
        tagLabels = labels,
        rating = "—",
        previewImageUrl = resolveCourseImageUrl(preview_image)
    )
}

object CourseRepository {

    private val _courses = mutableStateListOf(
        Course(
            id = 1,
            title = "Основы Аутизма",
            description = "A comprehensive guide for parents to understand ASD, recognize early signs...",
            price = "12.00 ₸",
            tagLabels = listOf("РОДИТЕЛЯМ")
        ),
        Course(
            id = 2,
            title = "Основы Аутизма",
            description = "A comprehensive guide for parents to understand ASD, recognize early signs...",
            price = "12.00 ₸",
            tagLabels = listOf("РОДИТЕЛЯМ")
        )
    )

    val courses: SnapshotStateList<Course>
        get() = _courses

    fun addCourse(
        title: String,
        description: String,
        price: String,
        tagLabels: List<String> = listOf("РОДИТЕЛЯМ")
    ): Course {
        val nextId = (_courses.maxOfOrNull { it.id } ?: 0) + 1
        val course = Course(
            id = nextId,
            title = title,
            description = description,
            price = price,
            tagLabels = tagLabels
        )
        _courses.add(0, course)
        return course
    }
}

data class CartItem(
    val id: Int,
    val title: String,
    val author: String,
    val price: Double
)

data class FavoriteItem(
    val id: Int,
    val title: String,
    val author: String,
    val price: Double,
    val previewImageUrl: String? = null
)

object CartRepository {
    private val _items = mutableStateListOf<CartItem>()
    private const val PREFS = "cart_storage"
    private const val KEY_ITEMS = "items_json"
    private var initialized = false
    private var appContext: Context? = null

    val items: SnapshotStateList<CartItem>
        get() = _items

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        appContext = context.applicationContext
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ITEMS, null) ?: return
        runCatching {
            val arr = JSONArray(raw)
            repeat(arr.length()) { index ->
                val obj = arr.optJSONObject(index) ?: return@repeat
                val item = CartItem(
                    id = obj.optInt("id"),
                    title = obj.optString("title"),
                    author = obj.optString("author"),
                    price = obj.optDouble("price")
                )
                if (_items.none { it.id == item.id }) _items.add(item)
            }
        }
    }

    fun add(item: CartItem) {
        if (_items.none { it.id == item.id }) {
            _items.add(item)
            persist()
        }
    }

    fun remove(itemId: Int) {
        _items.removeAll { it.id == itemId }
        persist()
    }

    fun contains(itemId: Int): Boolean = _items.any { it.id == itemId }

    fun total(): Double = _items.sumOf { it.price }

    private fun persist() {
        val appCtx = appContext ?: return
        val prefs = appCtx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray()
        _items.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            obj.put("author", item.author)
            obj.put("price", item.price)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_ITEMS, arr.toString()).apply()
    }
}

/**
 * Локальный журнал успешных оплат родителя (после POST purchase).
 * Дашборд специалиста считает покупки и выручку только по своим course_id.
 */
object PurchaseAnalyticsRepository {
    private const val PREFS = "purchase_analytics"
    private const val KEY_EVENTS = "events_json"
    private var appContext: Context? = null
    private val lock = Any()

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun recordPurchase(courseId: Int, amountKzt: Double) {
        val ctx = appContext ?: return
        synchronized(lock) {
            val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_EVENTS, null) ?: "[]"
            val arr = runCatching { JSONArray(raw) }.getOrElse { JSONArray() }
            val o = JSONObject()
            o.put("course_id", courseId)
            o.put("amount", amountKzt)
            arr.put(o)
            prefs.edit().putString(KEY_EVENTS, arr.toString()).apply()
        }
    }

    fun statsForCourses(courseIds: Set<Int>): Pair<Int, Double> {
        val ctx = appContext ?: return 0 to 0.0
        synchronized(lock) {
            val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_EVENTS, null) ?: "[]"
            val arr = runCatching { JSONArray(raw) }.getOrElse { JSONArray() }
            var count = 0
            var sum = 0.0
            repeat(arr.length()) { i ->
                val o = arr.optJSONObject(i) ?: return@repeat
                val cid = o.optInt("course_id")
                if (cid in courseIds) {
                    count++
                    sum += o.optDouble("amount")
                }
            }
            return count to sum
        }
    }
}

object FavoritesRepository {
    private val _items = mutableStateListOf<FavoriteItem>()
    private const val PREFS = "favorites_storage"
    private const val KEY_ITEMS = "favorites_json"
    private var initialized = false
    private var appContext: Context? = null

    val items: SnapshotStateList<FavoriteItem>
        get() = _items

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        appContext = context.applicationContext
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ITEMS, null) ?: return
        runCatching {
            val arr = JSONArray(raw)
            repeat(arr.length()) { index ->
                val obj = arr.optJSONObject(index) ?: return@repeat
                val item = FavoriteItem(
                    id = obj.optInt("id"),
                    title = obj.optString("title"),
                    author = obj.optString("author"),
                    price = obj.optDouble("price"),
                    previewImageUrl = obj.optString("previewImageUrl").ifBlank { null }
                )
                if (_items.none { it.id == item.id }) _items.add(item)
            }
        }
    }

    fun add(item: FavoriteItem) {
        if (_items.none { it.id == item.id }) {
            _items.add(item)
            persist()
        }
    }

    fun remove(itemId: Int) {
        _items.removeAll { it.id == itemId }
        persist()
    }

    fun toggle(item: FavoriteItem) {
        if (contains(item.id)) remove(item.id) else add(item)
    }

    fun contains(itemId: Int): Boolean = _items.any { it.id == itemId }

    private fun persist() {
        val appCtx = appContext ?: return
        val prefs = appCtx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray()
        _items.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            obj.put("author", item.author)
            obj.put("price", item.price)
            obj.put("previewImageUrl", item.previewImageUrl ?: "")
            arr.put(obj)
        }
        prefs.edit().putString(KEY_ITEMS, arr.toString()).apply()
    }
}

