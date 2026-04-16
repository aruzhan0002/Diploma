package com.example.diploma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun SAccountCreateSeventhPage(navController: NavController) {
    val blue = Color(0xFF006FFD)
    val border = Color(0xFFC5C6CC)

    var consult by remember { mutableStateOf(true) }
    var viaParent by remember { mutableStateOf(false) }
    var plans by remember { mutableStateOf(true) }
    var analytics by remember { mutableStateOf(false) }

    ScreenWithStickyButtons(
        navController = navController,
        currentStep = 7, // если у специалиста это последний шаг — ставь нужный
        title = "Как вы хотите работать\nв приложении?",
        nextText = "Перейти в приложение",
        onNext = {
            // сюда поставь переход в SettingsPage (или главный экран специалиста)
            navController.navigate("SettingsPageSpecialist")
        }
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Переключатели",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier
                    .width(327.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            SwitchCard(
                text = "Оказывать индивидуальные\nконсультации",
                checked = consult,
                onCheckedChange = { consult = it },
                width = 327.dp,
                borderColor = border,
                blue = blue
            )

            Spacer(modifier = Modifier.height(12.dp))

            SwitchCard(
                text = "Работать с ребёнком через\nродителя",
                checked = viaParent,
                onCheckedChange = { viaParent = it },
                width = 327.dp,
                borderColor = border,
                blue = blue
            )

            Spacer(modifier = Modifier.height(12.dp))

            SwitchCard(
                text = "Давать рекомендации и планы\nзанятий",
                checked = plans,
                onCheckedChange = { plans = it },
                width = 327.dp,
                borderColor = border,
                blue = blue
            )

            Spacer(modifier = Modifier.height(12.dp))

            SwitchCard(
                text = "Вести прогресс и аналитику",
                checked = analytics,
                onCheckedChange = { analytics = it },
                width = 327.dp,
                borderColor = border,
                blue = blue
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SwitchCard(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    width: androidx.compose.ui.unit.Dp,
    borderColor: Color,
    blue: Color
) {
    Row(
        modifier = Modifier
            .width(width)
            .heightIn(min = 64.dp)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = TextStyle(fontSize = 12.sp),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.7f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = blue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD8D8D8)
            )
        )

    }
}

/**
 * Универсальный шаблон как у 6/7/8:
 * - StepProgressBar + Title сверху
 * - контент скроллится
 * - кнопки всегда фиксированы внизу
 */
@Composable
fun ScreenWithStickyButtons(
    navController: NavController,
    currentStep: Int,
    title: String,
    nextText: String = "Далее",
    onNext: () -> Unit,
    onBack: () -> Unit = { navController.popBackStack() },
    content: LazyListScope.() -> Unit
) {
    val blue = Color(0xFF006FFD)

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
                StepProgressBar1(currentStep = currentStep)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = title,
                    style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))
            }

            content()
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .width(327.dp)
                    .height(45.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blue)
            ) {
                Text(nextText, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .width(327.dp)
                    .height(45.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, blue),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Text("Назад", color = blue)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSAccountCreateSeventhPage() {
    SAccountCreateSeventhPage(navController = rememberNavController())
}
