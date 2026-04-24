package kz.aruzhan.care_steps.data.remote

import com.google.gson.annotations.SerializedName

data class ChatBotRequest(
    val message: String,
    @SerializedName("session_id") val sessionId: Int? = null
)

data class ChatBotResponse(
    @SerializedName("session_id") val sessionId: Int?,
    val reply: String?,
    val model: String?
)
