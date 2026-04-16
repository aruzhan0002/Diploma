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
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SAccountCreateFourthPage(navController: NavController) {

    val blue = Color(0xFF006FFD)
    val borderColor = Color(0xFFC5C6CC)
    val selectedBg = Color(0xFFEAF2FF)
    val radius = RoundedCornerShape(16.dp)

    // Стаж
    var expYears by remember { mutableFloatStateOf(5f) } // по макету 5
    val expInt = expYears.roundToInt()

    // Методики (один выбор)
    val options = listOf(
        "ABA",
        "PECS",
        "DIR / Floortime",
        "Сенсорная интеграция"
    )
    var selectedMethod by remember { mutableStateOf("ABA") }
    var otherText by remember { mutableStateOf("") }

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
                    .padding(bottom = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                StepProgressBar1(currentStep = 4)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Опыт и подход",
                    style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Стаж работы + число рядом
                Row(
                    modifier = Modifier.width(327.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Стаж работы",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = expInt.toString(),
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))


                Slider(
                    value = expYears,
                    onValueChange = { expYears = it },
                    valueRange = 0f..25f,
                    modifier = Modifier.width(327.dp),
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFF006FFD),
                        inactiveTrackColor = Color(0xFFE8E9F1),
                        thumbColor = Color(0xFF006FFD)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Методики
                Text(
                    text = "Методики",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Карточка с вариантами + "Другое"
                Column(
                    modifier = Modifier
                        .width(327.dp)
                        .border(1.dp, borderColor, radius)
                        .clip(radius)
                        .background(Color.White)
                        .padding(12.dp)
                ) {

                    options.forEachIndexed { index, item ->
                        val isSelected = (item == selectedMethod)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) selectedBg else Color.White)
                                .clickable {
                                    selectedMethod = item
                                    // если выбрали не "Другое" — можно оставить otherText как есть
                                }
                                .padding(horizontal = 14.dp),
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
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = blue
                                )
                            }
                        }

                        if (index != options.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Другое — большое поле
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
                                    selectedMethod = "Другое"
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

                            if (selectedMethod == "Другое") {
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

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Опционально, но повышает доверие*",
                    style = TextStyle(fontSize = 12.sp, color = Color(0xFF8D8D8D)),
                    modifier = Modifier.width(327.dp)
                )
            }

            // КНОПКИ фикс снизу
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { navController.navigate("SAccountCreateFifthPage") }, // поменяй маршрут если у тебя другой
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
fun PreviewSAccountCreateFourthPage() {
    SAccountCreateFourthPage(navController = rememberNavController())
}
