package com.example.diploma

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun ChildPage(navController: NavController) {

    Scaffold(
        bottomBar = {
            BottomBar(navController, selectedIndex = 0)
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            item { Spacer(modifier = Modifier.height(10.dp)) }

            // -------- Приветствие --------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {
                        Text(
                            text = "Привет, Павел 👋",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "⭐ ⭐ ⭐ ⭐",
                            fontSize = 14.sp
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.icon5),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                navController.navigate("ChildModePage")
                            }
                    )
                }
            }

            item {
                Text(
                    text = "Давай продолжим занятие",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF1F3F6)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(93.dp)
                        .clickable { }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

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
                }
            }


            item {
                Text(
                    text = "Модули для тебя",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                val modules = List(6) { "Модуль" }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(320.dp)
                ) {
                    items(modules) { title ->
                        ModuleCard(title, navController)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun ModuleCard(title: String, navController: NavController
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F6)),
        modifier = Modifier
            .fillMaxWidth()
            .height(99.dp)
            .clickable {
                navController.navigate("ChildModulePage")
            }

    ) {
        Column(
            modifier = Modifier
                .width(343.dp)
                .padding(16.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFDCE6F7), shape = RoundedCornerShape(50))
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ChildPage() {
    ChildPage(navController = rememberNavController())
}