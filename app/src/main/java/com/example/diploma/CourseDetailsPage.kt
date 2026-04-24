@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package kz.aruzhan.care_steps

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.drawBehind
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kz.aruzhan.care_steps.data.remote.CourseCardResponse
import kz.aruzhan.care_steps.data.remote.CourseContentResponse
import kz.aruzhan.care_steps.data.remote.CourseDescriptionResponse
import kz.aruzhan.care_steps.data.remote.CourseSpecialistResponse
import kz.aruzhan.care_steps.data.remote.learningOutcomesList
import kz.aruzhan.care_steps.data.remote.specialistRoleSubtitle
import kz.aruzhan.care_steps.data.remote.specializationsForDescription
import kz.aruzhan.care_steps.data.remote.tagLabels
import kz.aruzhan.care_steps.data.remote.ApiClient
import kz.aruzhan.care_steps.data.remote.CourseContentModuleResponse
import kz.aruzhan.care_steps.data.remote.PartialCourseUpdateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

private data class CourseFetchResult(
    val course: CourseCardResponse?,
    val description: CourseDescriptionResponse?,
    val content: CourseContentResponse?,
    val specialist: CourseSpecialistResponse?,
    val previewImagePath: String?,
    val fullCourseDescription: String?,
    val fullCourseLearningOutcomes: String?,
    val fullCourseTags: List<String>,
    val specialistListDescription: String?,
    val specialistListPreviewImagePath: String?,
    val specialistListTags: List<String>,
    val rawCourseByIdDescription: String?,
    val rawCourseByIdLearningOutcomes: String?,
    val rawCourseByIdTags: List<String>,
    val rawCourseByIdPreviewImagePath: String?,
    val moduleFileById: Map<Int, String>,
    val moduleFileByTitle: Map<String, String>,
    val commonError: String?,
    val contentError: String?
)

private data class SpecialistCourseSnapshot(
    val description: String?,
    val previewImage: String?,
    val tags: List<String>
)

private val StringSetSaver = listSaver<Set<String>, String>(
    save = { it.toList() },
    restore = { it.toSet() }
)

/**
 * Тройка (id, title, url) для одного модуля, вытащенная из сырого JSON.
 * Любое поле может быть null, если в ответе его нет.
 */
private data class RawModuleFile(
    val id: Int?,
    val title: String?,
    val url: String?
)

/**
 * Ищет в сыром JSON любые объекты, похожие на «модуль с файлом», и возвращает
 * список (id, title, url). Понимает разные имена полей, которые встречаются у нас
 * на бэкенде (`file`, `material_file`, `url`, `file_url`, `download_url`,
 * `attachment`, `content_url`) и разный формат ответа (объект с `modules`, объект
 * с `results`, просто массив).
 */
private fun extractModuleFilesFromRawJson(rawText: String): List<RawModuleFile> {
    val trimmed = rawText.trim()
    if (trimmed.isEmpty()) return emptyList()
    val root: Any = runCatching {
        when (trimmed.firstOrNull()) {
            '[' -> JSONArray(trimmed)
            '{' -> JSONObject(trimmed)
            else -> return emptyList()
        }
    }.getOrElse { return emptyList() }

    val result = mutableListOf<RawModuleFile>()
    fun urlFromObject(obj: JSONObject): String? {
        val keys = listOf(
            "file", "material_file", "file_url", "download_url", "url", "attachment", "content_url",
            "material_url", "lesson_file", "resource_url", "link", "media_file", "document"
        )
        for (k in keys) {
            if (!obj.has(k)) continue
            val v = obj.opt(k)
            when (v) {
                is String -> if (v.isNotBlank()) return v
                is JSONObject -> {
                    val inner = v.optString("url").takeIf { it.isNotBlank() }
                        ?: v.optString("href").takeIf { it.isNotBlank() }
                        ?: v.optString("file").takeIf { it.isNotBlank() }
                    if (!inner.isNullOrBlank()) return inner
                }
            }
        }
        return null
    }

    fun walk(any: Any?) {
        when (any) {
            is JSONArray -> repeat(any.length()) { walk(any.opt(it)) }
            is JSONObject -> {
                val hasModuleShape = any.has("material_type") ||
                    any.has("material_type_label") ||
                    any.has("title") ||
                    any.has("file") ||
                    any.has("material_file")
                if (hasModuleShape) {
                    val id = any.opt("id")?.toString()?.toIntOrNull()
                    val title = any.optString("title").takeIf { it.isNotBlank() }
                    val url = urlFromObject(any)
                    if (url != null || id != null || title != null) {
                        result += RawModuleFile(id = id, title = title, url = url)
                    }
                }
                any.keys().forEach { key -> walk(any.opt(key)) }
            }
        }
    }
    walk(root)
    return result
}

/** URL файла урока: поля модуля, затем fallback по id/title из сырого парсинга. */
private fun resolveLessonMaterialUrl(
    module: CourseContentModuleResponse,
    moduleFileById: Map<Int, String>,
    moduleFileByTitle: Map<String, String>
): String? {
    val fromFields = listOf(module.file, module.materialFile, module.url)
        .firstOrNull { !it.isNullOrBlank() }
        ?.let { resolveCourseImageUrl(it) }
    if (!fromFields.isNullOrBlank()) return fromFields
    val id = module.id
    if (id > 0) moduleFileById[id]?.let { return it }
    val title = module.title?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    moduleFileByTitle[title]?.let { return it }
    return moduleFileByTitle.entries.firstOrNull { (k, _) ->
        k.equals(title, ignoreCase = true)
    }?.value
}

private val CourseMaterialListSaver = listSaver<List<CourseMaterial>, Any>(
    save = { items ->
        items.flatMap { item ->
            listOf(item.id, item.type, item.title, item.fileUri, item.displayName)
        }
    },
    restore = { raw ->
        raw.chunked(5).mapNotNull { chunk ->
            val id = chunk.getOrNull(0) as? Int ?: return@mapNotNull null
            val type = chunk.getOrNull(1) as? String ?: "article"
            val title = chunk.getOrNull(2) as? String ?: ""
            val fileUri = chunk.getOrNull(3) as? String ?: ""
            val displayName = chunk.getOrNull(4) as? String ?: "Файл не выбран"
            CourseMaterial(
                id = id,
                type = type,
                title = title,
                fileUri = fileUri,
                displayName = displayName
            )
        }
    }
)

@Composable
fun CourseDetailsPage(navController: NavController, courseId: Int = 1, mode: String = "parent") {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val isSpecialistMode = mode == "specialist"
    var course by remember { mutableStateOf<CourseCardResponse?>(null) }
    var descriptionData by remember { mutableStateOf<CourseDescriptionResponse?>(null) }
    var contentData by remember { mutableStateOf<CourseContentResponse?>(null) }
    var specialistData by remember { mutableStateOf<CourseSpecialistResponse?>(null) }
    var previewImagePath by remember { mutableStateOf<String?>(null) }
    var fullCourseDescription by remember { mutableStateOf<String?>(null) }
    var fullCourseLearningOutcomes by remember { mutableStateOf<String?>(null) }
    var fullCourseTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var specialistListDescription by remember { mutableStateOf<String?>(null) }
    var specialistListPreviewImagePath by remember { mutableStateOf<String?>(null) }
    var specialistListTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var rawCourseByIdDescription by remember { mutableStateOf<String?>(null) }
    var rawCourseByIdLearningOutcomes by remember { mutableStateOf<String?>(null) }
    var rawCourseByIdTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var rawCourseByIdPreviewImagePath by remember { mutableStateOf<String?>(null) }
    var moduleFileById by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var moduleFileByTitle by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var contentError by remember { mutableStateOf<String?>(null) }
    var specialistEditMode by rememberSaveable { mutableStateOf(false) }
    var editableDescription by rememberSaveable { mutableStateOf("") }
    var editableOutcomes by rememberSaveable { mutableStateOf("") }
    var editableTagValues by rememberSaveable(stateSaver = StringSetSaver) { mutableStateOf(emptySet()) }
    var editableMaterials by rememberSaveable(stateSaver = CourseMaterialListSaver) {
        mutableStateOf(emptyList())
    }
    var refreshTick by remember { mutableStateOf(0) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(courseId, refreshTick) {
        loading = true
        loadError = null
        val fetchResult = withContext(Dispatchers.IO) {
            val cardsResult = if (isSpecialistMode) {
                null
            } else {
                runCatching { ApiClient.withNetworkRetry { ApiClient.api.getCourseCards() } }
            }
            val detailsResult = runCatching {
                ApiClient.withNetworkRetry {
                    if (isSpecialistMode) {
                        ApiClient.api.getSpecialistCourseDescription(courseId)
                    } else {
                        ApiClient.api.getCourseDescription(courseId)
                    }
                }
            }
            val contentResult = runCatching {
                ApiClient.withNetworkRetry {
                    if (isSpecialistMode) {
                        ApiClient.api.getSpecialistCourseContent(courseId)
                    } else {
                        // Parent flow: private content may include module file URLs.
                        // Fallback to public content for compatibility when private is unavailable.
                        runCatching { ApiClient.api.getSpecialistCourseContent(courseId) }
                            .getOrElse { ApiClient.api.getCourseContent(courseId) }
                    }
                }
            }
            val specialistResult = runCatching {
                ApiClient.withNetworkRetry {
                    if (isSpecialistMode) {
                        ApiClient.api.getSpecialistCourseSpecialist(courseId)
                    } else {
                        ApiClient.api.getCourseSpecialist(courseId)
                    }
                }
            }
            val previewPathResult = runCatching {
                val previews = ApiClient.withNetworkRetry { ApiClient.api.getCoursePreviews() }
                previews.firstOrNull { it.id == courseId }?.preview_image
            }.recoverCatching {
                // Fallback: если в preview-ответе id/типы нестабильные.
                val raw = ApiClient.withNetworkRetry { ApiClient.api.getCoursePreviewsRaw() }.string()
                val arr = JSONArray(raw)
                var found: String? = null
                repeat(arr.length()) { idx ->
                    val o = arr.optJSONObject(idx) ?: return@repeat
                    val idRaw = o.opt("id")?.toString()?.toIntOrNull()
                    if (idRaw == courseId) {
                        found = o.optString("preview_image").ifBlank { null }
                        return@repeat
                    }
                }
                found
            }
            // Только для специалиста: «мои курсы» и приватный GET /api/courses/{id}/.
            // У родителя эти эндпоинты дают 403/404 — не дергаем, чтобы не засорять логи и не путать отладку.
            val specialistSnapshotResult = if (isSpecialistMode) {
                runCatching {
                    val list = ApiClient.withNetworkRetry { ApiClient.api.getSpecialistCourses() }
                    val item = list.firstOrNull { it.id == courseId }
                    if (item == null) null else SpecialistCourseSnapshot(
                        description = item.description,
                        previewImage = item.preview_image,
                        tags = item.tags
                    )
                }.recoverCatching {
                    val raw = ApiClient.withNetworkRetry { ApiClient.api.getSpecialistCoursesRaw() }.string()
                    val arr = JSONArray(raw)
                    var snap: SpecialistCourseSnapshot? = null
                    repeat(arr.length()) { idx ->
                        val o = arr.optJSONObject(idx) ?: return@repeat
                        val idRaw = o.opt("id")?.toString()?.toIntOrNull() ?: return@repeat
                        if (idRaw == courseId) {
                            val tags = mutableListOf<String>()
                            val tagsAny = o.opt("tags")
                            when (tagsAny) {
                                is JSONArray -> repeat(tagsAny.length()) { i ->
                                    tagsAny.optString(i).takeIf { it.isNotBlank() }?.let { tags += it }
                                }
                                is String -> if (tagsAny.isNotBlank()) tags += tagsAny
                            }
                            snap = SpecialistCourseSnapshot(
                                description = o.optString("description").ifBlank { null },
                                previewImage = o.optString("preview_image").ifBlank { null },
                                tags = tags
                            )
                            return@repeat
                        }
                    }
                    snap
                }
            } else {
                Result.success<SpecialistCourseSnapshot?>(null)
            }
            val rawCourseByIdResult = if (isSpecialistMode) {
                runCatching {
                    val raw = ApiClient.withNetworkRetry { ApiClient.api.getCourseByIdRaw(courseId) }.string()
                    val obj = JSONObject(raw)
                    val tags = mutableListOf<String>()
                    val tagsAny = obj.opt("tags")
                    when (tagsAny) {
                        is JSONArray -> repeat(tagsAny.length()) { i ->
                            tagsAny.optString(i).takeIf { it.isNotBlank() }?.let { tags += it }
                        }
                        is String -> if (tagsAny.isNotBlank()) tags += tagsAny
                    }
                    mapOf(
                        "description" to obj.optString("description").ifBlank { null },
                        "learning_outcomes" to obj.optString("learning_outcomes").ifBlank { null },
                        "preview_image" to obj.optString("preview_image").ifBlank { null },
                        "tags" to tags
                    )
                }.getOrNull()
            } else {
                null
            }
            val fullCourseResult = runCatching {
                if (isSpecialistMode) {
                    ApiClient.withNetworkRetry { ApiClient.api.getCourseById(courseId) }
                } else {
                    null // родитель: приватный GET /api/courses/{id}/ недоступен (404)
                }
            }

            val ownCourse = fullCourseResult.getOrNull()
            val nextCourse = if (isSpecialistMode) {
                val own = ownCourse
                if (own != null) {
                    CourseCardResponse(
                        id = own.id,
                        title = own.title,
                        level = own.level,
                        specialist_name = specialistResult.getOrNull()?.fullName?.ifBlank { "Специалист" } ?: "Специалист",
                        price = own.price,
                        average_rating = own.average_rating ?: 0.0,
                        purchased = null,
                        preview_image = own.preview_image,
                        tags = own.tags
                    )
                } else {
                    null
                }
            } else {
                cardsResult?.getOrNull()?.firstOrNull { it.id == courseId }
            }
            val nextDescription = detailsResult.getOrNull()
            val rawContent = contentResult.getOrNull()
            val nextPreviewImagePath = previewPathResult.getOrNull()
            val fullModules = ownCourse?.modules.orEmpty()
            val nextFullDescription = ownCourse?.description
            val nextFullLearningOutcomes = ownCourse?.learning_outcomes
            val nextFullTags = ownCourse?.tags.orEmpty()
            val specialistSnapshot = specialistSnapshotResult.getOrNull()
            val nextSpecialistListDescription = specialistSnapshot?.description
            val nextSpecialistListPreviewImagePath = specialistSnapshot?.previewImage
            val nextSpecialistListTags = specialistSnapshot?.tags.orEmpty()
            val nextRawByIdDescription = rawCourseByIdResult?.get("description") as? String
            val nextRawByIdLearningOutcomes = rawCourseByIdResult?.get("learning_outcomes") as? String
            @Suppress("UNCHECKED_CAST")
            val nextRawByIdTags = (rawCourseByIdResult?.get("tags") as? List<String>).orEmpty()
            val nextRawByIdPreviewImage = rawCourseByIdResult?.get("preview_image") as? String
            val fallbackContent = if (
                isSpecialistMode &&
                (rawContent == null || rawContent.modules.isEmpty()) &&
                fullModules.isNotEmpty()
            ) {
                CourseContentResponse(
                    id = courseId,
                    modulesCount = fullModules.size.toString(),
                    duration = rawContent?.duration,
                    modules = fullModules.map { m ->
                        CourseContentModuleResponse(
                            id = m.id,
                            title = m.title,
                            materialType = m.material_type,
                            materialTypeLabel = when (m.material_type.lowercase()) {
                                "article" -> "Статья"
                                "pdf" -> "PDF"
                                "video" -> "Видео"
                                else -> m.material_type
                            },
                            file = m.file
                        )
                    }
                )
            } else {
                rawContent
            }
            val nextContent = fallbackContent
            val nextSpecialist = specialistResult.getOrNull()

            // Собираем file URLs из всех возможных источников.
            // Это важно для родителя, у которого приватные эндпоинты могут быть закрыты,
            // а публичные — возвращать модуль без `file`. Парсим сырой JSON и пробуем
            // разные имена полей, чтобы ни один доступный URL не потерялся.
            val rawContentResults = buildList<okhttp3.ResponseBody?> {
                add(runCatching {
                    ApiClient.withNetworkRetry { ApiClient.api.getSpecialistCourseContentRaw(courseId) }
                }.getOrNull())
                add(runCatching {
                    ApiClient.withNetworkRetry { ApiClient.api.getCourseContentRaw(courseId) }
                }.getOrNull())
                // GET /api/courses/{id}/ только у специалиста; у родителя — 404 «No Course matches…».
                if (isSpecialistMode) {
                    add(runCatching {
                        ApiClient.withNetworkRetry { ApiClient.api.getCourseByIdRaw(courseId) }
                    }.getOrNull())
                }
            }
            val rawFilesById = mutableMapOf<Int, String>()
            val rawFilesByTitle = mutableMapOf<String, String>()
            rawContentResults.forEach { body ->
                if (body == null) return@forEach
                val text = runCatching { body.string() }.getOrNull() ?: return@forEach
                extractModuleFilesFromRawJson(text).forEach { (id, title, url) ->
                    val resolved = resolveCourseImageUrl(url) ?: return@forEach
                    if (id != null && id > 0) rawFilesById.putIfAbsent(id, resolved)
                    if (!title.isNullOrBlank()) rawFilesByTitle.putIfAbsent(title.trim(), resolved)
                }
            }

            val nextModuleFileById = buildMap<Int, String> {
                fullCourseResult.getOrNull()?.modules.orEmpty().forEach { m ->
                    val fileUrl = resolveCourseImageUrl(m.file) ?: return@forEach
                    if (m.id > 0) put(m.id, fileUrl)
                }
                rawFilesById.forEach { (id, url) -> putIfAbsent(id, url) }
            }
            val nextModuleFileByTitle = buildMap<String, String> {
                fullCourseResult.getOrNull()?.modules.orEmpty().forEach { m ->
                    val key = m.title.trim()
                    val fileUrl = resolveCourseImageUrl(m.file) ?: return@forEach
                    if (key.isNotBlank()) put(key, fileUrl)
                }
                rawFilesByTitle.forEach { (title, url) -> putIfAbsent(title, url) }
            }
            val allFailed = if (isSpecialistMode) {
                detailsResult.isFailure &&
                    contentResult.isFailure &&
                    specialistResult.isFailure &&
                    fullCourseResult.isFailure
            } else {
                (cardsResult?.isFailure == true) &&
                    detailsResult.isFailure &&
                    contentResult.isFailure &&
                    specialistResult.isFailure
            }
            val errorText = if (allFailed) {
                detailsResult.exceptionOrNull()?.message
                    ?: contentResult.exceptionOrNull()?.message
                    ?: specialistResult.exceptionOrNull()?.message
                    ?: fullCourseResult.exceptionOrNull()?.message
                    ?: cardsResult?.exceptionOrNull()?.message
                    ?: "Не удалось загрузить данные курса"
            } else null
            val contentErrorText = if (contentResult.isFailure && nextContent?.modules.orEmpty().isEmpty()) {
                contentResult.exceptionOrNull()?.message ?: "Не удалось получить содержание курса"
            } else {
                null
            }

            CourseFetchResult(
                course = nextCourse,
                description = nextDescription,
                content = nextContent,
                specialist = nextSpecialist,
                previewImagePath = nextPreviewImagePath,
                fullCourseDescription = nextFullDescription,
                fullCourseLearningOutcomes = nextFullLearningOutcomes,
                fullCourseTags = nextFullTags,
                specialistListDescription = nextSpecialistListDescription,
                specialistListPreviewImagePath = nextSpecialistListPreviewImagePath,
                specialistListTags = nextSpecialistListTags,
                rawCourseByIdDescription = nextRawByIdDescription,
                rawCourseByIdLearningOutcomes = nextRawByIdLearningOutcomes,
                rawCourseByIdTags = nextRawByIdTags,
                rawCourseByIdPreviewImagePath = nextRawByIdPreviewImage,
                moduleFileById = nextModuleFileById,
                moduleFileByTitle = nextModuleFileByTitle,
                commonError = errorText,
                contentError = contentErrorText
            )
        }
        course = fetchResult.course
        descriptionData = fetchResult.description
        contentData = fetchResult.content
        specialistData = fetchResult.specialist
        previewImagePath = fetchResult.previewImagePath
        fullCourseDescription = fetchResult.fullCourseDescription
        fullCourseLearningOutcomes = fetchResult.fullCourseLearningOutcomes
        fullCourseTags = fetchResult.fullCourseTags
        specialistListDescription = fetchResult.specialistListDescription
        specialistListPreviewImagePath = fetchResult.specialistListPreviewImagePath
        specialistListTags = fetchResult.specialistListTags
        rawCourseByIdDescription = fetchResult.rawCourseByIdDescription
        rawCourseByIdLearningOutcomes = fetchResult.rawCourseByIdLearningOutcomes
        rawCourseByIdTags = fetchResult.rawCourseByIdTags
        rawCourseByIdPreviewImagePath = fetchResult.rawCourseByIdPreviewImagePath
        moduleFileById = fetchResult.moduleFileById
        moduleFileByTitle = fetchResult.moduleFileByTitle
        loadError = fetchResult.commonError
        contentError = fetchResult.contentError
        loading = false
    }

    CartRepository.items
    val isInCart = CartRepository.contains(courseId)
    val imageUrl = course?.previewImageUrlResolved()
        ?: resolveCourseImageUrl(previewImagePath)
        ?: resolveCourseImageUrl(specialistListPreviewImagePath)
        ?: resolveCourseImageUrl(rawCourseByIdPreviewImagePath)
    val defaultDescription = descriptionData?.description
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: fullCourseDescription?.trim()?.takeIf { it.isNotEmpty() }
        ?: specialistListDescription?.trim()?.takeIf { it.isNotEmpty() }
        ?: rawCourseByIdDescription?.trim()?.takeIf { it.isNotEmpty() }
        ?: course?.let { "Курс: ${it.title}. Специалист: ${it.specialist_name}. Рейтинг: ${it.average_rating}." }
        ?: "Описание курса пока недоступно."
    val defaultOutcomes = descriptionData?.learningOutcomesList()
        ?.takeIf { it.isNotEmpty() }
        ?: parseLearningOutcomesText(fullCourseLearningOutcomes)
            .takeIf { it.isNotEmpty() }
        ?: parseLearningOutcomesText(rawCourseByIdLearningOutcomes)
    val defaultTags = descriptionData?.tagLabels()
        ?.takeIf { it.isNotEmpty() }
        ?: fullCourseTags
            .takeIf { it.isNotEmpty() }
        ?: specialistListTags
            .takeIf { it.isNotEmpty() }
        ?: rawCourseByIdTags

    LaunchedEffect(courseId, defaultDescription, defaultOutcomes, defaultTags, isSpecialistMode, specialistEditMode) {
        if (!isSpecialistMode || specialistEditMode) return@LaunchedEffect
        editableDescription = defaultDescription
        editableOutcomes = defaultOutcomes.joinToString("\n")
        editableTagValues = defaultTags
            .map { normalizeCourseTagValue(it) }
            .filter { it.isNotBlank() }
            .toSet()
    }
    LaunchedEffect(courseId, contentData, moduleFileById, moduleFileByTitle, isSpecialistMode, specialistEditMode) {
        if (!isSpecialistMode || specialistEditMode) return@LaunchedEffect
        editableMaterials = contentModulesToEditableMaterials(
            modules = contentData?.modules.orEmpty(),
            fallbackFileById = moduleFileById,
            fallbackFileByTitle = moduleFileByTitle
        )
    }

    val showSpecialistActionButton = isSpecialistMode && selectedTab != 2
    Scaffold(
        bottomBar = {
            if (isSpecialistMode && showSpecialistActionButton) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF006FFD))
                        .clickable(enabled = !saving) {
                            if (!specialistEditMode) {
                                if (selectedTab == 1 && editableMaterials.isEmpty()) {
                                    editableMaterials = contentModulesToEditableMaterials(
                                        modules = contentData?.modules.orEmpty(),
                                        fallbackFileById = moduleFileById,
                                        fallbackFileByTitle = moduleFileByTitle
                                    )
                                }
                                specialistEditMode = true
                            } else {
                                scope.launch {
                                    saving = true
                                    val base = withContext(Dispatchers.IO) {
                                        runCatching { ApiClient.withNetworkRetry { ApiClient.api.getCourseById(courseId) } }
                                    }.getOrNull()
                                    if (base == null) {
                                        saving = false
                                        Toast.makeText(ctx, "Не удалось получить курс для сохранения", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }
                                    val selectedTags = editableTagValues.toList().ifEmpty {
                                        base.tags
                                    }
                                    if (selectedTab == 0) {
                                        val patchReq = PartialCourseUpdateRequest(
                                            description = editableDescription.trim(),
                                            learning_outcomes = editableOutcomes.trim(),
                                            tags = selectedTags
                                        )
                                        val patchResult = withContext(Dispatchers.IO) {
                                            runCatching {
                                                ApiClient.withNetworkRetry { ApiClient.api.patchCourse(courseId, patchReq) }
                                            }
                                        }
                                        saving = false
                                        patchResult.onSuccess {
                                            specialistEditMode = false
                                            refreshTick += 1
                                            Toast.makeText(ctx, "Изменения успешно сохранены", Toast.LENGTH_SHORT).show()
                                        }.onFailure { e ->
                                            Toast.makeText(
                                                ctx,
                                                e.message ?: "Не удалось сохранить изменения описания",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        return@launch
                                    }
                                    val originalModuleIds = base.modules
                                        ?.map { it.id }
                                        ?.toSet()
                                        .orEmpty()
                                    val keptIds = editableMaterials
                                        .filter { it.id > 0 }
                                        .map { it.id }
                                        .toSet()
                                    val deletedIds = originalModuleIds - keptIds
                                    val newMaterials = editableMaterials
                                        .filter { it.id < 0 && it.fileUri.startsWith("content://") }

                                    val hasNewWithoutFile = editableMaterials
                                        .any { it.id < 0 && !it.fileUri.startsWith("content://") }
                                    if (hasNewWithoutFile) {
                                        saving = false
                                        Toast.makeText(ctx, "Для каждого нового материала выберите файл", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }

                                    val errors = mutableListOf<String>()

                                    for (moduleId in deletedIds) {
                                        val delResult = withContext(Dispatchers.IO) {
                                            runCatching {
                                                ApiClient.withNetworkRetry {
                                                    ApiClient.api.deleteModule(courseId, moduleId)
                                                }
                                            }
                                        }
                                        delResult.onFailure { e ->
                                            errors += "Удаление модуля $moduleId: ${e.message}"
                                        }
                                    }

                                    for ((idx, mat) in newMaterials.withIndex()) {
                                        val addResult = withContext(Dispatchers.IO) {
                                            runCatching {
                                                val uri = Uri.parse(mat.fileUri)
                                                val cr = ctx.contentResolver
                                                val bytes = cr.openInputStream(uri)?.use { it.readBytes() }
                                                    ?: error("Не удалось прочитать файл")
                                                val fileName = fileNameFromContentUri(ctx, uri)
                                                val mimeType = cr.getType(uri)
                                                    ?: defaultMimeByMaterialType(mat.type)
                                                val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                                                val filePart = MultipartBody.Part.createFormData("file", fileName, body)
                                                val titleText = mat.title.ifBlank { "Урок ${idx + 1}" }
                                                ApiClient.withNetworkRetry {
                                                    ApiClient.api.createModule(
                                                        courseId = courseId,
                                                        title = titleText.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                        description = "".toRequestBody("text/plain".toMediaTypeOrNull()),
                                                        materialType = mat.type.ifBlank { "article" }
                                                            .toRequestBody("text/plain".toMediaTypeOrNull()),
                                                        file = filePart
                                                    )
                                                }
                                            }
                                        }
                                        addResult.onFailure { e ->
                                            errors += "Добавление «${mat.title}»: ${e.message}"
                                        }
                                    }

                                    saving = false
                                    if (errors.isEmpty()) {
                                        specialistEditMode = false
                                        refreshTick += 1
                                        Toast.makeText(ctx, "Изменения успешно сохранены", Toast.LENGTH_SHORT).show()
                                    } else {
                                        refreshTick += 1
                                        Toast.makeText(
                                            ctx,
                                            "Частичные ошибки:\n${errors.joinToString("\n")}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            saving -> "Сохранение..."
                            specialistEditMode -> "Сохранить"
                            else -> "Изменить"
                        },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else if (!isSpecialistMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isInCart) Color(0xFF4CAF50) else Color(0xFF006FFD))
                        .clickable {
                            if (!isInCart) {
                                CartRepository.add(
                                    CartItem(
                                        id = courseId,
                                        title = course?.title ?: "Курс #$courseId",
                                        author = course?.specialist_name ?: "Специалист",
                                        price = course?.priceAsDouble() ?: 12000.0
                                    )
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isInCart) "✓  В корзине" else "+  Добавить в корзину",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(Color(0xFFDCE6F5))
                    ) {
                        if (!imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(ctx)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.icon6),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.Center),
                                tint = Color(0xFFB0BDD4)
                            )
                        }

                        Text(
                            text = "✕",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier
                                .padding(start = 20.dp, top = 45.dp)
                                .align(Alignment.TopStart)
                                .clickable { navController.popBackStack() }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CourseTabs(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    when(selectedTab){
                        0 -> {
                            if (isSpecialistMode) {
                                SpecialistEditableIntroductionContent(
                                    loading = loading,
                                    loadError = loadError,
                                    descriptionText = editableDescription,
                                    onDescriptionChange = { editableDescription = it },
                                    outcomesText = editableOutcomes,
                                    onOutcomesChange = { editableOutcomes = it },
                                    selectedTagValues = editableTagValues,
                                    onToggleTag = { value ->
                                        editableTagValues =
                                            if (value in editableTagValues) editableTagValues - value
                                            else editableTagValues + value
                                    },
                                    editMode = specialistEditMode
                                )
                            } else {
                                IntroductionContent(course, descriptionData, loading, loadError)
                            }
                        }
                        1 -> {
                            if (isSpecialistMode && specialistEditMode) {
                                SpecialistEditableSyllabusContent(
                                    materials = editableMaterials,
                                    onMaterialsChange = { editableMaterials = it }
                                )
                            } else {
                                CourseSyllabusContent(
                                    contentData = contentData,
                                    moduleFileById = moduleFileById,
                                    moduleFileByTitle = moduleFileByTitle,
                                    loading = loading,
                                    loadError = loadError,
                                    contentError = contentError
                                )
                            }
                        }
                        2 -> InstructorContent(course, specialistData, loading, loadError)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private fun parseLearningOutcomesText(raw: String?): List<String> {
    val s = raw?.trim().orEmpty()
    if (s.isBlank()) return emptyList()
    return s.split("\n", ";", "•")
        .map { it.trim().trimStart('-', '•') }
        .filter { it.isNotEmpty() }
}

private fun contentModulesToEditableMaterials(
    modules: List<CourseContentModuleResponse>,
    fallbackFileById: Map<Int, String>,
    fallbackFileByTitle: Map<String, String>
): List<CourseMaterial> {
    return modules.mapIndexed { index, module ->
        val title = module.title?.trim().orEmpty().ifBlank { "Урок ${index + 1}" }
        val fileValue = listOf(module.file, module.materialFile, module.url)
            .firstOrNull { !it.isNullOrBlank() }
            ?: fallbackFileById[module.id]
            ?: fallbackFileByTitle[title]
            .orEmpty()
        val fallbackName = fileNameFromUrl(fileValue)
        CourseMaterial(
            id = module.id.takeIf { it > 0 } ?: (index + 1),
            type = module.materialType?.lowercase()?.trim().orEmpty().ifBlank { "article" },
            title = title,
            fileUri = fileValue,
            displayName = fallbackName
        )
    }
}

private fun fileNameFromUrl(url: String): String {
    if (url.isBlank()) return "Файл не выбран"
    return url
        .substringAfterLast('/')
        .substringBefore('?')
        .ifBlank { "Файл не выбран" }
}


private fun fileNameFromContentUri(context: android.content.Context, uri: Uri): String {
    val projection = arrayOf(android.provider.OpenableColumns.DISPLAY_NAME)
    return runCatching {
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIdx >= 0) cursor.getString(nameIdx) else null
            } else null
        }
    }.getOrNull().orEmpty().ifBlank { "module.bin" }
}

private fun defaultMimeByMaterialType(materialType: String): String = when (materialType.lowercase()) {
    "pdf" -> "application/pdf"
    "video" -> "video/*"
    else -> "application/octet-stream"
}


private fun courseTagChoices(): List<Pair<String, String>> = listOf(
    "to_parents" to "РОДИТЕЛЯМ",
    "self_regulation" to "САМОРЕГУЛЯЦИЯ",
    "logical_thinking" to "ЛОГИЧЕСКОЕ МЫШЛЕНИЕ",
    "learning_through_play" to "ОБУЧЕНИЕ ЧЕРЕЗ ИГРУ",
    "for_children" to "ДЕТЯМ",
    "easy_start" to "ЛЕГКИЙ СТАРТ",
    "speech_therapy_work" to "ЛОГОПЕДИЧЕСКАЯ РАБОТА",
    "social_skills_start" to "СОЦИАЛЬНЫЕ НАВЫКИ СТАРТ",
    "with_parent" to "С УЧАСТИЕМ РОДИТЕЛЯ",
    "speech_understanding" to "ПОНИМАНИЕ РЕЧИ",
    "gradual_development" to "ПОСТЕПЕННОЕ РАЗВИТИЕ",
    "memory" to "ПАМЯТЬ",
    "intensive_course" to "ИНТЕНСИВНЫЙ КУРС"
)

private fun normalizeCourseTagValue(raw: String): String {
    val source = raw.trim()
    if (source.isBlank()) return ""
    val byValue = courseTagChoices().firstOrNull { it.first.equals(source, ignoreCase = true) }
    if (byValue != null) return byValue.first
    val byLabel = courseTagChoices().firstOrNull { it.second.equals(source, ignoreCase = true) }
    return byLabel?.first ?: source.lowercase()
}

private fun courseTagLabel(value: String): String {
    val normalized = normalizeCourseTagValue(value)
    return courseTagChoices().firstOrNull { it.first == normalized }?.second ?: value.uppercase()
}

@Composable
fun IntroductionContent(
    course: CourseCardResponse?,
    descriptionData: CourseDescriptionResponse?,
    loading: Boolean,
    loadError: String?
) {
    val descriptionText = descriptionData?.description
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: course?.let { "Курс: ${it.title}. Специалист: ${it.specialist_name}. Рейтинг: ${it.average_rating}." }
        ?: "Описание курса пока недоступно."
    val outcomes = descriptionData?.learningOutcomesList().orEmpty()
    val tags = descriptionData?.tagLabels().orEmpty()

    val outcomesText = when {
        loading -> "Загрузка..."
        !loadError.isNullOrBlank() -> "Не удалось загрузить результаты обучения"
        outcomes.isEmpty() -> "• Информация скоро появится"
        else -> outcomes.joinToString("\n") { "• $it" }
    }
    val displayTags = if (tags.isNotEmpty()) {
        tags
    } else {
        listOf(
            "РОДИТЕЛЯМ",
            course?.level?.let {
                when (it.lowercase()) {
                    "beginner" -> "ДЛЯ НАЧИНАЮЩИХ"
                    "intermediate" -> "БАЗОВЫЙ"
                    "advanced" -> "ПРОДВИНУТЫЙ"
                    else -> "ДЛЯ НАЧИНАЮЩИХ"
                }
            } ?: "ДЛЯ НАЧИНАЮЩИХ"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text("О курсе", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(10.dp))
        SectionInfoCard(descriptionText)

        Spacer(modifier = Modifier.height(20.dp))
        Text("Чему вы научитесь?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))
        SectionInfoCard(outcomesText)

        Spacer(modifier = Modifier.height(20.dp))
        Text("Теги", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            displayTags.forEach { Tag(it.uppercase()) }
        }
    }
}

@Composable
fun SpecialistEditableIntroductionContent(
    loading: Boolean,
    loadError: String?,
    descriptionText: String,
    onDescriptionChange: (String) -> Unit,
    outcomesText: String,
    onOutcomesChange: (String) -> Unit,
    selectedTagValues: Set<String>,
    onToggleTag: (String) -> Unit,
    editMode: Boolean
) {
    val outcomes = outcomesText.lines().map { it.trim() }.filter { it.isNotEmpty() }
    val tags = selectedTagValues.toList()
    var tagsExpanded by remember(editMode) { mutableStateOf(false) }
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF006FFD),
        unfocusedBorderColor = Color(0xFFD8DCE2),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text("О курсе", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))

        if (loading) {
            Text("Загрузка...", color = Color.Gray)
            return@Column
        }
        if (!loadError.isNullOrBlank() && descriptionText.isBlank()) {
            Text("Не удалось загрузить данные курса", color = Color.Gray)
            return@Column
        }

        if (editMode) {
            OutlinedTextField(
                value = descriptionText,
                onValueChange = onDescriptionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp),
                placeholder = { Text("Введите описание курса", color = Color.Gray) },
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            SectionInfoCard(descriptionText)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Чему вы научитесь?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))

        if (editMode) {
            OutlinedTextField(
                value = outcomesText,
                onValueChange = onOutcomesChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = { Text("Каждый пункт с новой строки", color = Color.Gray) },
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            SectionInfoCard(
                if (outcomes.isEmpty()) {
                    "• Добавьте результаты обучения"
                } else {
                    outcomes.joinToString("\n") { "• $it" }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Теги", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))

        if (editMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFD8DCE2), RoundedCornerShape(12.dp))
                    .clickable { tagsExpanded = !tagsExpanded }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = if (tagsExpanded) "Скрыть теги" else "Выбрать теги",
                    color = Color(0xFF5E6168),
                    fontSize = 14.sp
                )
            }
            if (tagsExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    courseTagChoices().forEach { (value, label) ->
                        SelectableTagChip(
                            text = label,
                            selected = value in selectedTagValues
                        ) { onToggleTag(value) }
                    }
                }
            }
        } else {
            if (tags.isEmpty()) {
                Text("Теги пока не указаны", color = Color.Gray)
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { Tag(courseTagLabel(it)) }
                }
            }
        }
    }
}


@Composable
fun InstructorContent(
    course: CourseCardResponse?,
    specialistData: CourseSpecialistResponse?,
    loading: Boolean,
    loadError: String?
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val avatarUrl = resolveCourseImageUrl(specialistData?.avatar)
    val specialistName = specialistData?.fullName?.trim().takeUnless { it.isNullOrEmpty() }
        ?: course?.specialist_name
        ?: "Специалист"
    val roleFromApi = specialistData?.specialistRoleSubtitle()?.trim().orEmpty()
    val subtitle = if (roleFromApi.isNotEmpty()) {
        roleFromApi
    } else {
        buildString {
            append("Рейтинг ${course?.average_rating ?: "—"} • ${course?.price ?: "—"} ₸")
        }
    }
    val description = specialistData?.approachDescription?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: specialistData?.specializationsForDescription()?.takeIf { it.isNotEmpty() }
        ?: course?.let { "Курс: ${it.title}. Специалист ${it.specialist_name} с рейтингом ${it.average_rating}." }
        ?: "Информация о специалисте пока недоступна."

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(ctx).data(avatarUrl).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.icon_user),
                        contentDescription = null,
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = specialistName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = when {
                loading -> "Загрузка..."
                !loadError.isNullOrBlank() -> "Не удалось загрузить данные специалиста"
                else -> description
            },
            fontSize = 14.sp,
            color = Color.Gray,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun LessonItem(title: String, type: String, time: String) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF4F6FA))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(type, color = Color.Gray, fontSize = 12.sp)
        }

        if (time.isNotEmpty()) {
            Text(time, color = Color(0xFF3D6DE0))
        }
    }
}

@Composable
fun Tag(text: String) {

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFEAF2FF))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, color = Color(0xFF2E63B8), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SelectableTagChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Color(0xFF006FFD) else Color(0xFFEAF2FF))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF2E63B8),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SectionInfoCard(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF9FAFC))
            .border(1.dp, Color(0xFFD9DDE5), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF5E6168),
            fontSize = 14.sp,
            lineHeight = 21.sp
        )
    }
}

private fun Modifier.courseDashedBorder(): Modifier = drawBehind {
    val stroke = Stroke(
        width = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    )
    drawRoundRect(
        color = Color(0xFFD1D5DB),
        style = stroke,
        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
    )
}

@Composable
fun CourseTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {

    val tabs = listOf("Описание", "Содержание", "Специалист")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Color(0xFFF1F3F7)),
        verticalAlignment = Alignment.CenterVertically
    ) {

        tabs.forEachIndexed { index, title ->

            val selected = index == selectedTab

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        if (selected) Color.White else Color.Transparent
                    )
                    .clickable { onTabSelected(index) },
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) Color.Black else Color.Gray
                )
            }
        }
    }
}

@Composable //Силабус
fun CourseSyllabusContent(
    contentData: CourseContentResponse?,
    moduleFileById: Map<Int, String>,
    moduleFileByTitle: Map<String, String>,
    loading: Boolean,
    loadError: String?,
    contentError: String?
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val modules = contentData?.modules.orEmpty()
    val modulesCountText = contentData?.modulesCount?.trim()?.takeIf { it.isNotEmpty() } ?: modules.size.toString()
    val durationText = contentData?.duration?.trim()?.takeIf { it.isNotEmpty() } ?: "—"
    val summaryText = "$modulesCountText урока • $durationText"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Контент курса",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = summaryText,
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            loading -> Text("Загрузка контента...", color = Color.Gray)
            !contentError.isNullOrBlank() -> Text("Ошибка загрузки содержания: $contentError", color = Color.Gray)
            !loadError.isNullOrBlank() -> Text("Не удалось загрузить содержание", color = Color.Gray)
            modules.isEmpty() -> Text("Контент курса пока недоступен", color = Color.Gray)
            else -> {
                modules.forEachIndexed { index, module ->
                    val title = module.title?.trim().takeUnless { it.isNullOrEmpty() } ?: "Урок ${index + 1}"
                    val type = module.materialTypeLabel?.trim()
                        ?.takeIf { it.isNotEmpty() }
                        ?: module.materialType?.trim()
                            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        ?: "Материал"
                    val materialUrl = resolveLessonMaterialUrl(module, moduleFileById, moduleFileByTitle)
                    LessonCard(
                        title = title,
                        type = type,
                        time = null,
                        onClick = {
                            val url = materialUrl?.trim()?.takeIf { it.isNotEmpty() }
                            if (url.isNullOrBlank()) {
                                Toast.makeText(ctx, "Файл для этого урока пока недоступен", Toast.LENGTH_SHORT).show()
                                return@LessonCard
                            }
                            val uri = Uri.parse(url)
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                addCategory(Intent.CATEGORY_BROWSABLE)
                            }
                            val chooser = Intent.createChooser(intent, title)
                            runCatching { ctx.startActivity(chooser) }
                                .onFailure {
                                    runCatching { ctx.startActivity(intent) }
                                        .onFailure {
                                            Toast.makeText(ctx, "Не удалось открыть файл", Toast.LENGTH_SHORT).show()
                                        }
                                }
                        }
                    )
                    if (index < modules.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecialistEditableSyllabusContent(
    materials: List<CourseMaterial>,
    onMaterialsChange: (List<CourseMaterial>) -> Unit
) {
    fun nextTempId(): Int = (materials.map { it.id }.filter { it < 0 }.minOrNull() ?: 0) - 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text("Материалы курса", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Загрузите файлы вашего курса в нужном порядке", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MaterialButton("Добавить Артикль", R.drawable.ic_material_article) {
                onMaterialsChange(materials + CourseMaterial(nextTempId(), "article"))
            }
            MaterialButton("Добавить PDF", R.drawable.ic_material_pdf) {
                onMaterialsChange(materials + CourseMaterial(nextTempId(), "pdf"))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        MaterialButton("Добавить Видео", R.drawable.ic_material_video) {
            onMaterialsChange(materials + CourseMaterial(nextTempId(), "video"))
        }

        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .courseDashedBorder()
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                materials.forEach { material ->
                    MaterialCard(
                        material = material,
                        onMaterialChange = { updated ->
                            onMaterialsChange(materials.map { if (it.id == updated.id) updated else it })
                        },
                        onDelete = {
                            onMaterialsChange(materials.filter { it.id != material.id })
                        }
                    )
                }
                if (materials.isEmpty()) {
                    Text("Добавьте материалы", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun LessonCard(
    title: String,
    type: String,
    time: String?,
    onClick: () -> Unit = {}
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF3F5F9))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painter = painterResource(R.drawable.icon_lesson_doc),
            contentDescription = null,
            modifier = Modifier.size(44.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = type,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        if (time != null) {

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE7F0FF))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = Color(0xFF2F6DE1),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewInstructorContent() {
    InstructorContent(
        course = null,
        specialistData = null,
        loading = false,
        loadError = null
    )
}