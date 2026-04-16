package com.example.diploma

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SettingsPageSpecialist(navController: NavController) {

    val textGray = Color(0xFF8D8D8D)
    val divider = Color(0xFFEDEDED)

    Scaffold(
        bottomBar = {
            SpecialistBottomBar(
                navController = navController,
                selectedIndex = 4   // Настройки = 4-й элемент (index 3)
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 45.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Настройки",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Профиль
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Box(contentAlignment = Alignment.BottomEnd) {

                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEAF2FF))
                    )

                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF006FFD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✎", color = Color.White, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Вероника Петрова",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "@nica_petro",
                    fontSize = 12.sp,
                    color = textGray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsRow("Профиль") {
                navController.navigate("ProfilePage")
            }
            Divider(color = divider)

            SettingsRow("Мои клиенты") {
                navController.navigate("ClientsPage")
            }
            Divider(color = divider)

            SettingsRow("Уведомления") { }
            Divider(color = divider)

            SettingsRow("Счет") { }
            Divider(color = divider)

            SettingsRow("Безопасность") { }
            Divider(color = divider)

            SettingsRow("Язык") { }
            Divider(color = divider)

            SettingsRow("Память") { }
            Divider(color = divider)
        }
    }
}

@Composable
private fun SettingsRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text("›", fontSize = 22.sp, color = Color(0xFFB0B0B0))
    }
}

@Composable
fun SpecialistBottomBar(
    navController: NavController,
    selectedIndex: Int
) {

    val items = listOf(
        Triple(R.drawable.icon1, "Рейтинг", "RatingPage"),
        Triple(R.drawable.icon3, "Чат", "SpecialistChatPage"),
        Triple(R.drawable.icon6, "Главная", "HomePage"),
        Triple(R.drawable.icon_store, "Курсы", "SpecialistCoursesPage"),
        Triple(R.drawable.icon7, "Настройки", "SettingsPageSpecialist")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {

        items.forEachIndexed { index, item ->

            val isSelected = index == selectedIndex
            val color =
                if (isSelected) Color(0xFF006FFD)
                else Color(0xFFBDBDBD)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    navController.navigate(item.third) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            ) {

                Image(
                    painter = painterResource(id = item.first),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    colorFilter = ColorFilter.tint(color)
                )

                if (isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.second,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsPageSpecialist() {
    SettingsPageSpecialist(navController = rememberNavController())
}