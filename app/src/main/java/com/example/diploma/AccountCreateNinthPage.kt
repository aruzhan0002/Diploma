package kz.aruzhan.care_steps

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kz.aruzhan.care_steps.ui.child.ChildViewModel

@Composable
fun AccountCreateNinthPage(navController: NavController, childVm: ChildViewModel) {

    val blue = Color(0xFF006FFD)
    val borderColor = Color(0xFFC5C6CC)
    val selectedBg = Color(0xFFEAF1FF)
    val options = listOf("5 минут", "10 минут", "15 минут", "Свободно")
    var selected by remember { mutableStateOf("5 минут") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StepProgressBar(currentStep = 9)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Сколько времени\nкомфортно заниматься?",
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                SingleSelectCard(
                    width = 327.dp,
                    options = options,
                    selected = selected,
                    onSelect = { selected = it },
                    borderColor = borderColor,
                    selectedBg = selectedBg,
                    checkColor = blue
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Вы всегда сможете изменить это позже*",
                    style = TextStyle(fontSize = 14.sp, color = Color(0xFF8D8D8D)),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        // 🔥 ЛОГИРОВАНИЕ ДЛЯ ОТЛАДКИ
                        Log.d(TAG, "🔥🔥🔥 КНОПКА ПЕРЕЙТИ В ПРИЛОЖЕНИЕ НАЖАТА НА 9 ЭКРАНЕ!")
                        Log.d(TAG, "🔥 selected duration = $selected")

                        // ✅ ШАГ 1: Сохраняем длительность
                        val apiDuration = mapToApiDuration(selected)
                        Log.d(TAG, "🔥 api duration = $apiDuration")

                        childVm.updateDuration(apiDuration)

                        Log.d(TAG, "✅ updateDuration вызван")

                        // ✅ ШАГ 2: Отправляем ВСЕ данные на сервер
                        Log.d(TAG, "📤 Отправка профиля на сервер...")

                        childVm.submitChildProfile(
                            onSuccess = {
                                Log.d(TAG, "✅✅✅ ПРОФИЛЬ УСПЕШНО ОТПРАВЛЕН!")
                                // ✅ ШАГ 3: При успехе - переходим в приложение
                                navController.navigate("SettingsPage") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onError = { error ->
                                Log.e(TAG, "❌ ОШИБКА СОХРАНЕНИЯ ПРОФИЛЯ РЕБЕНКА: $error")
                                println("ОШИБКА СОХРАНЕНИЯ ПРОФИЛЯ РЕБЕНКА: $error")
                                // Можно показать Toast или Snackbar
                            }
                        )
                    },
                    modifier = Modifier
                        .width(327.dp)
                        .height(45.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blue)
                ) {
                    Text("Перейти в приложение", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        Log.d(TAG, "⬅️ Нажата кнопка НАЗАД на 9 экране")
                        val popped = navController.popBackStack()
                        Log.d(TAG, "⬅️ popBackStack result = $popped")
                    },
                    modifier = Modifier
                        .width(327.dp)
                        .height(45.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, blue),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Text("Назад", color = blue)
                }
            }
        }
    }
}
fun mapToApiDuration(uiDuration: String): String {
    return when (uiDuration) {
        "5 минут" -> "5_min"
        "10 минут" -> "10_min"
        "15 минут" -> "15_min"
        "Свободно" -> "unlimited"
        else -> "5_min"
    }
}

@Composable
private fun SingleSelectCard(
    width: androidx.compose.ui.unit.Dp,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    borderColor: Color,
    selectedBg: Color,
    checkColor: Color
) {
    Column(
        modifier = Modifier
            .width(width)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        options.forEachIndexed { index, item ->
            val isSelected = item == selected

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) selectedBg else Color.Transparent)
                    .clickable { onSelect(item) }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.Black else Color(0xFF8D8D8D)
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (isSelected) {
                    Text(
                        text = "✓",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = checkColor
                        )
                    )
                }
            }

            if (index != options.lastIndex) {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

