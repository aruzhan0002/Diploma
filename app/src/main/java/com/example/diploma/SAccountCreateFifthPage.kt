package kz.aruzhan.care_steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kz.aruzhan.care_steps.ui.auth.SpecialistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SAccountCreateFifthPage(
    navController: NavController,
    specialistVm: SpecialistViewModel
) {
    val blue = Color(0xFF006FFD)
    val borderColor = Color(0xFFC5C6CC)
    val selectedBg = Color(0xFFEAF2FF)

    val devOptions = listOf(
        "Аутизм (РАС)",
        "Синдром Дауна",
        "ЗПР",
        "Смешанные случаи"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                StepProgressBar1(currentStep = 5)

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "С какими детьми вы\nработаете?",
                    style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Возраст",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(327.dp)
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = specialistVm.ageRange,
                    onValueChange = { specialistVm.ageRange = it },
                    modifier = Modifier
                        .width(327.dp)
                        .height(52.dp),
                    placeholder = { Text("2-4, Подростки") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = borderColor,
                        unfocusedBorderColor = borderColor,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Тип развития",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(327.dp)
                )

                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .width(327.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {

                    devOptions.forEach { item ->
                        val selected = specialistVm.developmentType == item

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) selectedBg else Color.White)
                                .clickable { specialistVm.developmentType = item }
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )

                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = blue
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = specialistVm.developmentTypeOther,
                        onValueChange = {
                            specialistVm.developmentTypeOther = it
                            specialistVm.developmentType = "Другое"
                        },
                        placeholder = { Text("Другое") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = borderColor,
                            unfocusedBorderColor = borderColor,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Формат работы",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(327.dp)
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .width(327.dp)
                        .height(56.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SegButton("Офлайн", specialistVm.workFormat == "Офлайн") {
                        specialistVm.workFormat = "Офлайн"
                    }
                    SegButton("Онлайн", specialistVm.workFormat == "Онлайн") {
                        specialistVm.workFormat = "Онлайн"
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { navController.navigate("SAccountCreateSixthPage") },
                modifier = Modifier
                    .width(327.dp)
                    .height(45.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blue)
            ) {
                Text("Далее", color = Color.White)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .width(327.dp)
                    .height(45.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, blue)
            ) {
                Text("Назад", color = blue)
            }
        }
    }
}

@Composable
private fun SegButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFFF6F8FF) else Color.White
    val fg = if (selected) Color.Black else Color(0xFF8D8D8D)

    Box(
        modifier = Modifier
            .width(141.5.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
            color = fg
        )
    }
}
