package com.example.diploma

import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


@Composable
fun ParentHomePage(navController: NavController) {

    Scaffold(
        bottomBar = {
            BottomBar(navController, selectedIndex = 3)
        }
    ) { innerPadding ->

        val quickItems = listOf(
            "Прогресс",
            "Специалист",
            "Библиотека",
            "Профиль"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            item { Spacer(modifier = Modifier.height(45.dp)) }

            // ---------- Приветствие ----------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {
                        Text(
                            text = "Привет, Вероника 👋",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Павел • 6 лет",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.icon5),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                navController.navigate("SettingsPage")
                            }
                    )
                }
            }

            // ---------- Главное занятие ----------
            item { MainLessonCard() }

            // ---------- Быстрые действия ----------
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(240.dp)
                ) {
                    items(quickItems) { title ->
                        QuickActionCard(title, navController)
                    }
                }
            }

            // ---------- Последние занятия ----------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Последние занятия",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "еще",
                        fontSize = 14.sp,
                        color = Color(0xFF006FFD)
                    )
                }
            }

            items(2) {
                SmallLessonCard()
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
@Composable
fun SmallLessonCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F6)),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        // ЛЕВАЯ ЧАСТЬ (картинка + текст)
        Row(verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .width(90.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFDCE6F7))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Моторика",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Осталось 2 упражнения",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // ПРАВАЯ ЧАСТЬ (стрелка)
        Image(
            painter = painterResource(id = R.drawable.icon9),
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            colorFilter = ColorFilter.tint(Color(0xFF006FFD))
        )
    }
    }

}
@Composable
fun QuickActionCard(
    title: String,
    navController: NavController
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F6)),
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth()
            .clickable {
                if (title == "Прогресс") {
                    navController.navigate("ProgressPage")
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // 💙 КРУЖОК
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFFE6EBF5),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon8), // 💙 твоя иконка сердца
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(Color(0xFF006FFD))
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
@Composable
fun MainLessonCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F6)),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // ЛЕВАЯ ЧАСТЬ (картинка + текст)
            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFDCE6F7))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Моторика",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Осталось 2 упражнения",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // ПРАВАЯ ЧАСТЬ (стрелка)
            Image(
                painter = painterResource(id = R.drawable.icon9),
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                colorFilter = ColorFilter.tint(Color(0xFF006FFD))
            )
        }
    }

}
@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    ParentHomePage(navController = rememberNavController())
}