package com.example.diploma

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
fun AccountCreateSeventhPage(navController: NavController, childVm: ChildViewModel) {


    val noticeOptions = listOf(
        "Быстро устает",
        "Расстраивается при ошибке",
        "Боится нового",
        "Любит повторения"
    )
    var noticeSelected by remember { mutableStateOf(setOf("Быстро устает", "Боится нового")) }


    val joyOptions = listOf(
        "⭐ звёзды",
        "👏 аплодисменты",
        "🧸 наклейки",
        "📺 мультик после занятия"
    )
    var joySelected by remember { mutableStateOf(setOf("⭐ звёзды", "🧸 наклейки", "📺 мультик после занятия")) }

    val cardBorder = Color(0xFFC5C6CC)
    val selectedBg = Color(0xFFF6F8FF)
    val blue = Color(0xFF006FFD)

    fun toggle(set: Set<String>, item: String): Set<String> {
        return if (set.contains(item)) set - item else set + item
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,

            contentPadding = PaddingValues(top = 0.dp, bottom = 140.dp)
        ) {

            item {
                StepProgressBar(currentStep = 7)

                Spacer(modifier = Modifier.height(34.dp))

                Text(
                    text = "Поведение и мотивация",
                    style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(modifier = Modifier.height(34.dp))

                // Блок 1
                Text(
                    text = "Что вы замечаете чаще?\n(мультивыбор)",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                MultiSelectCard(
                    options = noticeOptions,
                    selected = noticeSelected,
                    onToggle = { item -> noticeSelected = toggle(noticeSelected, item) },
                    width = 327.dp,
                    borderColor = cardBorder,
                    selectedBg = selectedBg,
                    checkColor = blue
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Блок 2
                Text(
                    text = "Что радует ребёнка? (мультивыбор)",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                MultiSelectCard(
                    options = joyOptions,
                    selected = joySelected,
                    onToggle = { item -> joySelected = toggle(joySelected, item) },
                    width = 327.dp,
                    borderColor = cardBorder,
                    selectedBg = selectedBg,
                    checkColor = blue
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // ---------- STICKY BUTTONS ----------
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
                    Log.d(TAG, "🔥🔥🔥 КНОПКА ДАЛЕЕ НАЖАТА НА 7 ЭКРАНЕ!")
                    Log.d(TAG, "🔥 noticeSelected = $noticeSelected")
                    Log.d(TAG, "🔥 joySelected = $joySelected")

                    val apiBehavior = noticeSelected.map { mapToApiBehaviorItem(it) }
                    val apiMotivators = joySelected.map { mapToApiMotivatorItem(it) }

                    Log.d(TAG, "🔥 api behavior = $apiBehavior")
                    Log.d(TAG, "🔥 api motivators = $apiMotivators")

                    // ✅ Сохраняем поведение и мотивацию в ViewModel
                    childVm.updateBehaviorAndMotivation(
                        behavior = apiBehavior,
                        motivators = apiMotivators
                    )

                    Log.d(TAG, "✅ updateBehaviorAndMotivation вызван, идём на 8 экран")

                    // ✅ Переходим на следующий экран
                    navController.navigate("AccountCreateEighthPage")
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
                    Log.d(TAG, "⬅️ Нажата кнопка НАЗАД на 7 экране")
                    val popped = navController.popBackStack()
                    Log.d(TAG, "⬅️ popBackStack result = $popped")
                },
                modifier = Modifier
                    .width(327.dp)
                    .height(45.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, blue)
            ) {
                Text("Назад", color = blue)
            }
        }
    }
}
fun mapToApiBehaviorItem(uiValue: String): String {
    return when (uiValue) {
        "Быстро устает" -> "tires_quickly"
        "Расстраивается при ошибке" -> "upset_by_mistake"
        "Боится нового" -> "fears_new"
        "Любит повторения" -> "loves_repetition"
        else -> uiValue.lowercase().replace(" ", "_")
    }
}

// ✅ Функции для преобразования UI значений в формат API (motivators)
fun mapToApiMotivatorItem(uiValue: String): String {
    return when (uiValue) {
        "⭐ звёзды" -> "stars"
        "👏 аплодисменты" -> "applause"
        "🧸 наклейки" -> "stickers"
        "📺 мультик после занятия" -> "cartoon"
        else -> uiValue.lowercase().replace(" ", "_")
    }
}

@Composable
private fun MultiSelectCard(
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    width: androidx.compose.ui.unit.Dp,
    borderColor: Color,
    selectedBg: Color,
    checkColor: Color
) {
    Column(
        modifier = Modifier
            .width(width)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        options.forEachIndexed { index, item ->
            SelectRow(
                text = item,
                checked = selected.contains(item),
                onClick = { onToggle(item) },
                selectedBg = selectedBg,
                checkColor = checkColor
            )

            if (index != options.lastIndex) {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun SelectRow(
    text: String,
    checked: Boolean,
    onClick: () -> Unit,
    selectedBg: Color,
    checkColor: Color
) {
    val bg = if (checked) selectedBg else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = text,
            style = TextStyle(
                fontSize = 15.sp,
                color = if (checked) Color.Black else Color(0xFF7A7A7A)
            ),
            modifier = Modifier.weight(1f)
        )

        if (checked) {
            Text(text = "✓", color = checkColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}


