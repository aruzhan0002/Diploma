package com.example.diploma

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.diploma.ui.auth.SpecialistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SAccountCreateSixthPage(
    navController: NavController,
    specialistVm: SpecialistViewModel
) {
    val blue = Color(0xFF006FFD)
    val borderColor = Color(0xFFC5C6CC)
    val selectedBg = Color(0xFFEAF2FF)

    val languageOptions = listOf("Русский", "Казахский", "Английский")

    fun toggleLang(item: String) {
        specialistVm.selectedLanguages =
            if (specialistVm.selectedLanguages.contains(item))
                specialistVm.selectedLanguages - item
            else
                specialistVm.selectedLanguages + item
    }

    val timezones = listOf(
        "UTC/GMT +5 часов",
        "UTC/GMT +6 часов",
        "UTC/GMT +3 часов"
    )
    var tzExpanded by remember { mutableStateOf(false) }

    val cities = listOf("Алматы", "Астана", "Шымкент", "Караганда")
    var cityExpanded by remember { mutableStateOf(false) }

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
                StepProgressBar1(currentStep = 6)

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Язык работы и регион",
                    style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Языки (мультивыбор)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(327.dp)
                )

                Spacer(Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .width(327.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    languageOptions.forEachIndexed { index, item ->
                        val checked = specialistVm.selectedLanguages.contains(item)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (checked) selectedBg else Color.White)
                                .clickable { toggleLang(item) }
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item,
                                fontSize = 16.sp,
                                color = if (checked) Color.Black else Color(0xFF8D8D8D),
                                modifier = Modifier.weight(1f)
                            )
                            if (checked) {
                                Text(
                                    text = "✓",
                                    color = blue,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (index != languageOptions.lastIndex) {
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Часовой пояс",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(327.dp)
                )

                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = tzExpanded,
                    onExpandedChange = { tzExpanded = !tzExpanded },
                    modifier = Modifier.width(327.dp)
                ) {
                    OutlinedTextField(
                        value = specialistVm.timeZone,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tzExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = borderColor,
                            unfocusedBorderColor = borderColor,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = tzExpanded,
                        onDismissRequest = { tzExpanded = false }
                    ) {
                        timezones.forEach { tz ->
                            DropdownMenuItem(
                                text = { Text(tz) },
                                onClick = {
                                    specialistVm.timeZone = tz
                                    tzExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Город",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(327.dp)
                )

                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = cityExpanded,
                    onExpandedChange = { cityExpanded = !cityExpanded },
                    modifier = Modifier.width(327.dp)
                ) {
                    OutlinedTextField(
                        value = specialistVm.city,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = borderColor,
                            unfocusedBorderColor = borderColor,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = cityExpanded,
                        onDismissRequest = { cityExpanded = false }
                    ) {
                        cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    specialistVm.city = city
                                    cityExpanded = false
                                }
                            )
                        }
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
                onClick = { navController.navigate("SAccountCreateSeventhPage") },
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
