package kz.aruzhan.care_steps

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
fun AccountCreateFifthPage(navController: NavController, childVm: ChildViewModel) {
    val scrollState = rememberScrollState()

    var speechSelected by remember { mutableStateOf("Не говорит") }
    var instructionSelected by remember { mutableStateOf("Только жесты / картинки") }
    val speechOptions = listOf(
        "Не говорит",
        "Говорит отдельные слова",
        "Говорит фразами",
        "Говорит свободно"
    )

    val instructionOptions = listOf(
        "Только жесты / картинки",
        "Короткие инструкции",
        "Понимает объяснения"
    )

    // ✅ 100% белый фон на телефоне
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // ✅ СКРОЛЛЯЩИЙСЯ КОНТЕНТ (размеры и отступы твои)
            Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)   // только по бокам
                        .padding(bottom = 140.dp)
                        .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StepProgressBar(currentStep = 5)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Коммуникация и\nпонимание",
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .width(327.dp)
                        .height(60.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 1 блок
                Text(
                    text = "Как ребёнок общается?",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                SelectCard(
                    width = 327.dp,
                    options = speechOptions,
                    selected = speechSelected,
                    onSelect = { speechSelected = it }
                )

                Spacer(modifier = Modifier.height(30.dp))

                // 2 блок
                Text(
                    text = "Как ребёнок понимает инструкции?",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                SelectCard(
                    width = 327.dp,
                    options = instructionOptions,
                    selected = instructionSelected,
                    onSelect = { instructionSelected = it }
                )

                Spacer(modifier = Modifier.height(28.dp))
                // ❗️КНОПКИ отсюда убрали вниз, чтобы были фиксированы
            }

            // ✅ КНОПКИ ФИКСИРОВАНЫ СНИЗУ (размеры твои)
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
                        println("🔥🔥🔥 КНОПКА НАЖАТА НА 5 ЭКРАНЕ!")
                        println("🔥 speechSelected = $speechSelected")
                        println("🔥 instructionSelected = $instructionSelected")

                        val apiComm = mapToApiCommunication(speechSelected)
                        val apiInstr = mapToApiInstruction(instructionSelected)

                        println("🔥 api communication = $apiComm")
                        println("🔥 api instruction = $apiInstr")

                        childVm.updateCommunication(
                            communication = apiComm,
                            instruction = apiInstr
                        )

                        println("✅ updateCommunication вызван, идём на 6 экран")
                        navController.navigate("AccountCreateSixthPage")
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
                        println("⬅️ Нажата кнопка НАЗАД на 5 экране")
                        val popped = navController.popBackStack()
                        println("⬅️ popBackStack result = $popped")
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


@Composable
private fun SelectCard(
    width: androidx.compose.ui.unit.Dp,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(width)
            .border(1.dp, Color(0xFFC5C6CC), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        options.forEachIndexed { index, item ->
            val isSelected = item == selected

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFFEAF1FF) else Color.Transparent)
                    .clickable { onSelect(item) }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
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
                            color = Color(0xFF006FFD)
                        )
                    )
                }
            }

            if (index != options.lastIndex) Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
fun mapToApiCommunication(uiValue: String): String {
    return when (uiValue) {
        "Не говорит" -> "no_speech"
        "Говорит отдельные слова" -> "single_words"
        "Говорит фразами" -> "phrases"
        "Говорит свободно" -> "fluent"
        else -> "no_speech"
    }
}

// ✅ Функция для преобразования UI значения в формат API (understands_instructions)
fun mapToApiInstruction(uiValue: String): String {
    return when (uiValue) {
        "Только жесты / картинки" -> "gestures_pictures"
        "Короткие инструкции" -> "short_instructions"
        "Понимает объяснения" -> "understands_explanations"
        else -> "gestures_pictures"
    }
}


