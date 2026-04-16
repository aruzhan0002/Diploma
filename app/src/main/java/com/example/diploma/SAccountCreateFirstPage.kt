package com.example.diploma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SAccountCreateFirstPage(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isChecked by remember { mutableStateOf(false) }

    val blue = Color(0xFF006FFD)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StepProgressBar1(currentStep = 1)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Создайте аккаунт",
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .width(327.dp)
                        .height(40.dp)
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Ваша почта
                Text(
                    text = "Ваша почта",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                    modifier = Modifier
                        .width(327.dp)
                        .height(20.dp)

                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("name@email.com") },
                    modifier = Modifier
                        .width(327.dp)
                        .height(50.dp)
                        .border(1.dp, Color(0xFFC5C6CC), RoundedCornerShape(12.dp)),
                    textStyle = TextStyle(fontSize = 16.sp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    )
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Создайте пароль
                Text(
                    text = "Создайте пароль",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                    modifier = Modifier
                        .width(327.dp)
                        .height(20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                painter = painterResource(id = R.drawable.img3),
                                contentDescription = "Toggle password visibility",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .width(327.dp)
                        .height(48.dp)
                        .border(1.dp, Color(0xFFC5C6CC), RoundedCornerShape(12.dp)),
                    textStyle = TextStyle(fontSize = 16.sp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    )
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Подтвердите пароль
                Text(
                    text = "Подтвердите пароль",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                    modifier = Modifier
                        .width(327.dp)
                        .height(20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Подтвердите пароль") },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                painter = painterResource(id = R.drawable.img3),
                                contentDescription = "Toggle password visibility",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .width(327.dp)
                        .height(48.dp)
                        .border(1.dp, Color(0xFFC5C6CC), RoundedCornerShape(12.dp)),
                    textStyle = TextStyle(fontSize = 16.sp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // чекбокс
                Row(
                    modifier = Modifier.width(327.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it }
                    )
                    Text(
                        text = "Я ознакомился(-ась) с ТП Правами и Условиями и Политикой безопасности.",
                        style = TextStyle(fontSize = 14.sp),
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ✅ КНОПКИ — ВСЕГДА СНИЗУ
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (isChecked) {
                            navController.navigate("SAccountCreateSecondPage")
                        }
                    },
                    modifier = Modifier
                        .width(327.dp)
                        .height(45.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blue)
                ) {
                    Text(text = "Далее", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .width(327.dp)
                        .height(45.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, blue),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Text(text = "Назад", color = blue)
                }
            }
        }
    }
}

@Composable
fun StepProgressBar1(currentStep: Int) {
    Row(
        modifier = Modifier
            .padding(top = 50.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Create 9 steps
        for (i in 1..7) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(43.dp) // Размер по ширине
                    .height(8.dp)  // Высота каждого индикатора
                    .clip(RoundedCornerShape(4.dp)) // Закругление углов
                    .background(
                        color = if (i <= currentStep) Color.Blue else Color(0xFFE8E9F1)
                    )
            ) {
                // Отображение в каждом Box, если нужно, можно добавить контент
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewSAccountCreateFirstPage() {
    SAccountCreateFirstPage(navController = rememberNavController())
}
