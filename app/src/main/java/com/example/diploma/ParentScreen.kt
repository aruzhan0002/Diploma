package kz.aruzhan.care_steps

import LoginScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun ParentScreen(navController: NavController) {

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img4), // замените на свой ресурс
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(538.dp),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(Alignment.CenterVertically)
                ) {
                    Text(
                        text = "Давайте познакомимся",
                        style = TextStyle(
                            fontSize = 24.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally) // Центрируем по горизонтали
                            .padding(top = 20.dp, bottom = 5.dp) // Отступ сверху и снизу
                    )

                    Text(
                        text = "с вашим ребенком",
                        style = TextStyle(
                            fontSize = 24.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally) // Центрируем по горизонтали
                            .padding(top = 5.dp, bottom = 20.dp) // Отступ снизу
                    )
                }




                Box(
                    modifier = Modifier
                        .width(320.dp) // Ширина контейнера
                        .height(80.dp) // Высота контейнера
                        .align(Alignment.CenterHorizontally) // Центрируем Box по горизонтали
                        .padding(top = 20.dp, bottom = 20.dp) // Отступы
                ) {
                    Text(
                        text = "Мы поможем подобрать подходящие занятия и создать комфортную среду обучения.",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.Gray
                        ),
                        modifier = Modifier
                            .align(Alignment.Center) // Центрируем текст внутри контейнера
                    )
                }


                Button(
                    onClick = {
                        navController.navigate("CreateAccountFirstPage") // Переход на страницу создания аккаунта
                    },
                    modifier = Modifier
                        .width(327.dp) // Ширина кнопки
                        .height(45.dp) // Высота кнопки
                        .border(5.dp, Color(0xFF006FFD), shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)), // Скругленные углы
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006FFD)) // Синий фон
                ) {
                    Text(text = "Далее", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        // Просто вызываем ParentScreen() без передачи navController
        ParentScreen(navController = rememberNavController()) // Для UI без навигации
    }
}

