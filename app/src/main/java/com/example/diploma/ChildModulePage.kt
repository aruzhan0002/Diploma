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
fun ChildModulePage(navController: NavController) {

    Scaffold(
        bottomBar = {
            BottomBar(navController, selectedIndex = 0)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

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
                            .clickable {
                                navController.popBackStack()
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Модуль",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.icon5),
                    contentDescription = null,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { navController.navigate("ChildModePage") }
                )
            }

            // -------- Верхняя карточка --------
            MainLessonCard()

            // -------- Программа --------
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F6)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {

                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color(0xFFDCE6F7))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Программа",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "урок 1 из 4",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                ModuleCard(
                    title = "Практика",
                    navController = navController,
                    modifier = Modifier.weight(1f)
                )

                ModuleCard(
                    title = "Игры",
                    navController = navController,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
@Composable
fun ModuleCard(
    title: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF1F3F6)
        ),
        modifier = modifier
            .height(140.dp)
            .clickable {

                when (title) {
                    "Практика" -> navController.navigate("ChildPracticePage")
                    "Игры" -> navController.navigate("ChildGamesPage")
                    "Программа" -> navController.navigate("ChildProgramPage")
                }

            }
    )  {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFFDCE6F7),
                        shape = RoundedCornerShape(50)
                    )
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
fun ChildModulePagePreview() {
    ChildModulePage(navController = rememberNavController())
}