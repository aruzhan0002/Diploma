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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun SettingsPage(navController: NavController) {

    Scaffold(
        bottomBar = {
            BottomBar(navController, selectedIndex = 4)
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

            // -------- Аватар --------

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
                    color = Color(0xFF8D8D8D)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // -------- Меню --------

            SettingsRow("Профиль")
            Divider(color = Color(0xFFEDEDED))

            SettingsRow("Ребенок")
            Divider(color = Color(0xFFEDEDED))

            SettingsRow("Уведомления")
            Divider(color = Color(0xFFEDEDED))

            SettingsRow("Счет")
            Divider(color = Color(0xFFEDEDED))

            SettingsRow("Безопасность")
            Divider(color = Color(0xFFEDEDED))

            SettingsRow("Язык")
            Divider(color = Color(0xFFEDEDED))

            SettingsRow("Память")
            Divider(color = Color(0xFFEDEDED))
        }
    }
}
// ----------------------
// СТРОКА НАСТРОЕК
// ----------------------

@Composable
private fun SettingsRow(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "›",
            fontSize = 22.sp,
            color = Color(0xFFB0B0B0)
        )
    }
}

// ----------------------
// НИЖНИЙ БАР
// ----------------------

@Composable
fun BottomBar(
    navController: NavController,
    selectedIndex: Int
) {

    val items = listOf(
        Triple(R.drawable.icon1, "Ребенок", "ChildPage"),
        Triple(R.drawable.icon3, "Чат", "ChatPage"),
        Triple(R.drawable.icon6, "Главная", "HomePage"),
        Triple(R.drawable.icon4, "Специалисты", "SpecialistsPage"),
        Triple(R.drawable.icon5, "Настройки", "SettingsPage")
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
            val color = if (isSelected) Color(0xFF006FFD) else Color(0xFFBDBDBD)

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
                    modifier = Modifier.size(20.dp),
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
@Composable
private fun BottomItem(
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    Image(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        modifier = Modifier
            .size(20.dp)
            .clickable { onClick() }
    )
}

// ----------------------
// PREVIEW
// ----------------------

@Preview(showBackground = true)
@Composable
fun PreviewSettingsPage() {
    SettingsPage(navController = rememberNavController())
}