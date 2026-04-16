package com.example.diploma

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SAccountCreateThirdPage(navController: NavController) {

    val blue = Color(0xFF006FFD)
    val borderColor = Color(0xFFC5C6CC)
    val selectedBg = Color(0xFFEAF2FF)
    val radius = RoundedCornerShape(16.dp)

    var selected by remember { mutableStateOf("ABA-терапия") }
    var otherText by remember { mutableStateOf("") }

    val options = listOf(
        "🧩 ABA-терапия",
        "🗣️ Логопед",
        "🧠 Нейропсихолог",
        "👐 Эрготерапия",
        "🎨 Арт-терапия",
        "🧸 Сенсорная терапия",
        "📚 Спецпедагог"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    // чтобы контент не залезал под кнопки
                    .padding(bottom = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                StepProgressBar1(currentStep = 3)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Ваша специализация",
                    style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Кем вы приходитесь ребенку?",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // список выбора
                Column(
                    modifier = Modifier
                        .width(327.dp)
                        .border(1.dp, borderColor, radius)
                        .clip(radius)
                        .background(Color.White)
                        .padding(12.dp)
                ) {

                    options.forEachIndexed { index, item ->
                        val clean = item
                            .replace("🧩 ", "")
                            .replace("🗣️ ", "")
                            .replace("🧠 ", "")
                            .replace("👐 ", "")
                            .replace("🎨 ", "")
                            .replace("🧸 ", "")
                            .replace("📚 ", "")

                        val isSelected = (clean == selected)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) selectedBg else Color.White)
                                .clickable { selected = clean }
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color.Black else Color(0xFF6B6B6B)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = blue
                                )
                            }
                        }

                        if (index != options.lastIndex) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Другое (большое поле)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        shape = radius,
                        color = Color.White,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Box(Modifier.fillMaxSize()) {

                            OutlinedTextField(
                                value = otherText,
                                onValueChange = {
                                    otherText = it
                                    selected = "Другое"
                                },
                                placeholder = { Text("Другое", color = Color(0xFF9AA0A6)) },
                                modifier = Modifier.fillMaxSize(),
                                maxLines = 5,
                                shape = radius,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )

                            if (selected == "Другое") {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = blue,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(14.dp)
                                )
                            }
                        }
                    }
                }
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

                        navController.navigate("SAccountCreateFourthPage") }, // поменяй маршрут как у тебя
                    modifier = Modifier
                        .width(327.dp)
                        .height(45.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blue)
                ) {
                    Text("Далее", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .width(327.dp)
                        .height(45.dp),
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

@Preview(showBackground = true)
@Composable
fun PreviewSAccountCreateThirdPage() {
    SAccountCreateThirdPage(navController = rememberNavController())
}
