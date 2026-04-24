package kz.aruzhan.care_steps.data.remote

import com.google.gson.annotations.SerializedName

data class CreateMoodTrackingRequest(
    val child: Int,
    @SerializedName("overall_feeling") val overallFeeling: String,
    val emotions: List<String>,
    val observations: List<String>,
    @SerializedName("sleep_quality") val sleepQuality: String,
    @SerializedName("appetite_quality") val appetiteQuality: String,
    val note: String
)

data class MoodTrackingResponse(
    val id: Int,
    val child: Int,
    @SerializedName("overall_feeling") val overallFeeling: String,
    val emotions: List<String>,
    val observations: List<String>,
    @SerializedName("sleep_quality") val sleepQuality: String,
    @SerializedName("appetite_quality") val appetiteQuality: String,
    val note: String,
    @SerializedName("created_at") val createdAt: String
)

data class MoodTrackingSummaryResponse(
    val period: String? = null,
    val date: String? = null,
    val range: MoodRange? = null,
    @SerializedName("total_entries") val totalEntries: Int? = null,
    @SerializedName("calendar_days") val calendarDays: List<MoodCalendarDay> = emptyList(),
    val sleep: MoodQualitySummary? = null,
    val appetite: MoodQualitySummary? = null,
    val donut: MoodDonutSummary? = null,
    @SerializedName("period_mood") val periodMood: MoodPeriodMood? = null
)

data class MoodRange(
    val start: String? = null,
    val end: String? = null
)

data class MoodCalendarDay(
    val date: String? = null,
    @SerializedName("weekday_short_ru") val weekdayShortRu: String? = null,
    @SerializedName("mood_score") val moodScore: Int?
    ,
    @SerializedName("has_data") val hasData: Boolean? = null
)

data class MoodQualitySummary(
    @SerializedName("score_avg") val scoreAvg: Float? = null,
    val trend: String? = null,
    @SerializedName("summary_key") val summaryKey: String? = null,
    @SerializedName("summary_ru") val summaryRu: String? = null,
    val breakdown: MoodQualityBreakdown? = null
)

data class MoodDonutSummary(
    @SerializedName("good_pct") val goodPct: Float? = null,
    @SerializedName("medium_pct") val mediumPct: Float? = null,
    @SerializedName("low_pct") val lowPct: Float? = null,
    val counts: MoodDonutCounts? = null
)

data class MoodQualityBreakdown(
    val good: Int? = null,
    val normal: Int? = null,
    val bad: Int? = null
)

data class MoodDonutCounts(
    val good: Int? = null,
    val medium: Int? = null,
    val low: Int? = null
)

data class MoodPeriodMood(
    @SerializedName("verdict_key") val verdictKey: String? = null,
    @SerializedName("verdict_ru") val verdictRu: String? = null,
    val counts: MoodDonutCounts? = null
)
