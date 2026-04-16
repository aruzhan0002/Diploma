package com.example.diploma

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.diploma.StepProgressBar


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountCreateEighthPage(navController: NavController, childVm: ChildViewModel) {
    val blue = Color(0xFF006FFD)
    val chipBg = Color(0xFFEFF4FF)


    val chips = listOf(
        "🚗 МАШИНЫ",
        "🍎 ЕДА",
        "🚀 КОСМОС",
        "🔤 БУКВЫ",
        "🔢 ЦИФРЫ",
        "🐻 ЖИВОТНЫЕ",
        "🎵 МУЗЫКА",
        "🎨 РИСОВАНИЕ",
        "🧩 ПАЗЛЫ",
        "⚽ СПОРТ",
        "📚 КНИГИ",
        "🎮 ИГРЫ",
        "🧠 ЛОГИКА",
        "🌈 ЦВЕТА",
        "🦕 ДИНОЗАВРЫ",
        "🧪 ОПЫТЫ"
    )

    var selected by remember { mutableStateOf(setOf("🚀 КОСМОС", "🔤 БУКВЫ")) }

    fun toggle(item: String) {
        selected = if (selected.contains(item)) selected - item else selected + item
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                // важно: снизу место под закреплённые кнопки
                .padding(bottom = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StepProgressBar(currentStep = 8)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Интересы",
                style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.width(327.dp)
                    .height(40.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Что нравится ребёнку?",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.width(327.dp)
                    .height(20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.width(327.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 3
            ) {
                chips.forEach { item ->
                    InterestChip(
                        text = item,
                        selected = selected.contains(item),
                        onClick = { toggle(item) },
                        blue = blue,
                        chipBg = chipBg
                    )
                }
            }
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
                    Log.d(TAG, "🔥🔥🔥 КНОПКА ДАЛЕЕ НАЖАТА НА 8 ЭКРАНЕ!")
                    Log.d(TAG, "🔥 selected interests = $selected")

                    val apiInterests = selected.map { mapToApiInterestItem(it) }

                    Log.d(TAG, "🔥 api interests = $apiInterests")

                    childVm.updateInterests(
                        interests = apiInterests
                    )

                    Log.d(TAG, "✅ updateInterests вызван, идём на 9 экран")

                    navController.navigate("AccountCreateNinthPage")
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
                    Log.d(TAG, "⬅️ Нажата кнопка НАЗАД на 8 экране")
                    val popped = navController.popBackStack()
                    Log.d(TAG, "⬅️ popBackStack result = $popped")
                },
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

fun mapToApiInterestItem(uiValue: String): String {
    return when (uiValue) {
        "🚗 МАШИНЫ" -> "cars"
        "🍎 ЕДА" -> "food"
        "🚀 КОСМОС" -> "space"
        "🔤 БУКВЫ" -> "letters"
        "🔢 ЦИФРЫ" -> "numbers"
        "🐻 ЖИВОТНЫЕ" -> "animals"
        "🎵 МУЗЫКА" -> "music"
        "🎨 РИСОВАНИЕ" -> "drawing"
        "🧩 ПАЗЛЫ" -> "puzzles"
        "⚽ СПОРТ" -> "sport"
        "📚 КНИГИ" -> "books"
        "🎮 ИГРЫ" -> "games"
        "🧠 ЛОГИКА" -> "logic"
        "🌈 ЦВЕТА" -> "colors"
        "🦕 ДИНОЗАВРЫ" -> "dinosaurs"
        "🧪 ОПЫТЫ" -> "experiments"
        else -> uiValue.lowercase().replace(" ", "_").replace("🚗", "").replace("🍎", "").replace("🚀", "").replace("🔤", "").replace("🔢", "").replace("🐻", "").replace("🎵", "").replace("🎨", "").replace("🧩", "").replace("⚽", "").replace("📚", "").replace("🎮", "").replace("🧠", "").replace("🌈", "").replace("🦕", "").replace("🧪", "").trim()
    }
}

@Composable
fun InterestChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    blue: Color,
    chipBg: Color
) {
    val bg = if (selected) blue else chipBg
    val fg = if (selected) Color.White else blue

    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp), // ✅ ширина сама растёт от текста
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg
        )
    }
}


