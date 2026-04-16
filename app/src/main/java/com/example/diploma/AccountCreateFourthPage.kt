package com.example.diploma
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.diploma.ui.child.ChildViewModel

@Composable
fun AccountCreateFourthPage(navController: NavController, childVm: ChildViewModel) {
    var selected by remember { mutableStateOf("Аутизм (РАС)") }


    val options = listOf(
        "🧩 Аутизм (РАС)",
        "🧠 Синдром Дауна",
        "🌱 ЗПР",
        "❓ Пока не знаем / не уверены"
    )
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)   // только по бокам
                    .padding(bottom = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StepProgressBar(currentStep = 4)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Какой тип развития у\nребёнка?",
                    style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .width(327.dp)
                        .height(58.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Блок выбора
                Column(
                    modifier = Modifier
                        .width(327.dp)
                        .border(1.dp, Color(0xFFC5C6CC), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    options.forEach { item ->
                        val cleanText = item.replace("🧩 ", "")
                            .replace("🧠 ", "")
                            .replace("🌱 ", "")
                            .replace("❓ ", "")

                        val isSelected = (cleanText == selected)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFFEAF1FF) else Color.Transparent)
                                .clickable { selected = cleanText }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item,
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color.Black else Color(0xFF6B6B6B)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF006FFD)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Вы всегда сможете изменить это позже*",
                    style = TextStyle(fontSize = 14.sp, color = Color(0xFF8D8D8D)),
                    modifier = Modifier
                        .width(327.dp)
                        .height(16.dp)
                )

            }
                // Кнопка Далее
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            Log.d("NAV", "✅ FourthPage: NEXT pressed, selected=$selected")
                            childVm.updateDevelopmentType(mapToApiDevelopmentType(selected))
                            navController.navigate("AccountCreateFifthPage")
                        },
                        modifier = Modifier
                            .width(327.dp)
                            .height(45.dp)
                            .border(5.dp, Color(0xFF006FFD), shape = RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006FFD))
                    ) {
                        Text(text = "Далее", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            Log.d("NAV", "⬅️ FourthPage: BACK pressed, current=${navController.currentDestination?.route}")
                            val popped = navController.popBackStack()
                            Log.d("NAV", "⬅️ FourthPage: popBackStack result=$popped, now=${navController.currentDestination?.route}")

                            // если вдруг в стеке реально нет 3 страницы
                            if (!popped) {
                                navController.navigate("AccountCreateThirdPage") {
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier
                            .width(327.dp)
                            .height(45.dp)
                            .border(1.dp, Color(0xFF006FFD), shape = RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(text = "Назад", color = Color(0xFF006FFD))
                    }
                }
            }
        }
    }
// Добавь эту функцию где-нибудь в файле
fun mapToApiDevelopmentType(uiType: String): String {
    return when (uiType) {
        "Аутизм (РАС)" -> "autism"
        "Синдром Дауна" -> "down_syndrome"
        "ЗПР" -> "zpr"
        "Пока не знаем / не уверены" -> "unknown"
        else -> "unknown"
    }
}



