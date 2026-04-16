package com.example.diploma
import android.R.attr.onClick
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun SpecialistCoursesPage(navController: NavController) {

    Scaffold(
        bottomBar = {
            SpecialistBottomBar(navController, selectedIndex = 3)
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "С возврашением, Павел",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(15.dp))


            }

            // ===== СТАТИСТИКА 2х2 =====

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Всего курсов", "3")
                    StatCard("Всего покупок", "4.9")
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Общая прибыль", "14,804")
                    StatCard("Средний рейтинг", "4.9★")
                }
            }


            item {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.width(343.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Мои курсы",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                Color(0xFF006FFD),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { navController.navigate("CreateCoursePage")}
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "+ добавить курс",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // ===== СПИСОК КУРСОВ =====

            items(
                items = CourseRepository.courses,
                key = { it.id }
            ) { course ->
                CourseCard1(
                    course = course
                ) {
                    navController.navigate("CourseDetailsPage")
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
@Composable
fun StatCard(title: String, value: String) {

    Box(
        modifier = Modifier
            .height(94.dp)
            .width(168.dp)
            .background(
                Color(0xFFF3F5F9),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp)
    ) {

        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CourseCard1(
    course: Course,
    onClick: () -> Unit
) {


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp)
            .background(
                Color(0xFFF4F6FA),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onClick() }
    ) {

        Box(
            modifier = Modifier
                .width(115.dp)
                .fillMaxHeight()
                .background(
                    Color(0xFFDCE6F5),
                    shape = RoundedCornerShape(
                        topStart = 22.dp,
                        bottomStart = 22.dp
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Плейсхолдер под будущую обложку курса (из бэка)
        }

        // ===== ПРАВАЯ ЧАСТЬ =====
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // --- Верхний блок ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // Бейдж
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFF2F6FED),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = course.audienceLabel,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = course.rating,
                    fontWeight = FontWeight.Medium
                )
            }

            // --- Название ---
            Column {
                Text(
                    text = course.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = course.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2
                )
            }

            // --- Нижний блок ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    CircleIconButton(R.drawable.icon_edit)
                    CircleIconButton(R.drawable.icon_add)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = course.price,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
@Composable
fun CircleIconButton(iconRes: Int) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(
                Color(0xFFE8EEF9),
                shape = CircleShape
            )
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color(0xFF2F6FED),
            modifier = Modifier.size(13.dp)
        )
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewSpecialistCoursesPage() {
    SpecialistCoursesPage(navController = rememberNavController())
}