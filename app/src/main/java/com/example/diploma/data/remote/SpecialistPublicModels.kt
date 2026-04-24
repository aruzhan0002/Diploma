package com.example.diploma.data.remote

import com.google.gson.JsonElement

/**
 * Карточка специалиста для родителя (GET с query q, specialization_search).
 */
data class SpecialistCardResponse(
    val id: Int,
    val full_name: String? = null,
    val specialization: String? = null,
    val avatar: String? = null,
    val average_rating: Double? = null,
    val reviews_count: Int? = null,
    val years_experience: String? = null,
    val price_from: String? = null,
    val currency: String? = null,
    val short_description: String? = null
)

/**
 * Полная карточка специалиста для детального экрана профиля
 * (GET /api/auth/public/specialists/cards/{specialist_id}/).
 *
 * Поля `specializations`, `methods`, `languages`, `development_types` бэкенд
 * может отдавать как строку или как JSON-массив — поэтому принимаем `JsonElement`
 * и форматируем на клиенте. `years_experience` приходит числом в деталях и строкой
 * в списке, поэтому тоже `JsonElement`.
 *
 * `work_format` в детальной карточке может быть строкой или объектом `{ value, label }`.
 */
data class SpecialistDetailResponse(
    val id: Int,
    val full_name: String? = null,
    val avatar: String? = null,
    val average_rating: Double? = null,
    val reviews_count: Int? = null,
    val price_from: String? = null,
    val currency: String? = null,
    val approach_description: String? = null,
    val specializations: JsonElement? = null,
    val methods: JsonElement? = null,
    val languages: JsonElement? = null,
    val development_types: JsonElement? = null,
    val work_format: JsonElement? = null,
    val years_experience: JsonElement? = null,
    val age_range: String? = null,
    val city: String? = null,
    val time_zone: String? = null,
    val provide_individual_consultations: Boolean? = null,
    val work_with_child_through_parent: Boolean? = null,
    val provide_recommendations_and_plans: Boolean? = null,
    val track_progress_and_analytics: Boolean? = null
)
