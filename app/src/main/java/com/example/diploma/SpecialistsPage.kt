package com.example.diploma

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun SpecialistsPage(navController: NavController) {

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            BottomBar(navController, selectedIndex = 3)
        }
    ) { innerPadding ->

        val courses = List(6) { "Полный курс моторики" }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            item { Spacer(modifier = Modifier.height(45.dp)) }

            // ---------- Верхняя панель ----------
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.icon_search),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Image(
                            painter = painterResource(id = R.drawable.icon_heart),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Box {
                            Image(
                                painter = painterResource(id = R.drawable.icon_bag),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        Color(0xFF006FFD),
                                        RoundedCornerShape(50)
                                    )
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("9", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // ---------- Переключатель ----------
            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .background(Color(0xFFF1F3F6), RoundedCornerShape(50))
                        .fillMaxWidth()
                        .height(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.White, RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Курсы", fontWeight = FontWeight.SemiBold)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Специалисты", color = Color.Gray)
                    }
                }
            }

            // ---------- Баннер ----------
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFFDCE6F7)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Баннер", color = Color.Gray)
                }
            }

            // ---------- Лучшее ----------
            item {
                SectionTitle("Лучшее для тебя")
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(courses) {
                        CourseCard()
                    }
                }
            }

            // ---------- Популярные ----------
            item {
                SectionTitle("Популярные")
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(courses) {
                        CourseCard()
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}



@Composable
fun CourseCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F6)),
        modifier = Modifier
            .width(180.dp)
            .height(220.dp)
    ) {
        Column {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFDCE6F7))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    text = "Полный курс моторики",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Алина Киримбаева",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "12.000 ₸",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
@Composable
fun SectionTitle(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text("еще", color = Color(0xFF006FFD))
    }
}
@Preview(showBackground = true)
@Composable
fun SpecialistsPagePreview() {
    SpecialistsPage(navController = rememberNavController())
}