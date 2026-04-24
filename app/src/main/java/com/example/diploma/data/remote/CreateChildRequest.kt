package com.example.diploma.data.remote

data class ChildProfileResponse(
    val id: Int,
    val name: String,
    val age: Int,
    val avatar: String? = null,
    val development_type: String,
    val communication_style: String,
    val understands_instructions: String,
    val sensory_sensitivities: String,
    val motor_difficulties: String,
    val behavior_notices: String,
    val motivators: String,
    val interests: String,
    val comfortable_duration: String,
    val created_at: String?
)

data class CreateChildRequest(
    val name: String,
    val age: Int,
    val development_type: String,
    val communication_style: String,
    val understands_instructions: String,
    val sensory_sensitivities: String,
    val motor_difficulties: String,
    val behavior_notices: String,
    val motivators: String,
    val interests: String,
    val comfortable_duration: String
)