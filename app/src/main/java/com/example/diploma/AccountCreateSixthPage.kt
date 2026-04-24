package kz.aruzhan.care_steps

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kz.aruzhan.care_steps.ui.child.ChildViewModel

@Composable
fun AccountCreateSixthPage(navController: NavController, childVm: ChildViewModel) {
    var selectedSensory by remember {
        mutableStateOf(setOf("🎧 громкие звуки", "✨ анимации", "📳 вибрации"))
    }

    var selectedMotor by remember {
        mutableStateOf(setOf("нажимать маленькие кнопки", "делать drag & drop"))
    }

    // ✅ Подключаем ChildViewModel
    val blue = Color(0xFF006FFD)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                item {
                    StepProgressBar(currentStep = 6)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Сенсорные особенности\nи моторика",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text(
                        text = "Есть ли чувствительность к...\n(мультивыбор)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .height(45.dp)
                            .width(327.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    MultiSelectBlock(
                        items = listOf(
                            "🎧 громкие звуки",
                            "💡 яркий свет",
                            "✨ анимации",
                            "📳 вибрации",
                            "👥 персонажи / лица"
                        ),
                        selectedItems = selectedSensory,
                        onToggle = { item ->
                            selectedSensory =
                                if (selectedSensory.contains(item)) selectedSensory - item else selectedSensory + item
                        }
                    )
                    Spacer(modifier = Modifier.padding(14.dp))
                }

                item {
                    Text(
                        text = "Ребёнку сложно... (мультивыбор)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .height(25.dp)
                            .width(327.dp)
                    )

                    MultiSelectBlock(
                        items = listOf(
                            "нажимать маленькие кнопки",
                            "удерживать палец",
                            "делать drag & drop"
                        ),
                        selectedItems = selectedMotor,
                        onToggle = { item ->
                            selectedMotor =
                                if (selectedMotor.contains(item)) selectedMotor - item else selectedMotor + item
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        // 🔥 ЛОГИРОВАНИЕ ДЛЯ ОТЛАДКИ
                        Log.d("SixthPage", "🔥🔥🔥 КНОПКА ДАЛЕЕ НАЖАТА НА 6 ЭКРАНЕ!")
                        Log.d("SixthPage", "🔥 selectedSensory = $selectedSensory")
                        Log.d("SixthPage", "🔥 selectedMotor = $selectedMotor")

                        val apiSensory = selectedSensory.map { mapToApiSensoryItem(it) }
                        val apiMotor = selectedMotor.map { mapToApiMotorItem(it) }

                        Log.d("SixthPage", "🔥 api sensory = $apiSensory")
                        Log.d("SixthPage", "🔥 api motor = $apiMotor")

                        // ✅ Сохраняем сенсорные особенности и моторику в ViewModel
                        childVm.updateSensoryAndMotor(
                            sensory = apiSensory,
                            motor = apiMotor
                        )

                        Log.d("SixthPage", "✅ updateSensoryAndMotor вызван, идём на 7 экран")

                        // ✅ Переходим на следующий экран
                        navController.navigate("AccountCreateSeventhPage")
                    },
                    modifier = Modifier
                        .width(327.dp)
                        .height(45.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blue)
                ) {
                    Text("Далее", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        Log.d("SixthPage", "⬅️ Нажата кнопка НАЗАД на 6 экране")
                        val popped = navController.popBackStack()
                        Log.d("SixthPage", "⬅️ popBackStack result = $popped")
                    },
                    modifier = Modifier
                        .width(327.dp)
                        .height(45.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, blue),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Text("Назад", color = blue)
                }
            }
        }
    }
}

// ✅ Функции для преобразования UI значений в формат API (sensory_sensitivities)
fun mapToApiSensoryItem(uiValue: String): String {
    return when (uiValue) {
        "🎧 громкие звуки" -> "loud_sounds"
        "💡 яркий свет" -> "bright_lights"
        "✨ анимации" -> "animations"
        "📳 вибрации" -> "vibrations"
        "👥 персонажи / лица" -> "faces_characters"
        else -> uiValue.lowercase().replace(" ", "_")
    }
}

// ✅ Функции для преобразования UI значений в формат API (motor_difficulties)
fun mapToApiMotorItem(uiValue: String): String {
    return when (uiValue) {
        "нажимать маленькие кнопки" -> "small_buttons"
        "удерживать палец" -> "hold_finger"
        "делать drag & drop" -> "drag_drop"
        else -> uiValue.lowercase().replace(" ", "_")
    }
}

@Composable
fun MultiSelectBlock(
    items: List<String>,
    selectedItems: Set<String>,
    onToggle: (String) -> Unit
) {
    val borderColor = Color(0xFFC5C6CC)
    val selectedBg = Color(0xFFEAF2FF)
    val blue = Color(0xFF006FFD)

    Column(
        modifier = Modifier
            .width(327.dp)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedItems.contains(item)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) selectedBg else Color.White)
                    .clickable { onToggle(item) }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = item,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    color = Color.Black
                )

                if (isSelected) {
                    Text(
                        text = "✓",
                        color = blue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (index != items.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
