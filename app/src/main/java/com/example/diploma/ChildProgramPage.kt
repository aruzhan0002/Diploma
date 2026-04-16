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
fun ChildProgramPage(navController: NavController) {

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

                // -------- Верхняя панель --------
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

                // -------- Баннер --------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(
                            Color(0xFFDCE6F7),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Баннер", color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // -------- Заголовок --------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Программа",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Программа состоит из коротких видеоуроков способствующие в развитии моторики ребенка",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Завершено 1 из 4",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // -------- Список уроков --------
            items(4) { index ->

                LessonItem(
                    title = "Урок ${index + 1}",
                    isDone = index == 0
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
@Composable
fun LessonItem(
    title: String,
    isDone: Boolean
) {

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF1F3F6)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable { }
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Subtitle",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (isDone) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(
                            Color(0xFF006FFD),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = Color.White, fontSize = 12.sp)
                }
            } else {
                Text("›", fontSize = 20.sp, color = Color.Gray)
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ChildProgramPagePreview() {
    ChildProgramPage(navController = rememberNavController())
}