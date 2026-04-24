package com.example.diploma

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun CourseFilterPage(navController: NavController, filters: CourseFilters) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Отмена",
                color = Color(0xFF006FFD),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
            Text("Фильтр", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "Очистить все",
                color = Color(0xFF006FFD),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { filters.clear() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
        ) {
            item { RatingSection(filters) }
            item { Divider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 8.dp)) }

            item { PriceSection(filters) }
            item { Divider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 8.dp)) }

            item { LevelSection(filters) }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF006FFD))
                .clickable {
                    navController.navigate("SearchResultsPage/all") {
                        popUpTo("CourseFilterPage") { inclusive = true }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("Применить изменения", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SectionHeader(title: String, count: Int = 0) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        if (count > 0) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF006FFD), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("$count", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text("⌄", fontSize = 18.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun FilterCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF006FFD),
                uncheckedColor = Color(0xFFD0D5DD)
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 14.sp)
    }
}

@Composable
private fun RatingSection(filters: CourseFilters) {
    SectionHeader("Рейтинг", if (filters.selectedRatings.isNotEmpty()) 1 else 0)

    val options = listOf("3", "4", "4.5")
    options.forEach { option ->
        FilterCheckbox(
            label = "$option+",
            checked = option in filters.selectedRatings,
            onCheckedChange = { checked ->
                if (checked) filters.selectedRatings.add(option)
                else filters.selectedRatings.remove(option)
            }
        )
    }
}

@Composable
private fun PriceSection(filters: CourseFilters) {
    SectionHeader("Цена")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${filters.priceMin.toInt()} ₸", fontSize = 14.sp, color = Color.Gray)
        Text("${filters.priceMax.toInt()} ₸", fontSize = 14.sp, color = Color.Gray)
    }

    RangeSlider(
        value = filters.priceMin..filters.priceMax,
        onValueChange = { range ->
            filters.priceMin = range.start
            filters.priceMax = range.endInclusive
        },
        valueRange = 5000f..32990f,
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFF006FFD),
            activeTrackColor = Color(0xFF006FFD),
            inactiveTrackColor = Color(0xFFE0E0E0)
        )
    )
}

@Composable
private fun LevelSection(filters: CourseFilters) {
    SectionHeader("Уровень", if (filters.selectedLevels.isNotEmpty()) 1 else 0)

    val options = listOf(
        "Начальный (без навыков)" to "beginner",
        "Базовый" to "intermediate",
        "Продвинутый" to "advanced"
    )
    options.forEach { (label, value) ->
        FilterCheckbox(
            label = label,
            checked = value in filters.selectedLevels,
            onCheckedChange = { checked ->
                if (checked) filters.selectedLevels.add(value)
                else filters.selectedLevels.remove(value)
            }
        )
    }
}
