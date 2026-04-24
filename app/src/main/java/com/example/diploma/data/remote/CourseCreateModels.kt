package kz.aruzhan.care_steps.data.remote

import com.google.gson.annotations.SerializedName

data class CoursePreviewResponse(
    val id: Int,
    val preview_image: String?
)

data class ChoiceItem(
    val value: String,
    val label: String
)

data class CourseChoicesResponse(
    val category: List<ChoiceItem> = emptyList(),
    val level: List<ChoiceItem> = emptyList(),
    @SerializedName("course_tag") val courseTag: List<ChoiceItem> = emptyList()
)

data class CreateCourseModuleRequest(
    val title: String,
    val description: String,
    val material_type: String,
    val file: String
)

data class CreatedCourseModuleResponse(
    val id: Int,
    val title: String,
    val description: String,
    val material_type: String,
    val file: String,
    val created_at: String
)

data class CreatedCourseResponse(
    val id: Int,
    val title: String,
    val description: String,
    val learning_outcomes: String? = null,
    val tags: List<String> = emptyList(),
    val category: String,
    val level: String,
    val price: String,
    val duration: Int,
    val preview_image: String?,
    /** Если бэкенд отдаёт рейтинг в списке «мои курсы». */
    val average_rating: Double? = null,
    /** В списке курсов бэкенд может не отдавать модули — тогда null или []. */
    val modules: List<CreatedCourseModuleResponse>? = null
)

data class PurchaseCourseResponse(
    val id: Int,
    val course_id: Int,
    val course_title: String?,
    val created_at: String
)

data class UpdateCourseModuleItem(
    val id: Int? = null,
    val title: String,
    val description: String,
    val material_type: String,
    val file: String
)

data class UpdateCourseRequest(
    val title: String,
    val description: String,
    val learning_outcomes: String,
    val tags: List<String>,
    val category: String,
    val level: String,
    val price: String,
    val duration: Int,
    val modules: List<UpdateCourseModuleItem> = emptyList()
)

data class PartialCourseUpdateRequest(
    val description: String? = null,
    val learning_outcomes: String? = null,
    val tags: List<String>? = null
)
