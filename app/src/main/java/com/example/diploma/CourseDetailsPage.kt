package com.example.diploma
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
@Composable
fun CourseDetailsPage(navController: NavController) {

    val tabs = listOf("Вступление", "Силабус", "Инструктор")
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF3D6DE0))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Изменить", color = Color.White, fontSize = 16.sp)
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF3D6DE0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("-", color = Color.White, fontSize = 24.sp)
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {

            // Верхняя картинка
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0xFFDCE6F5))
            ) {

                Icon(
                    painter = painterResource(R.drawable.icon_close),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 20.dp, top = 45.dp)
                        .size(22.dp)
                        .align(Alignment.TopStart)
                        .clickable { navController.popBackStack() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            Spacer(modifier = Modifier.height(16.dp))

            CourseTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            when(selectedTab){
                0 -> IntroductionContent()
                1 -> CourseSyllabusContent()
                2 -> InstructorContent()
            }
        }
    }
}

@Composable
fun IntroductionContent() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("О курсе", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Icon(
                painter = painterResource(R.drawable.icon_heart),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "The perfect T-shirt for when you want to feel comfortable but still stylish. Amazing for all occasions. Made of 100% cotton fabric in four colours.",
            color = Color.Gray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Чему вы научитесь?", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(10.dp))

        Text("• The perfect T-shirt", color = Color.Gray)
        Text("• Made of 100% cotton", color = Color.Gray)

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

            Tag("РОДИТЕЛЯМ")
            Tag("ДЛЯ НАЧИНАЮЩИХ")
        }
    }
}


@Composable
fun InstructorContent() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFDCE6F5)),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    painter = painterResource(R.drawable.icon_user),
                    contentDescription = null,
                    tint = Color(0xFF6F9AD3),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {

                Text(
                    text = "Контент курса",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Спич ланг",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "The perfect T-shirt for when you want to feel comfortable but still stylish. Amazing for all occasions. Made of 100% cotton fabric in four colours. Its modern style gives a lighter look to the outfit. Perfect for the warmest days.",
            fontSize = 14.sp,
            color = Color.Gray,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun LessonItem(title: String, type: String, time: String) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF4F6FA))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(type, color = Color.Gray, fontSize = 12.sp)
        }

        if (time.isNotEmpty()) {
            Text(time, color = Color(0xFF3D6DE0))
        }
    }
}

@Composable
fun Tag(text: String) {

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF3D6DE0))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, color = Color.White, fontSize = 10.sp)
    }
}
@Composable
fun CourseTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {

    val tabs = listOf("Вступление", "Силабус", "Инструктор")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Color(0xFFF1F3F7)),
        verticalAlignment = Alignment.CenterVertically
    ) {

        tabs.forEachIndexed { index, title ->

            val selected = index == selectedTab

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        if (selected) Color.White else Color.Transparent
                    )
                    .clickable { onTabSelected(index) },
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) Color.Black else Color.Gray
                )
            }
        }
    }
}

@Composable //Силабус
fun CourseSyllabusContent() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Контент курса",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "3 урока • 6 часов",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        LessonCard(
            title = "Вступление",
            type = "Видео",
            time = "30 мин"
        )

        Spacer(modifier = Modifier.height(12.dp))

        LessonCard(
            title = "Урок 1",
            type = "PDF",
            time = null
        )

        Spacer(modifier = Modifier.height(12.dp))

        LessonCard(
            title = "Урок 2",
            type = "Видео",
            time = "40 мин"
        )
    }
}
@Composable
fun LessonCard(
    title: String,
    type: String,
    time: String?
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF3F5F9))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFAFC6E9))
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = type,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        if (time != null) {

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE7F0FF))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = Color(0xFF2F6DE1),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewInstructorContent() {
    InstructorContent()
}