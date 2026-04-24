package kz.aruzhan.care_steps

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kz.aruzhan.care_steps.data.remote.ApiClient
import kz.aruzhan.care_steps.data.remote.CreateMoodTrackingRequest
import kotlinx.coroutines.launch

object SurveyState {
    var completedToday by mutableStateOf(false)
    var todayMoodScore by mutableStateOf<Int?>(null)
}

private val Blue = Color(0xFF0A73F0)
private val LightBlue = Color(0xFFEAF2FF)
private val GrayBg = Color(0xFFF2F4F8)
private val BorderGray = Color(0xFFE0E0E0)

private data class SurveyOption(val emoji: String, val label: String)

@Composable
fun DailySurveyPage(navController: NavController) {
    var currentStep by remember { mutableIntStateOf(0) }

    val step1Options = listOf(
        SurveyOption("😊", "Хорошо"),
        SurveyOption("😐", "Нормально"),
        SurveyOption("😟", "Сложно"),
        SurveyOption("😢", "Очень тяжело")
    )
    var step1Selected by remember { mutableStateOf(-1) }

    val step2Options = listOf(
        SurveyOption("😊", "Радостный"),
        SurveyOption("😌", "Спокойный"),
        SurveyOption("😰", "Тревожный"),
        SurveyOption("😠", "Раздраженный"),
        SurveyOption("🥱", "Уставший"),
        SurveyOption("😢", "Грустный")
    )
    val step2Selected = remember { mutableStateListOf<Int>() }

    val step3Options = listOf(
        SurveyOption("🤝", "Был контактным"),
        SurveyOption("🙈", "Избегал общения"),
        SurveyOption("🎯", "Был сосредоточен"),
        SurveyOption("🔄", "Повторяющееся поведение"),
        SurveyOption("🧸", "Играл спокойно"),
        SurveyOption("😤", "Были истерики")
    )
    val step3Selected = remember { mutableStateListOf<Int>() }

    val sleepOptions = listOf("Хороший", "Обычный", "Плохой")
    var sleepSelected by remember { mutableIntStateOf(-1) }
    val appetiteOptions = listOf("Хороший", "Обычный", "Плохой")
    var appetiteSelected by remember { mutableIntStateOf(-1) }

    var noteText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var childId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    val feelingMap = listOf("good", "normal", "hard", "very_hard")
    val emotionMap = listOf("joyful", "calm", "anxious", "irritated", "tired", "sad")
    val observationMap = listOf("contactful", "avoided_communication", "focused", "repetitive_behavior", "played_calmly", "had_meltdowns")
    val qualityMap = listOf("good", "normal", "bad")

    LaunchedEffect(Unit) {
        val children = runCatching { ApiClient.api.getChildren() }.getOrNull().orEmpty()
        childId = children.minByOrNull { it.id }?.id
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        SurveyTopBar(onBack = {
            if (currentStep > 0) currentStep-- else navController.popBackStack()
        })

        Spacer(modifier = Modifier.height(16.dp))

        StepProgressBar(currentStep = currentStep, totalSteps = 5)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            when (currentStep) {
                0 -> StepMood(step1Options, step1Selected) { step1Selected = it }
                1 -> StepMultiSelect(
                    title = "Какие эмоции были сегодня?",
                    options = step2Options,
                    selected = step2Selected
                )
                2 -> StepMultiSelect(
                    title = "Что вы заметили сегодня?",
                    options = step3Options,
                    selected = step3Selected
                )
                3 -> StepFactors(
                    sleepOptions, sleepSelected, { sleepSelected = it },
                    appetiteOptions, appetiteSelected, { appetiteSelected = it }
                )
                4 -> StepNote(noteText) { noteText = it }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        errorText?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        Button(
            onClick = {
                if (currentStep < 4) {
                    currentStep++
                } else {
                    scope.launch {
                        isLoading = true
                        errorText = null
                        val resolvedChildId = childId
                        if (resolvedChildId == null) {
                            isLoading = false
                            errorText = "Не найден ребенок для отправки опроса"
                            return@launch
                        }
                        val request = CreateMoodTrackingRequest(
                            child = resolvedChildId,
                            overallFeeling = if (step1Selected >= 0) feelingMap[step1Selected] else "normal",
                            emotions = step2Selected.map { emotionMap[it] },
                            observations = step3Selected.map { observationMap[it] },
                            sleepQuality = if (sleepSelected >= 0) qualityMap[sleepSelected] else "normal",
                            appetiteQuality = if (appetiteSelected >= 0) qualityMap[appetiteSelected] else "normal",
                            note = noteText.trim()
                        )
                        val result = runCatching { ApiClient.api.createMoodTracking(request) }
                        isLoading = false
                        if (result.isSuccess) {
                            SurveyState.todayMoodScore = when (step1Selected) {
                                0 -> 5   // good
                                1 -> 3   // normal
                                2 -> 2   // hard
                                else -> 1 // very_hard
                            }
                            SurveyState.completedToday = true
                            navController.popBackStack()
                        } else {
                            errorText = "Не удалось отправить опрос"
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    if (currentStep < 4) "Далее" else "Завершить",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SurveyTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(44.dp)
    ) {
        Text(
            text = "<",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF222222),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable { onBack() }
                .padding(horizontal = 4.dp)
        )
        Text(
            text = "Ежедневный опрос",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun StepProgressBar(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (index <= currentStep) Blue else GrayBg)
            )
        }
    }
}

@Composable
private fun StepMood(
    options: List<SurveyOption>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Text(
        "Как ребенок чувствовал\nсебя сегодня?",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 30.sp
    )
    Spacer(modifier = Modifier.height(28.dp))
    options.forEachIndexed { index, option ->
        SingleOptionRow(
            emoji = option.emoji,
            label = option.label,
            isSelected = index == selectedIndex,
            onClick = { onSelect(index) }
        )
        if (index < options.lastIndex) Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun StepMultiSelect(
    title: String,
    options: List<SurveyOption>,
    selected: MutableList<Int>
) {
    Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp)
    Spacer(modifier = Modifier.height(4.dp))
    Text("мультивыбор", fontSize = 14.sp, color = Color(0xFF999999))
    Spacer(modifier = Modifier.height(24.dp))
    options.forEachIndexed { index, option ->
        val isSelected = selected.contains(index)
        SingleOptionRow(
            emoji = option.emoji,
            label = option.label,
            isSelected = isSelected,
            onClick = {
                if (isSelected) selected.remove(index) else selected.add(index)
            }
        )
        if (index < options.lastIndex) Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun SingleOptionRow(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) LightBlue else Color.White
    val borderColor = if (isSelected) Blue else BorderGray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Blue,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun StepFactors(
    sleepOptions: List<String>,
    sleepSelected: Int,
    onSleepSelect: (Int) -> Unit,
    appetiteOptions: List<String>,
    appetiteSelected: Int,
    onAppetiteSelect: (Int) -> Unit
) {
    Text(
        "Что могло повлиять на\nсамочувствие?",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 30.sp
    )
    Spacer(modifier = Modifier.height(28.dp))

    Text("Сон", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(10.dp))
    FactorGroup(sleepOptions, sleepSelected, onSleepSelect)

    Spacer(modifier = Modifier.height(24.dp))

    Text("Апетит", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(10.dp))
    FactorGroup(appetiteOptions, appetiteSelected, onAppetiteSelect)
}

@Composable
private fun FactorGroup(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, BorderGray, RoundedCornerShape(14.dp))
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            val bgColor = if (isSelected) LightBlue else Color.White

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    option,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Blue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (index < options.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderGray)
                )
            }
        }
    }
}

@Composable
private fun StepNote(noteText: String, onTextChange: (String) -> Unit) {
    Text(
        "Хотите добавить\nзаметку?",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 30.sp
    )
    Spacer(modifier = Modifier.height(24.dp))
    Text("Заметка", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF555555))
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = noteText,
        onValueChange = onTextChange,
        placeholder = { Text("Раскажите нам", color = Color(0xFFBBBBBB)) },
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Blue,
            unfocusedBorderColor = BorderGray,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}
