package com.example.diploma
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
fun ChildGamesPage(navController: NavController) {

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
                .padding(horizontal = 20.dp)
        ) {

            item {

                // ---- Верхняя панель ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Image(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { navController.popBackStack() }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Модуль",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.icon5),
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { navController.navigate("ChildModePage") }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ---- Баннер ----
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFDCE6F7), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Баннер", color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ---- Заголовок ----
                Text(
                    text = "Игры",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Игры состоят из коротких игр способствующие в развитии моторики ребенка",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // ---- Сетка игр ----
            items(2) { rowIndex ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    GameCard(
                        title = "Игра ${rowIndex * 2 + 1}",
                        modifier = Modifier.weight(1f)
                    )

                    GameCard(
                        title = "Игра ${rowIndex * 2 + 2}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
@Composable
fun GameCard(
    title: String,
    modifier: Modifier = Modifier
) {

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF1F3F6)
        ),
        modifier = modifier
            .height(150.dp)
            .clickable { }
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFFDCE6F7))
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ChildGamesPagePreview() {
    ChildGamesPage(navController = rememberNavController())
}