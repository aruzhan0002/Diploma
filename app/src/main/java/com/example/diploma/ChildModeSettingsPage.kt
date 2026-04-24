@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.diploma

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildModeSettingsPage(navController: NavController) {

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006FFD),
                    contentColor = Color.White
                )
            ) {
                Text("Применить изменения", fontSize = 15.sp)
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // Верхняя панель
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = null,
                        tint = Color(0xFF006FFD),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { navController.popBackStack() }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Настройки детского режима",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "Отмена",
                    color = Color(0xFF006FFD),
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            var pinExpanded by remember { mutableStateOf(false) }
            var interestsExpanded by remember { mutableStateOf(false) }
            var difficultyExpanded by remember { mutableStateOf(false) }
            var sensoryExpanded by remember { mutableStateOf(false) }
            var motorExpanded by remember { mutableStateOf(false) }
            var rewardsExpanded by remember { mutableStateOf(false) }

            SettingsSectionHeader(
                title = "PIN код",
                expanded = pinExpanded,
                onToggle = { pinExpanded = !pinExpanded }
            )
            if (pinExpanded) {
                PinSectionContent()
            }

            Divider(color = Color(0xFFE5E7EB))

            SettingsSectionHeader(
                title = "Интересы и темы",
                expanded = interestsExpanded,
                onToggle = { interestsExpanded = !interestsExpanded }
            )
            if (interestsExpanded) {
                InterestsSectionContent()
            }

            Divider(color = Color(0xFFE5E7EB))

            SettingsSectionHeader(
                title = "Уровень сложности",
                expanded = difficultyExpanded,
                onToggle = { difficultyExpanded = !difficultyExpanded }
            )
            if (difficultyExpanded) {
                DifficultySectionContent()
            }

            Divider(color = Color(0xFFE5E7EB))

            SettingsSectionHeader(
                title = "Сенсорные настройки",
                expanded = sensoryExpanded,
                onToggle = { sensoryExpanded = !sensoryExpanded }
            )
            if (sensoryExpanded) {
                SensorySectionContent()
            }

            Divider(color = Color(0xFFE5E7EB))

            SettingsSectionHeader(
                title = "Моторика и интерфейс",
                expanded = motorExpanded,
                onToggle = { motorExpanded = !motorExpanded }
            )
            if (motorExpanded) {
                MotorSectionContent()
            }

            Divider(color = Color(0xFFE5E7EB))

            SettingsSectionHeader(
                title = "Награды",
                expanded = rewardsExpanded,
                onToggle = { rewardsExpanded = !rewardsExpanded }
            )
            if (rewardsExpanded) {
                RewardsSectionContent()
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Icon(
            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = Color(0xFF9CA3AF)
        )
    }
}

@Composable
private fun PinSectionContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "Введите PIN код",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        var pin by remember { mutableStateOf("") }

        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            placeholder = {
                Text("Создайте PIN код", fontSize = 14.sp, color = Color.Gray)
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF006FFD),
                unfocusedBorderColor = Color(0xFFE5E7EB)
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Ваш пинкод будет использоваться для выхода из детского режима*",
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestsSectionContent() {
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

    var selected by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 3
        ) {
            chips.forEach { item ->
                InterestChip(
                    text = item,
                    selected = selected.contains(item),
                    onClick = {
                        selected = if (selected.contains(item)) {
                            selected - item
                        } else {
                            selected + item
                        }
                    },
                    blue = blue,
                    chipBg = chipBg
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DifficultySectionContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        val modules = listOf(
            "Коммуникация",
            "Моторика",
            "Социальное",
            "Обучение",
            "Творчество"
        )

        modules.forEachIndexed { index, name ->
            DifficultyRow(
                title = name,
                initialStars = if (index == 1 || index == 4) 3 else 2
            )
            if (index != modules.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DifficultyRow(
    title: String,
    initialStars: Int
) {
    var stars by remember { mutableStateOf(initialStars) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 14.sp)

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) { index ->
                val filled = index < stars
                Icon(
                    painter = painterResource(
                        id = if (filled) R.drawable.star_filled else R.drawable.star_outlined
                    ),
                    contentDescription = null,
                    tint = if (filled) Color(0xFFFBBF24) else Color(0xFFD1D5DB),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { stars = index + 1 }
                )
            }
        }
    }
}

@Composable
private fun SensorySectionContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var noSharpSounds by remember { mutableStateOf(true) }
        var noAnimations by remember { mutableStateOf(true) }
        var voiceHints by remember { mutableStateOf(true) }
        var calmColors by remember { mutableStateOf(true) }

        SettingCheckboxRow("Без резких звуков", noSharpSounds) { noSharpSounds = it }
        SettingCheckboxRow("Без анимаций", noAnimations) { noAnimations = it }
        SettingCheckboxRow("Включить голосовые подсказки", voiceHints) { voiceHints = it }
        SettingCheckboxRow("Спокойная цветовая схема", calmColors) { calmColors = it }
    }
}

@Composable
private fun MotorSectionContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var bigButtons by remember { mutableStateOf(true) }
        var simplifiedTasks by remember { mutableStateOf(true) }
        var noDrag by remember { mutableStateOf(true) }

        SettingCheckboxRow("Крупные кнопки", bigButtons) { bigButtons = it }
        SettingCheckboxRow("Упрощённые задания", simplifiedTasks) { simplifiedTasks = it }
        SettingCheckboxRow("Без перетаскивания", noDrag) { noDrag = it }
    }
}

@Composable
private fun RewardsSectionContent() {
    val blue = Color(0xFF006FFD)
    val chipBg = Color(0xFFEFF4FF)

    val rewards = listOf(
        "⭐ ЗВЁЗДЫ",
        "👏 АПЛОДИСМЕНТЫ",
        "🎁 ПОДАРОК",
        "🎉 АНИМАЦИЯ",
        "📺 МИНИ-ИГРА"
    )

    var selected by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 3
        ) {
            rewards.forEach { reward ->
                InterestChip(
                    text = reward,
                    selected = selected.contains(reward),
                    onClick = {
                        selected = if (selected.contains(reward)) {
                            selected - reward
                        } else {
                            selected + reward
                        }
                    },
                    blue = blue,
                    chipBg = chipBg
                )
            }
        }
    }
}

@Composable
private fun SettingCheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF006FFD)
            )
        )

        Text(
            text = text,
            fontSize = 14.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChildModeSettingsPagePreview() {
    ChildModeSettingsPage(navController = rememberNavController())
}

