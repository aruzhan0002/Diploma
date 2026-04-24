package com.example.diploma.data.remote

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonElement

// ---------- REQUESTS ----------
data class RegisterRequest(
    val email: String,
    val password: String,
    val password_confirm: String
)

data class LoginRequest(
    // Backend auth endpoint expects `username` field even for email login.
    @SerializedName("username") val email: String,
    val password: String
)

data class RefreshRequest(
    val refresh: String
)

data class PasswordResetRequestBody(
    val email: String
)

data class PasswordResetVerifyBody(
    val email: String,
    val code: String
)

data class PasswordResetConfirmBody(
    val reset_token: String,
    val new_password: String,
    val new_password_confirm: String
)

data class ProfileRequest(
    val full_name: String,
    val relationship: String,
    val relationship_other: String? = null,
    val avatar: String? = null
)

data class RegisterResponse(
    val message: String?,
    val user_id: Int?
)

data class TokenResponse(
    val access: String,
    val refresh: String,
    val user_type: String? = null
)

data class AccessResponse(
    val access: String
)

data class PasswordResetRequestResponse(
    val email: String? = null,
    val retry_after_seconds: Int? = null,
    val resend_after_seconds: Int? = null,
    val detail: String? = null,
    val message: String? = null
)

data class PasswordResetVerifyResponse(
    val reset_token: String? = null,
    val email: String? = null,
    val code: String? = null,
    val detail: String? = null
)

data class PasswordResetConfirmResponse(
    val access: String? = null,
    val refresh: String? = null,
    val user_type: String? = null,
    val detail: String? = null
)

data class ProfileResponse(
    val id: Int,
    val email: String,
    val full_name: String,
    val relationship: String,
    val relationship_other: String?,
    val avatar: String? = null
)

data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String,
    val new_password_confirm: String
)

data class ParentAddressRequest(
    val address: String
)

data class ParentAddressResponse(
    val id: Int,
    val address: String
)

data class SpecialistRequest(
    val full_name: String,
    val approach_description: String
)

data class SpecialistDescriptionRequest(
    val specializations: String,
    val years_experience: Int,
    val methods: String,
    val age_range: String,
    val development_types: String,
    val work_format: String,
    val languages: String,
    val time_zone: String,
    val city: String,
    val provide_individual_consultations: Boolean,
    val work_with_child_through_parent: Boolean,
    val provide_recommendations_and_plans: Boolean,
    val track_progress_and_analytics: Boolean
)

/** GET /api/auth/settings/specialist/ — сервер отдаёт массивы и число, не строки. */
data class SpecialistSettingsResponse(
    val email: String = "",
    val full_name: String = "",
    val approach_description: String? = null,
    val avatar: String? = null,
    val specializations: JsonElement? = null,
    val years_experience: JsonElement? = null,
    val methods: JsonElement? = null,
    val age_range: String? = null,
    val work_format: String? = null,
    val time_zone: String? = null,
    val city: String? = null
)

data class SpecialistSettingsUpdateRequest(
    val full_name: String? = null,
    val approach_description: String? = null,
    val specializations: List<String>? = null,
    val years_experience: Int? = null,
    val methods: List<String>? = null,
    val age_range: String? = null,
    val work_format: String? = null,
    val time_zone: String? = null,
    val city: String? = null
)
