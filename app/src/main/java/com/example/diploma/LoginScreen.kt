import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.diploma.R

@Composable
fun LoginScreen(navController: NavController) {
    var passwordVisible by remember { mutableStateOf(false) } // Состояние видимости пароля
    var passwordText by remember { mutableStateOf("") }
    var emailText by remember { mutableStateOf("") }// Текст пароля
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок экрана

        // Изображение в верхней части экрана
        Image(
            painter = painterResource(id = R.drawable.img2), // Замените на свой ресурс
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(315.dp), // Регулируйте высоту по необходимости
            contentScale = ContentScale.Crop
        )
        Spacer(
            modifier = Modifier.padding(20.dp)
        )
        Text(
            text = "С возвращением",
            style = TextStyle(
                fontSize = 30.sp,
                color = Color.Black,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)
        )

        // Поля ввода
        Column(

            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),

        ) {
            // Поле для ввода почты
            TextField(
                value = emailText,
                onValueChange = { emailText = it },
                label = { Text("Почта") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(50.dp)
                    .border(1.dp, Color(0xC5C6CC), shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                textStyle = TextStyle(fontSize = 16.sp),
                singleLine = true // Оставляем поле для ввода одной строки
            )

            // Поле для ввода пароля
            TextField(
                value = passwordText,
                onValueChange = { passwordText = it },
                label = { Text("Пароль") },
                modifier = Modifier
                    .width(327.dp)
                    .padding(vertical = 8.dp)
                    .height(50.dp)
                    .border(1.dp, Color(0xC5C6CC), shape = RoundedCornerShape(12.dp)) // Рамка для поля
                    .clip(RoundedCornerShape(12.dp)),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(painter = painterResource(id = R.drawable.img3), contentDescription = "Toggle password visibility",
                            modifier = Modifier.size(16.dp))
                    }
                }
            )

            // Ссылка для забытого пароля
            TextButton(
                onClick = { /* Handle forgot password action */ },

            ) {
                Text(
                    text = "Забыли пароль?",
                    style = TextStyle(fontSize = 16.sp, color = Color(0xFF006FFD)),
                    modifier = Modifier.padding(vertical = 8.dp)

                )
            }
        }

        // Кнопки
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Отступы между кнопками
        ) {
            // Кнопка входа
            Button(
                onClick = {
                    navController.navigate("SettingsPage")
                },
                modifier = Modifier
                    .width(327.dp) // Ширина кнопки
                    .height(45.dp) // Высота кнопки
                    .border(5.dp, Color(0xFF006FFD), shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)), // Скругленные углы
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006FFD)) // Синий фон
            ) {
                Text(text = "Войти", color = Color.White) // Белый текст
            }
            Divider(
                color = Color.Gray.copy(alpha = 0.4f), // 40% непрозрачности (60% прозрачности)
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp) // Отступ сверху и снизу
            )

            // Текст "или" — вставляем сюда между кнопками
            Text(
                text = "или",
                style = TextStyle(fontSize = 16.sp, color = Color.Gray),
                modifier = Modifier.padding(vertical = 5.dp).align(Alignment.CenterHorizontally) // Центрируем текст
            )

            // Кнопка регистрации
            Button(
                onClick = { navController.popBackStack()},
                modifier = Modifier
                    .width(327.dp) // Ширина кнопки
                    .height(45.dp)
                    .border(1.dp, Color(0xFF006FFD), shape = RoundedCornerShape(12.dp)), // Синяя рамка с закруглениями
                colors = ButtonDefaults.buttonColors(containerColor = Color.White) // Белый фон
            ) {
                Text(text = "Зарегистрироваться", color = Color(0xFF006FFD)) // Синий текст
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    val navController = rememberNavController()  // Создаем NavController
//    MaterialTheme {
//        LoginScreen(navController = navController) // Передаем его в LoginScreen
//    }
//}
