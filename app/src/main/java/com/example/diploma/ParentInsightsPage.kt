package com.example.diploma

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.diploma.data.remote.ApiClient
import com.example.diploma.data.remote.MoodTrackingSummaryResponse
import com.example.diploma.data.remote.TokenStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ParentInsightsPage(navController: NavController) {
    var summary by remember { mutableStateOf<MoodTrackingSummaryResponse?>(null) }
    var childName by remember { mutableStateOf("Ребенок") }
    var childId by remember { mutableStateOf<Int?>(null) }
    var selectedPeriod by remember { mutableStateOf("week") }
    var accountKey by remember { mutableStateOf("") }

    LaunchedEffect(SurveyState.completedToday, selectedPeriod) {
        val currentAccountKey = TokenStorage.accessToken.orEmpty().hashCode().toString()
        if (accountKey != currentAccountKey) {
            accountKey = currentAccountKey
            SurveyState.completedToday = false
            SurveyState.todayMoodScore = null
            summary = null
        }

        val children = runCatching { ApiClient.api.getChildren() }.getOrNull().orEmpty()
        val firstChild = children.minByOrNull { it.id }
        childName = firstChild?.name ?: "Ребенок"
        childId = firstChild?.id

        val res = runCatching {
            ApiClient.api.getMoodTrackingSummary(
                childId = childId,
                period = selectedPeriod
            )
        }.getOrNull()
        summary = if (res?.isSuccessful == true) res.body() else null

        val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val todayFromApi = summary?.calendarDays
            ?.firstOrNull { it.date == todayIso && (it.hasData == true || it.moodScore != null) }
        if (todayFromApi != null) {
            SurveyState.completedToday = true
            SurveyState.todayMoodScore = todayFromApi.moodScore
        } else {
            // Если сервер не вернул запись за сегодня — считаем, что сегодня опрос не пройден.
            SurveyState.completedToday = false
            SurveyState.todayMoodScore = null
        }
    }

    val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val apiByDate = summary?.calendarDays?.associateBy { it.date ?: "" } ?: emptyMap()
    val weekStrip = buildWeekStripMonFirst()
    val weekDates = weekStrip.map { it.iso }
    val weekLabels = weekStrip.map { it.weekdayRu }
    val weekNumbers = weekStrip.map { it.dayOfMonth }
    val todayIndex = weekDates.indexOf(todayIso)
    val weekScores = weekStrip.map { day ->
        val fromApi = apiByDate[day.iso]?.moodScore
        if (day.iso == todayIso && SurveyState.todayMoodScore != null) {
            SurveyState.todayMoodScore
        } else {
            fromApi
        }
    }
    val latestMoodIndex = weekScores.indexOfLast { it != null }
    // Всегда подсвечиваем реальный «сегодня» в полоске недели (как в заголовке),
    // а не последний день с данными из API — иначе при отсутствии записи за сегодня
    // выделялся бы, например, вторник с эмодзи.
    val selectedIndex = when {
        todayIndex in 0..6 -> todayIndex
        latestMoodIndex >= 0 -> latestMoodIndex
        else -> 3
    }
    val hasAnyData = summary?.calendarDays?.any { it.hasData == true || it.moodScore != null } == true
    val sleepValue = if (hasAnyData) {
        summary?.sleep.toCardText(defaultText = "Нет данных за период")
    } else {
        "Нет данных за период"
    }
    val appetiteValue = if (hasAnyData) {
        summary?.appetite.toCardText(defaultText = "Нет данных за период")
    } else {
        "Нет данных за период"
    }
    val sleepColor = if (hasAnyData) summary?.sleep.toCardColor(defaultColor = Color(0xFF7D7D7D)) else Color(0xFF8D8D8D)
    val appetiteColor = if (hasAnyData) summary?.appetite.toCardColor(defaultColor = Color(0xFF7D7D7D)) else Color(0xFF8D8D8D)
    val donut = if (hasAnyData) summary?.donut.toDonutSweeps() else Triple(0f, 0f, 0f)
    val todayText = run {
        val locale = Locale("ru")
        val date = SimpleDateFormat("d MMMM", locale).format(Date())
        "Сегодня, $date"
    }

    Scaffold(
        bottomBar = { BottomBar(navController, selectedIndex = 0) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9EC9FF))
                )
                Spacer(modifier = Modifier.size(10.dp))
                Column {
                    Text(childName, fontWeight = FontWeight.Bold, fontSize = 33.sp / 2)
                    Text(todayText, color = Color(0xFF2E2E2E), fontSize = 16.sp / 1.2f)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            WeekMoodCard(weekScores, weekLabels, weekNumbers, selectedIndex)
            Spacer(modifier = Modifier.height(14.dp))

            if (SurveyState.completedToday) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0A73F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        "Вы прошли опрос на сегодня",
                        fontSize = 15.sp,
                        color = Color(0xFF555555)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0A73F0))
                        .clickable { navController.navigate("DailySurveyPage") },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_daily_survey),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Пройти ежедневный опрос", color = Color.White, fontSize = 18.sp / 1.2f, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusSmallCard(
                    title = "😴Сон",
                    value = sleepValue,
                    valueColor = sleepColor,
                    modifier = Modifier.weight(1f)
                )
                StatusSmallCard(
                    title = "🍽️Апетит",
                    value = appetiteValue,
                    valueColor = appetiteColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            WeekStateCard(
                donut = donut,
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it }
            )
        }
    }
}

@Composable
private fun WeekMoodCard(scores: List<Int?>, labels: List<String>, numbers: List<String>, selectedTodayIndex: Int) {
    val days = if (labels.size == 7) labels else listOf("СР", "ЧТ", "ПТ", "СБ", "ВС", "ПН", "ВТ")
    val dayNumbers = if (numbers.size == 7) numbers else listOf("", "", "", "", "23", "24", "25")
    val padded = (scores + List(7) { null }).take(7)
    val moods = padded.map {
        when {
            it == null -> null
            it >= 4 -> R.drawable.ic_mood_happy
            it == 3 -> R.drawable.ic_mood_neutral
            else -> R.drawable.ic_mood_sad
        }
    }
    val selected = if (selectedTodayIndex in 0..6) selectedTodayIndex else 3

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF4F6FA))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEachIndexed { index, d ->
            val isSelected = index == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) Color(0xFF0A73F0) else Color.Transparent)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(d, color = if (isSelected) Color.White else Color(0xFF8B8B8B), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                if (moods[index] != null) {
                    Image(
                        painter = painterResource(moods[index]!!),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(dayNumbers[index], color = if (isSelected) Color.White else Color(0xFF4A4A4A), fontSize = 18.sp / 1.1f)
                }
            }
        }
    }
}

@Composable
private fun StatusSmallCard(title: String, value: String, valueColor: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF9FAFC))
            .padding(12.dp)
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = valueColor, fontSize = 14.sp / 1.2f)
    }
}

@Composable
private fun WeekStateCard(
    donut: Triple<Float, Float, Float>,
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
) {
    var periodExpanded by remember { mutableStateOf(false) }
    val periodTitle = when (selectedPeriod) {
        "day" -> "В день"
        "month" -> "В месяц"
        else -> "В неделю"
    }
    val stateTitle = when (selectedPeriod) {
        "day" -> "Состояние дня"
        "month" -> "Состояние месяца"
        else -> "Состояние недели"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(bottom = 12.dp)
            .background(Color(0xFFFCFDFE), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stateTitle, fontSize = 34.sp / 2, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF2F4F8))
                    .border(1.dp, Color(0xFFE6EAF2), RoundedCornerShape(10.dp))
                    .clickable { periodExpanded = true }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        periodTitle,
                        color = Color(0xFF2D3A55),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF7B8798),
                        modifier = Modifier.size(16.dp)
                    )
                }
                DropdownMenu(
                    expanded = periodExpanded,
                    onDismissRequest = { periodExpanded = false },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "В день",
                                fontWeight = if (selectedPeriod == "day") FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedPeriod == "day") Color(0xFF0A73F0) else Color(0xFF2D3A55)
                            )
                        },
                        onClick = {
                            periodExpanded = false
                            onPeriodSelected("day")
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "В неделю",
                                fontWeight = if (selectedPeriod == "week") FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedPeriod == "week") Color(0xFF0A73F0) else Color(0xFF2D3A55)
                            )
                        },
                        onClick = {
                            periodExpanded = false
                            onPeriodSelected("week")
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "В месяц",
                                fontWeight = if (selectedPeriod == "month") FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedPeriod == "month") Color(0xFF0A73F0) else Color(0xFF2D3A55)
                            )
                        },
                        onClick = {
                            periodExpanded = false
                            onPeriodSelected("month")
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(130.dp)) {
                val stroke = 50f
                val inset = stroke / 2f
                val goodSweep = donut.first
                val normalSweep = donut.second
                val badSweep = donut.third
                drawArc(Color(0xFF7CB52E), 0f, goodSweep, false,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(stroke, cap = StrokeCap.Butt))
                drawArc(Color(0xFFF79C00), goodSweep, normalSweep, false,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(stroke, cap = StrokeCap.Butt))
                drawArc(Color(0xFF0F63CC), goodSweep + normalSweep, badSweep, false,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(stroke, cap = StrokeCap.Butt))
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendDot(Color(0xFF7CB52E), "Хорошое")
                LegendDot(Color(0xFFF79C00), "Среднее")
                LegendDot(Color(0xFF0F63CC), "Низкое")
            }
        }
    }
}

private fun com.example.diploma.data.remote.MoodQualitySummary?.toCardText(defaultText: String): String {
    if (this == null) return defaultText
    return summaryRu ?: defaultText
}

private fun com.example.diploma.data.remote.MoodQualitySummary?.toCardColor(defaultColor: Color): Color {
    if (this == null) return defaultColor
    return when (summaryKey) {
        "good" -> Color(0xFF76B82A)
        "bad", "unstable" -> Color(0xFFE53935)
        else -> Color(0xFF7D7D7D)
    }
}

private fun com.example.diploma.data.remote.MoodDonutSummary?.toDonutSweeps(): Triple<Float, Float, Float> {
    if (this == null) return Triple(250f, 70f, 40f)
    val g = (goodPct ?: 0f).coerceAtLeast(0f)
    val n = (mediumPct ?: 0f).coerceAtLeast(0f)
    val b = (lowPct ?: 0f).coerceAtLeast(0f)
    val sum = g + n + b
    if (sum <= 0f) return Triple(250f, 70f, 40f)
    return Triple((g / sum) * 360f, (n / sum) * 360f, (b / sum) * 360f)
}

@Composable
private fun LegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.size(8.dp))
        Text(text, fontSize = 24.sp / 1.5f, color = Color(0xFF222222))
    }
}

private data class WeekDayStrip(
    val iso: String,
    val weekdayRu: String,
    val dayOfMonth: String
)

/** ПН–ВС текущей календарной недели; даты и подписи совпадают с реальным временем устройства. */
private fun buildWeekStripMonFirst(): List<WeekDayStrip> {
    val labels = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
    val cal = Calendar.getInstance(TimeZone.getDefault())
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val daysFromMonday = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
    cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
    val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return (0 until 7).map { off ->
        val c = cal.clone() as Calendar
        c.add(Calendar.DAY_OF_MONTH, off)
        WeekDayStrip(
            iso = isoFmt.format(c.time),
            weekdayRu = labels[off],
            dayOfMonth = c.get(Calendar.DAY_OF_MONTH).toString()
        )
    }
}
